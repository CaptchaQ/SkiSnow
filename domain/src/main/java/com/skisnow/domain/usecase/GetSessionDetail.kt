package com.skisnow.domain.usecase

import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.port.SessionRepository

data class SessionDetail(
    val day: SkiDay,
    val points: List<TrackPoint>,
    val stats: SessionStats?,
)

class GetSessionDetail(
    private val sessions: SessionRepository,
) {
    suspend operator fun invoke(id: SessionId): SessionDetail? {
        val day = sessions.getSession(id) ?: return null
        return SessionDetail(
            day = day,
            points = sessions.getTrackPoints(id),
            stats = sessions.getStats(id),
        )
    }
}
