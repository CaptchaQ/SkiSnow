package com.skisnow.domain.usecase

import com.skisnow.domain.model.SessionStatus
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.port.LocationTracker
import com.skisnow.domain.port.SessionRepository
import com.skisnow.domain.port.StatsCalculator
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class StartSession(
    private val sessions: SessionRepository,
    private val locationTracker: LocationTracker,
) {
    /**
     * Creates a new recording session if none is active and starts GPS tracking.
     * @throws IllegalStateException if a session is already active or GPS cannot start.
     */
    suspend operator fun invoke(startedAt: Instant = Instant.now()): SkiDay {
        val active = sessions.getActiveSession()
        if (active != null && active.status in setOf(SessionStatus.RECORDING, SessionStatus.PAUSED)) {
            throw IllegalStateException("Active session already exists: ${active.id.value}")
        }
        val day = sessions.createSession(startedAt)
        val started = locationTracker.start(day.id)
        if (!started) {
            sessions.updateStatus(day.id, SessionStatus.FAILED, endedAt = Instant.now())
            throw IllegalStateException("Location permission required to start tracking")
        }
        return day
    }
}

class PauseSession(
    private val sessions: SessionRepository,
    private val locationTracker: LocationTracker,
) {
    suspend operator fun invoke() {
        val active = sessions.getActiveSession()
            ?: throw IllegalStateException("No active session to pause")
        if (active.status != SessionStatus.RECORDING) {
            throw IllegalStateException("Session is not recording: ${active.status}")
        }
        sessions.updateStatus(active.id, SessionStatus.PAUSED)
        locationTracker.pause()
    }
}

class ResumeSession(
    private val sessions: SessionRepository,
    private val locationTracker: LocationTracker,
) {
    suspend operator fun invoke() {
        val active = sessions.getActiveSession()
            ?: throw IllegalStateException("No active session to resume")
        if (active.status != SessionStatus.PAUSED) {
            throw IllegalStateException("Session is not paused: ${active.status}")
        }
        val resumed = locationTracker.resume(active.id)
        if (!resumed) {
            throw IllegalStateException("Location permission required to resume tracking")
        }
        sessions.updateStatus(active.id, SessionStatus.RECORDING)
    }
}

class StopSession(
    private val sessions: SessionRepository,
    private val statsCalculator: StatsCalculator,
    private val locationTracker: LocationTracker,
) {
    /**
     * Computes stats and marks SAVED/FAILED.
     * [stopTracking]=true (UI): tear down GPS/FGS via tracker first.
     * [stopTracking]=false (notification FINALIZE / already torn down).
     *
     * If session is already STOPPING (crash mid-stop), **resume finalize**
     * instead of early-return — otherwise recover/Force reset become no-ops.
     */
    suspend operator fun invoke(
        endedAt: Instant = Instant.now(),
        stopTracking: Boolean = true,
    ): SkiDay {
        val active = sessions.getActiveSession()
            ?: throw IllegalStateException("No active session to stop")

        // Note: getActiveSession only returns RECORDING/PAUSED/STOPPING.
        // STOPPING must continue finalize (not early-return).

        val alreadyStopping = active.status == SessionStatus.STOPPING
        // If we already entered STOPPING, GPS is likely gone — skip tracker.stop re-entry.
        if (stopTracking && !alreadyStopping) {
            locationTracker.stop()
        }
        if (!alreadyStopping) {
            sessions.updateStatus(active.id, SessionStatus.STOPPING)
        }

        return try {
            val points = sessions.getTrackPoints(active.id)
            val stats = statsCalculator.calculate(points)
            sessions.saveStats(active.id, stats)
            sessions.updateStatus(active.id, SessionStatus.SAVED, endedAt = endedAt)
            sessions.getSession(active.id)
                ?: error("Session missing after save: ${active.id.value}")
        } catch (t: Throwable) {
            sessions.updateStatus(active.id, SessionStatus.FAILED, endedAt = endedAt)
            throw t
        }
    }
}

class ObserveActiveSession(
    private val sessions: SessionRepository,
) {
    operator fun invoke(): Flow<SkiDay?> = sessions.observeActiveSession()
}

class ObserveSessionHistory(
    private val sessions: SessionRepository,
) {
    operator fun invoke(): Flow<List<SkiDay>> = sessions.observeHistory()
}
