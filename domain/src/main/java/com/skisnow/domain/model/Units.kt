package com.skisnow.domain.model

/**
 * User-facing display units. Domain stores SI; UI converts based on this preference.
 */
data class UserSettings(
    val units: Units = Units.METRIC,
)

enum class Units {
    METRIC,
    IMPERIAL,
}

/**
 * Pure unit conversions (domain stores SI).
 */
object UnitConverter {

    fun distanceKm(distanceM: Double, units: Units): Double = when (units) {
        Units.METRIC -> distanceM / 1000.0
        Units.IMPERIAL -> distanceM / 1000.0 * 0.621371
    }

    fun speedKmh(speedMps: Double, units: Units): Double = when (units) {
        Units.METRIC -> speedMps * 3.6
        Units.IMPERIAL -> speedMps * 2.2369363
    }

    fun distanceLabel(units: Units): String = when (units) {
        Units.METRIC -> "km"
        Units.IMPERIAL -> "mi"
    }

    fun speedLabel(units: Units): String = when (units) {
        Units.METRIC -> "km/h"
        Units.IMPERIAL -> "mph"
    }

    fun altitudeM(valueM: Double, units: Units): Double = when (units) {
        Units.METRIC -> valueM
        Units.IMPERIAL -> valueM * 3.28084
    }

    fun altitudeLabel(units: Units): String = when (units) {
        Units.METRIC -> "m"
        Units.IMPERIAL -> "ft"
    }
}