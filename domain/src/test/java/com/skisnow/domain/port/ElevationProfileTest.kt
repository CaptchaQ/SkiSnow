package com.skisnow.domain.port

import com.skisnow.domain.model.LocationSource
import com.skisnow.domain.model.TrackPoint
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElevationProfileTest {

    private val calc = DefaultStatsCalculator()

    @Test
    fun emptyPoints_returnsEmptyProfile() {
        assertTrue(calc.elevationProfile(emptyList()).isEmpty())
    }

    @Test
    fun onlyPointsWithoutAltitude_returnsEmpty() {
        val t = Instant.parse("2026-01-01T10:00:00Z")
        val pts = listOf(
            TrackPoint(t, 45.0, 6.0, altitudeM = null, speedMps = 8.0, accuracyM = 5.0, source = LocationSource.FUSED),
            TrackPoint(t.plusSeconds(1), 45.001, 6.0, altitudeM = null, speedMps = 9.0, accuracyM = 5.0, source = LocationSource.FUSED),
        )
        assertTrue(calc.elevationProfile(pts).isEmpty())
    }

    @Test
    fun staircaseDescent_monotonicAltitudes() {
        val t = Instant.parse("2026-01-01T10:00:00Z")
        val pts = (0..9).map { i ->
            TrackPoint(
                time = t.plusSeconds(i.toLong()),
                lat = 45.0 + i * 0.001,
                lon = 6.0,
                altitudeM = 1000.0 - i * 20.0,
                speedMps = 10.0,
                accuracyM = 5.0,
                source = LocationSource.FUSED,
            )
        }
        val profile = calc.elevationProfile(pts)
        assertEquals(pts.size, profile.size)
        assertTrue(profile.first().first == 0.0)
        assertTrue("first alt", profile.first().second > profile.last().second)
        assertTrue("last dist", profile.last().first > 0.0)
    }

    @Test
    fun downsample_respectsMaxSamples() {
        val t = Instant.parse("2026-01-01T10:00:00Z")
        val pts = (0 until 1000).map { i ->
            TrackPoint(
                time = t.plusSeconds(i.toLong()),
                lat = 45.0,
                lon = 6.0 + i * 0.00001,
                altitudeM = 1000.0,
                speedMps = 5.0,
                accuracyM = 5.0,
                source = LocationSource.FUSED,
            )
        }
        val profile = calc.elevationProfile(pts, maxSamples = 50)
        assertTrue(profile.size in 51..51)
    }
}