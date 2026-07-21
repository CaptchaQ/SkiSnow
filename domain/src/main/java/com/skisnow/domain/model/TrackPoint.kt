package com.skisnow.domain.model

import java.time.Instant

/**
 * Single GPS sample for a ski day. Domain uses SI units.
 */
data class TrackPoint(
    val time: Instant,
    val lat: Double,
    val lon: Double,
    val altitudeM: Double? = null,
    val speedMps: Double? = null,
    val accuracyM: Double? = null,
    val source: LocationSource = LocationSource.UNKNOWN,
)
