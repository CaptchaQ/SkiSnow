package com.skisnow.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.port.StatsCalculator
import com.skisnow.domain.usecase.GetSessionDetail
import com.skisnow.domain.model.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val day: SkiDay? = null,
    val points: List<TrackPoint> = emptyList(),
    val stats: SessionStats? = null,
    val elevationProfile: List<Pair<Double, Double>> = emptyList(),
    val notFound: Boolean = false,
) {
    val maxSpeedMps: Double get() = stats?.maxSpeedMps ?: 0.0
    val distanceM: Double get() = stats?.distanceM ?: 0.0
    val verticalDropM: Double get() = stats?.verticalDropM ?: 0.0
    val durationMs: Long get() = stats?.duration?.toMillis() ?: 0L
    val hasData: Boolean get() = day != null && points.isNotEmpty()
}

class SessionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getSessionDetail: GetSessionDetail,
    private val statsCalculator: StatsCalculator,
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle.get<String>(KEY_SESSION_ID)) {
        "session id missing"
    }

    private val _state = MutableStateFlow(SessionDetailUiState())
    val state: StateFlow<SessionDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val detail = getSessionDetail(SessionId(sessionId))
        if (detail == null) {
            _state.update { it.copy(notFound = true) }
            return
        }
        val profile = statsCalculator.elevationProfile(detail.points)
        _state.update {
            it.copy(
                day = detail.day,
                points = detail.points,
                stats = detail.stats,
                elevationProfile = profile,
            )
        }
    }

    companion object {
        const val KEY_SESSION_ID = "session_id"
    }
}