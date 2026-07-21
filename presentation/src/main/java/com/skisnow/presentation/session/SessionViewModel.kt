package com.skisnow.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SessionStatus
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.model.WeatherSnapshot
import com.skisnow.domain.port.LocationTracker
import com.skisnow.domain.port.SessionRepository
import com.skisnow.domain.port.StatsCalculator
import com.skisnow.domain.port.WeatherRepository
import com.skisnow.domain.usecase.ObserveActiveSession
import com.skisnow.domain.usecase.ObserveSessionHistory
import com.skisnow.domain.usecase.PauseSession
import com.skisnow.domain.usecase.ResumeSession
import com.skisnow.domain.usecase.StartSession
import com.skisnow.domain.usecase.StopSession
import java.time.Instant
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(
    val active: SkiDay? = null,
    val points: List<TrackPoint> = emptyList(),
    val liveStats: SessionStats = SessionStats(),
    val history: List<SkiDay> = emptyList(),
    val weather: WeatherSnapshot? = null,
    val weatherError: Boolean = false,
    val permissionNeeded: Boolean = false,
    val errorMessage: String? = null,
    val isBusy: Boolean = false,
) {
    val isRecording: Boolean get() = active?.status == SessionStatus.RECORDING
    val isPaused: Boolean get() = active?.status == SessionStatus.PAUSED
    val isStopping: Boolean get() = active?.status == SessionStatus.STOPPING
    /** True only for user-controllable active session (not STOPPING). */
    val hasActive: Boolean get() =
        active?.status == SessionStatus.RECORDING || active?.status == SessionStatus.PAUSED

    val lastPoint: TrackPoint? get() = points.lastOrNull()

    val speedKmh: Int
        get() = ((liveStats.maxSpeedMps * 3.6).roundToInt())

    val distanceKm: String
        get() = String.format("%.2f", liveStats.distanceM / 1000.0)

    val verticalM: Int
        get() = liveStats.verticalDropM.roundToInt()

    val weatherChip: String?
        get() {
            val h = weather?.hourly?.firstOrNull() ?: return null
            val t = h.temperatureC?.let { "${it.roundToInt()}°C" } ?: "—"
            val snow = h.snowfallCm?.let { " snow ${"%.1f".format(it)}cm" } ?: ""
            val wind = h.windSpeedMps?.let { " wind ${"%.0f".format(it * 3.6)}km/h" } ?: ""
            return "$t$snow$wind"
        }
}

