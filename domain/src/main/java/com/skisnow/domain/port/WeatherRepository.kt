package com.skisnow.domain.port

import com.skisnow.domain.model.WeatherSnapshot

/**
 * Mountain weather fetch + cache. Provider swap stays behind this port (ADR-0004).
 */
interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double, forceRefresh: Boolean = false): WeatherSnapshot?
}
