package com.skisnow.domain.port

import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.TrackPoint
import java.time.Duration
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure stats from track points. Algorithm constants: docs/product/stats-spec.md.
 */
interface StatsCalculator {
    fun calculate(points: List<TrackPoint>): SessionStats
}

/**
 * Proposed-default implementation of [docs/product/stats-spec.md].
 */
class DefaultStatsCalculator(
    private val maxAccuracyM: Double = 25.0,
    private val movingSpeedMps: Double = 0.5,
    private val maxGapSeconds: Long = 120,
    private val minAltitudeDeltaM: Double = 1.0,
    private val medianWindow: Int = 5,
) : StatsCalculator {

    override fun calculate(points: List<TrackPoint>): SessionStats {
        if (points.isEmpty()) return SessionStats()

        val gated = points.filter { p ->
            val acc = p.accuracyM
            acc != null && acc <= maxAccuracyM
        }
        if (gated.isEmpty()) {
            return SessionStats(duration = durationOf(points))
        }

        var distanceM = 0.0
        var movingDistanceM = 0.0
        var movingSeconds = 0.0
        var maxSpeed = 0.0

        for (i in 1 until gated.size) {
            val a = gated[i - 1]
            val b = gated[i]
            val dt = Duration.between(a.time, b.time).seconds
            if (dt < 0) continue
            if (dt > maxGapSeconds) continue

            val segment = haversineM(a.lat, a.lon, b.lat, b.lon)
            distanceM += segment

            val speed = b.speedMps
            if (speed != null && speed > maxSpeed) maxSpeed = speed

            val moving = (speed != null && speed >= movingSpeedMps) ||
                (speed == null && segment / dt.coerceAtLeast(1) >= movingSpeedMps)
            if (moving && dt > 0) {
                movingDistanceM += segment
                movingSeconds += dt.toDouble()
            }
        }

        val altitudes = gated.mapNotNull { it.altitudeM }
        val smoothed = medianSmooth(altitudes, medianWindow)
        var verticalDrop = 0.0
        for (i in 1 until smoothed.size) {
            val delta = smoothed[i - 1] - smoothed[i]
            if (delta >= minAltitudeDeltaM) verticalDrop += delta
        }

        val avgMoving = if (movingSeconds > 0) movingDistanceM / movingSeconds else 0.0

        return SessionStats(
            duration = durationOf(gated),
            distanceM = distanceM,
            verticalDropM = verticalDrop,
            maxSpeedMps = maxSpeed,
            avgMovingSpeedMps = avgMoving,
        )
    }

    private fun durationOf(points: List<TrackPoint>): Duration {
        if (points.size < 2) return Duration.ZERO
        return Duration.between(points.first().time, points.last().time).let {
            if (it.isNegative) Duration.ZERO else it
        }
    }

    private fun medianSmooth(values: List<Double>, window: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        if (window <= 1 || values.size == 1) return values
        val half = window / 2
        return values.indices.map { i ->
            val from = (i - half).coerceAtLeast(0)
            val to = (i + half).coerceAtMost(values.lastIndex)
            val slice = values.subList(from, to + 1).sorted()
            slice[slice.size / 2]
        }
    }

    private fun haversineM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lon2 - lon1)
        val a = sin(dPhi / 2) * sin(dPhi / 2) +
            cos(p1) * cos(p2) * sin(dLambda / 2) * sin(dLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
