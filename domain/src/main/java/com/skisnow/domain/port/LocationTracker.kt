package com.skisnow.domain.port

import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.TrackPoint
import kotlinx.coroutines.flow.Flow

/**
 * Streams location samples while recording.
 * Android Fused/FGS implementation lives in data layer.
 * Pause/Stop must call [stop] / [pause] so GPS is not left running.
 */
interface LocationTracker {
    fun observeTrackPoints(): Flow<TrackPoint>

    /** @return true if tracking actually started (permission + FGS launch). */
    fun start(sessionId: SessionId): Boolean

    fun pause()

    /** @return true if tracking actually resumed. */
    fun resume(sessionId: SessionId): Boolean

    fun stop()
}
