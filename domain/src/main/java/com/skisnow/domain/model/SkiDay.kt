package com.skisnow.domain.model

import java.time.Instant

/**
 * One recorded ski day / track session.
 */
data class SkiDay(
    val id: SessionId,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val status: SessionStatus,
)