class SessionViewModel(
    private val startSession: StartSession,
    private val pauseSession: PauseSession,
    private val resumeSession: ResumeSession,
    private val stopSession: StopSession,
    private val observeActiveSession: ObserveActiveSession,
    private val observeSessionHistory: ObserveSessionHistory,
    private val locationTracker: LocationTracker,
    private val sessionRepository: SessionRepository,
    private val statsCalculator: StatsCalculator,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    private var pointsJob: Job? = null
    private val livePoints = mutableListOf<TrackPoint>()

    init {
        viewModelScope.launch { recoverStuckSessions() }
        viewModelScope.launch {
            observeActiveSession().collect { day ->
                _state.update { it.copy(active = day, isBusy = false) }
                when (day?.status) {
                    SessionStatus.RECORDING, SessionStatus.PAUSED -> {
                        ensurePointsCollector()
                        hydratePoints(day.id.value)
                    }
                    SessionStatus.STOPPING -> {
                        // Never leave UI without controls: finalize stuck STOPPING.
                        runCatching { stopSession(stopTracking = true) }
                            .onFailure {
                                sessionRepository.updateStatus(
                                    day.id,
                                    SessionStatus.FAILED,
                                    Instant.now(),
                                )
                                locationTracker.stop()
                            }
                    }
                    else -> {
                        if (day == null) {
                            livePoints.clear()
                            _state.update { it.copy(points = emptyList(), liveStats = SessionStats()) }
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            observeSessionHistory().collect { list ->
                _state.update { it.copy(history = list) }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(permissionNeeded = !granted, isBusy = false) }
        if (granted) {
            viewModelScope.launch { doStart() }
        } else {
            _state.update {
                it.copy(errorMessage = "Location permission required to record a ski day")
            }
        }
    }

    fun startIfPermitted(hasPermission: Boolean) {
        if (!hasPermission) {
            _state.update { it.copy(permissionNeeded = true, isBusy = false) }
            return
        }
        viewModelScope.launch { doStart() }
    }

    fun pause() {
        viewModelScope.launch {
            runCatching { pauseSession() }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message, isBusy = false) } }
        }
    }

    fun resume(hasPermission: Boolean) {
        if (!hasPermission) {
            _state.update { it.copy(permissionNeeded = true, isBusy = false) }
            return
        }
        viewModelScope.launch {
            runCatching { resumeSession() }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message, isBusy = false) } }
        }
    }

    fun stop() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            try {
                runCatching { stopSession() }
                    .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
                livePoints.clear()
            } finally {
                _state.update {
                    it.copy(
                        isBusy = false,
                        points = emptyList(),
                        liveStats = SessionStats(),
                    )
                }
            }
        }
    }

    /** Force-clear any stuck active session so Start is available again. */
    fun forceReset() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            try {
                runCatching { stopSession(stopTracking = true) }
                val still = sessionRepository.getActiveSession()
                if (still != null) {
                    sessionRepository.updateStatus(still.id, SessionStatus.FAILED, Instant.now())
                    locationTracker.stop()
                }
                livePoints.clear()
                pointsJob?.cancel()
                pointsJob = null
            } finally {
                _state.update {
                    it.copy(
                        isBusy = false,
                        active = null,
                        points = emptyList(),
                        liveStats = SessionStats(),
                    )
                }
            }
        }
    }

    fun refreshWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            val snap = weatherRepository.getWeather(lat, lon, forceRefresh = false)
            _state.update {
                it.copy(weather = snap, weatherError = snap == null)
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private suspend fun recoverStuckSessions() {
        val active = sessionRepository.getActiveSession() ?: return
        when (active.status) {
            SessionStatus.STOPPING -> {
                runCatching { stopSession(stopTracking = true) }
                    .onFailure {
                        sessionRepository.updateStatus(active.id, SessionStatus.FAILED, Instant.now())
                        locationTracker.stop()
                    }
            }
            SessionStatus.RECORDING, SessionStatus.PAUSED -> {
                // Process death left session active — re-attach GPS when possible.
                if (active.status == SessionStatus.RECORDING) {
                    val ok = locationTracker.start(active.id)
                    if (!ok) {
                        // No permission / FGS cannot start: free UI back to Start.
                        sessionRepository.updateStatus(
                            active.id,
                            SessionStatus.FAILED,
                            Instant.now(),
                        )
                    }
                }
            }
            else -> Unit
        }
    }

    private suspend fun doStart() {
        _state.update { it.copy(isBusy = true, permissionNeeded = false, errorMessage = null) }
        try {
            val existing = sessionRepository.getActiveSession()
            if (existing != null) {
                runCatching { stopSession(stopTracking = true) }
                val still = sessionRepository.getActiveSession()
                if (still != null) {
                    sessionRepository.updateStatus(still.id, SessionStatus.FAILED, Instant.now())
                    locationTracker.stop()
                }
            }
            livePoints.clear()
            ensurePointsCollector()
            startSession()
        } catch (e: Throwable) {
            _state.update { it.copy(errorMessage = e.message ?: "Failed to start") }
        } finally {
            _state.update { it.copy(isBusy = false) }
        }
    }

    private fun ensurePointsCollector() {
        if (pointsJob?.isActive == true) return
        pointsJob = viewModelScope.launch {
            locationTracker.observeTrackPoints().collect { point ->
                livePoints.add(point)
                val snapshot = livePoints.toList()
                val stats = statsCalculator.calculate(snapshot)
                _state.update { it.copy(points = snapshot, liveStats = stats) }
                if (snapshot.size == 1 || snapshot.size % 30 == 0) {
                    refreshWeather(point.lat, point.lon)
                }
            }
        }
    }

    private suspend fun hydratePoints(sessionId: String) {
        val existing = sessionRepository.getTrackPoints(SessionId(sessionId))
        if (existing.isNotEmpty() && livePoints.isEmpty()) {
            livePoints.addAll(existing)
            _state.update {
                it.copy(points = existing, liveStats = statsCalculator.calculate(existing))
            }
        }
    }
}
