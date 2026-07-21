package com.skisnow.domain.port

import com.skisnow.domain.model.LocationSource
import com.skisnow.domain.model.TrackPoint
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultStatsCalculatorTest {

    private val calculator = DefaultStatsCalculator()

    @Test
    fun emptyList_returnsZeros() {
        val stats = calculator.calculate(emptyList())
        assertEquals(0.0, stats.distanceM, 0.0)
        assertEquals(0.0, stats.verticalDropM, 0.0)
        assertEquals(0.0, stats.maxSpeedMps, 0.0)
        assertEquals(0L, stats.duration.seconds)
    }

    @Test
    fun accuracyOutliers_areDroppedFromDistance() {
        val t0 = Instant.parse("2026-01-01T10:00:00Z")
        val points = listOf(
            point(t0, 45.0, 6.0, accuracy = 5.0, speed = 10.0),
            point(t0.plusSeconds(10), 45.001, 6.0, accuracy = 80.0, speed = 50.0),
            point(t0.plusSeconds(20), 45.002, 6.0, accuracy = 5.0, speed = 12.0),
        )
        val stats = calculator.calculate(points)
        assertTrue(stats.distanceM > 100.0)
        assertEquals(12.0, stats.maxSpeedMps, 0.01)
    }

    @Test
    fun gapOver120s_skipsSegment() {
        val t0 = Instant.parse("2026-01-01T10:00:00Z")
        val points = listOf(
            point(t0, 45.0, 6.0, accuracy = 5.0, speed = 5.0),
            point(t0.plusSeconds(200), 45.01, 6.0, accuracy = 5.0, speed = 5.0),
        )
        val stats = calculator.calculate(points)
        assertEquals(0.0, stats.distanceM, 0.0)
    }

    @Test
    fun pureDescent_accumulatesVerticalDrop() {
        val t0 = Instant.parse("2026-01-01T10:00:00Z")
        val points = (0..10).map { i ->
            point(
                time = t0.plusSeconds(i.toLong()),
                lat = 45.0,
                lon = 6.0 + i * 0.0001,
                accuracy = 5.0,
                speed = 8.0,
                altitude = 2000.0 - i * 10.0,
            )
        }
        val stats = calculator.calculate(points)
        assertTrue("verticalDrop=${stats.verticalDropM}", stats.verticalDropM >= 80.0)
    }

    private fun point(
        time: Instant,
        lat: Double,
        lon: Double,
        accuracy: Double,
        speed: Double,
        altitude: Double? = null,
    ) = TrackPoint(
        time = time,
        lat = lat,
        lon = lon,
        altitudeM = altitude,
        speedMps = speed,
        accuracyM = accuracy,
        source = LocationSource.FUSED,
    )
}
