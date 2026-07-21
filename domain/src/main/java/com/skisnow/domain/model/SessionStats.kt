package com.skisnow.domain.model

import java.time.Duration

/**
 * Aggregated stats for a ski day. Computed by [com.skisnow.domain.port.StatsCalculator].
 */
data class SessionStats(
    val duration: Duration = Duration.ZERO,
    val distanceM: Double = 0.0,
    val verticalDropM: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val avgMovingSpeedMps: Double = 0.0,
)
