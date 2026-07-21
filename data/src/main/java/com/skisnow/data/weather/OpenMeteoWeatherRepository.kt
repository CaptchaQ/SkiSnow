package com.skisnow.data.weather

import com.skisnow.domain.model.HourlyWeather
import com.skisnow.domain.model.WeatherSnapshot
import com.skisnow.domain.port.WeatherRepository
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/**
 * Open-Meteo forecast client (no API key). Network optional; returns null/cached on failure.
 */
class OpenMeteoWeatherRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
) : WeatherRepository {

    @Volatile
    private var cache: WeatherSnapshot? = null

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        forceRefresh: Boolean,
    ): WeatherSnapshot? = withContext(Dispatchers.IO) {
        val cached = cache
        if (!forceRefresh && cached != null &&
            cached.lat == lat && cached.lon == lon &&
            Instant.now().epochSecond - cached.fetchedAt.epochSecond < CACHE_TTL_SEC
        ) {
            return@withContext cached
        }
        runCatching { fetch(lat, lon) }
            .onSuccess { cache = it }
            .getOrElse { cached }
    }

    private fun fetch(lat: Double, lon: Double): WeatherSnapshot {
        val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
            .addQueryParameter("latitude", lat.toString())
            .addQueryParameter("longitude", lon.toString())
            .addQueryParameter(
                "hourly",
                listOf(
                    "temperature_2m",
                    "snowfall",
                    "snow_depth",
                    "wind_speed_10m",
                    "wind_gusts_10m",
                    "freezing_level_height",
                ).joinToString(","),
            )
            .addQueryParameter("forecast_days", "2")
            .addQueryParameter("timezone", "UTC")
            .addQueryParameter("wind_speed_unit", "ms")
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Open-Meteo HTTP ${response.code}")
            val body = response.body?.string() ?: error("empty body")
            return parse(lat, lon, body)
        }
    }

    private fun parse(lat: Double, lon: Double, body: String): WeatherSnapshot {
        val root = JSONObject(body)
        val hourly = root.getJSONObject("hourly")
        val times = hourly.getJSONArray("time")
        val temps = hourly.optJSONArray("temperature_2m")
        val snow = hourly.optJSONArray("snowfall")
        val depth = hourly.optJSONArray("snow_depth")
        val wind = hourly.optJSONArray("wind_speed_10m")
        val gusts = hourly.optJSONArray("wind_gusts_10m")
        val freeze = hourly.optJSONArray("freezing_level_height")
        val limit = minOf(times.length(), 48)
        val rows = ArrayList<HourlyWeather>(limit)
        for (i in 0 until limit) {
            rows += HourlyWeather(
                time = parseHourlyTime(times.getString(i)),
                temperatureC = temps.optDoubleOrNull(i),
                snowfallCm = snow.optDoubleOrNull(i),
                snowDepthM = depth.optDoubleOrNull(i),
                windSpeedMps = wind.optDoubleOrNull(i),
                windGustsMps = gusts.optDoubleOrNull(i),
                freezingLevelM = freeze.optDoubleOrNull(i),
            )
        }
        return WeatherSnapshot(
            lat = lat,
            lon = lon,
            fetchedAt = Instant.now(),
            hourly = rows,
        )
    }

    private fun parseHourlyTime(raw: String): Instant {
        return try {
            when {
                raw.endsWith("Z") -> Instant.parse(raw)
                raw.length == 16 -> Instant.parse("${raw}:00Z")
                else -> Instant.parse(raw)
            }
        } catch (_: Exception) {
            Instant.now()
        }
    }

    private fun JSONArray?.optDoubleOrNull(index: Int): Double? {
        if (this == null || isNull(index)) return null
        val v = optDouble(index, Double.NaN)
        return v.takeUnless { it.isNaN() }
    }

    companion object {
        private const val CACHE_TTL_SEC = 15 * 60L
    }
}
