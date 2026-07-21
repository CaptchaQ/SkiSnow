package com.skisnow.data.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.port.LocationTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Domain-facing tracker. Starts/stops [TrackingForegroundService] which owns Fused updates.
 * Emits points into a process-wide shared flow for UI + persistence.
 */
class FusedLocationTracker(
    private val appContext: Context,
) : LocationTracker {

    override fun observeTrackPoints(): Flow<TrackPoint> = TrackPointBus.points

    override fun start(sessionId: SessionId): Boolean {
        if (!hasLocationPermission()) return false
        val intent = Intent(appContext, TrackingForegroundService::class.java).apply {
            action = TrackingForegroundService.ACTION_START
            putExtra(TrackingForegroundService.EXTRA_SESSION_ID, sessionId.value)
        }
        ContextCompat.startForegroundService(appContext, intent)
        return true
    }

    override fun pause() {
        val intent = Intent(appContext, TrackingForegroundService::class.java).apply {
            action = TrackingForegroundService.ACTION_PAUSE
        }
        appContext.startService(intent)
    }

    override fun resume(sessionId: SessionId): Boolean {
        if (!hasLocationPermission()) return false
        val intent = Intent(appContext, TrackingForegroundService::class.java).apply {
            action = TrackingForegroundService.ACTION_RESUME
            putExtra(TrackingForegroundService.EXTRA_SESSION_ID, sessionId.value)
        }
        ContextCompat.startForegroundService(appContext, intent)
        return true
    }

    override fun stop() {
        val intent = Intent(appContext, TrackingForegroundService::class.java).apply {
            action = TrackingForegroundService.ACTION_STOP
        }
        appContext.startService(intent)
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}

/**
 * In-process bus from FGS → observers. Not a domain type.
 */
object TrackPointBus {
    private val _points = MutableSharedFlow<TrackPoint>(extraBufferCapacity = 64)
    val points = _points.asSharedFlow()

    fun emit(point: TrackPoint) {
        _points.tryEmit(point)
    }
}
