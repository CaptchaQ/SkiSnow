package com.skisnow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun metric_distance_and_speed() {
        assertEquals(1.0, UnitConverter.distanceKm(1000.0, Units.METRIC), 1e-6)
        assertEquals(36.0, UnitConverter.speedKmh(10.0, Units.METRIC), 1e-6)
        assertEquals("km", UnitConverter.distanceLabel(Units.METRIC))
        assertEquals("km/h", UnitConverter.speedLabel(Units.METRIC))
    }

    @Test
    fun imperial_distance_and_speed() {
        assertEquals(0.621371, UnitConverter.distanceKm(1000.0, Units.IMPERIAL), 1e-4)
        assertEquals(22.369363, UnitConverter.speedKmh(10.0, Units.IMPERIAL), 1e-3)
        assertEquals("mi", UnitConverter.distanceLabel(Units.IMPERIAL))
        assertEquals("mph", UnitConverter.speedLabel(Units.IMPERIAL))
    }

    @Test
    fun altitude_metric_passthrough_imperial_feet() {
        assertEquals(1000.0, UnitConverter.altitudeM(1000.0, Units.METRIC), 1e-6)
        assertEquals(3280.84, UnitConverter.altitudeM(1000.0, Units.IMPERIAL), 0.01)
        assertEquals("ft", UnitConverter.altitudeLabel(Units.IMPERIAL))
    }
}