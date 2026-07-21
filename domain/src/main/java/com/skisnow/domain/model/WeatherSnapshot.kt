package com.skisnow.domain.model

import java.time.Instant

data class HourlyWeather(
    val time: Instant,
    val temperatureC: Double? = null,
    val snowfallCm: Double? = null,
    val snowDepthM: Double? = null,
    val windSpeedMps: Double? = null,
    val windGustsMps: Double? = null,
    val freezingLevelM: Double? = null,
)

data class WeatherSnapshot(
    val lat: Double,
    val lon: Double,
    val fetchedAt: Instant,
    val hourly: List<HourlyWeather> = emptyList(),
)
