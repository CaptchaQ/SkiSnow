package com.skisnow.data.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.skisnow.data.R
import com.skisnow.domain.model.LocationSource
import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.port.SessionRepository
import com.skisnow.domain.usecase.StopSession
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service type location. Persistent notification with Stop action.
 *
 * Stop paths (no re-entrancy loop):
 * - UI [StopSession] → [LocationTracker.stop] → [ACTION_STOP] → GPS/FGS teardown only
 * - Notification → [ACTION_FINALIZE] → [StopSession](stopTracking=false) then teardown
 *   (does not call tracker.stop, so no second ACTION_STOP mid-finalize)
 */
class TrackingForegroundService : Service() {

    private val sessions: SessionRepository by inject()
    private val stopSession: StopSession by inject()
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sessionId: String? = null
    private var receiving = false
    private val shuttingDown = AtomicBoolean(false)

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (shuttingDown.get()) return
            val sid = sessionId ?: return
            val points = result.locations.map { it.toTrackPoint() }
            if (points.isEmpty()) return
            points.forEach(TrackPointBus::emit)
            scope.launch {
                sessions.appendTrackPoints(SessionId(sid), points)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_RESUME -> {
                if (shuttingDown.get()) return START_NOT_STICKY
                sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: sessionId
                startAsForeground()
                startLocationUpdates()
            }
            ACTION_PAUSE -> {
                stopLocationUpdates()
                updateNotification(paused = true)
            }
            ACTION_STOP -> {
                // Teardown only — domain finalize is owned by StopSession (UI) or ACTION_FINALIZE.
                teardownService()
            }
            ACTION_FINALIZE -> {
                // Notification Stop: GPS off → finalize session without re-entering tracker.stop.
                if (!shuttingDown.compareAndSet(false, true)) {
                    return START_NOT_STICKY
                }
                stopLocationUpdates()
                scope.launch {
                    runCatching {
                        stopSession(stopTracking = false)
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
            else -> {
                if (!shuttingDown.get()) startAsForeground()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        scope.cancel()
        super.onDestroy()
    }

    private fun teardownService() {
        if (!shuttingDown.compareAndSet(false, true) && receiving.not()) {
            // Already shutting down — still ensure stopSelf for sticky restarts.
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startAsForeground() {
        ensureChannel()
        val notification = buildNotification(paused = false)
        if (Build.VERSION.SDK_INT >= 29) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationUpdates() {
        if (receiving || shuttingDown.get()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(1f)
            .setWaitForAccurateLocation(false)
            .build()
        try {
            fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
            receiving = true
            updateNotification(paused = false)
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        if (!receiving) return
        fused.removeLocationUpdates(callback)
        receiving = false
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.tracking_channel_desc)
        }
        mgr.createNotificationChannel(channel)
    }

    private fun buildNotification(paused: Boolean): Notification {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        val contentPi = PendingIntent.getActivity(
            this,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = Intent(this, TrackingForegroundService::class.java).apply {
            action = ACTION_FINALIZE
        }
        val stopPi = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = if (paused) {
            getString(R.string.tracking_notification_paused)
        } else {
            getString(R.string.tracking_notification_title)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(getString(R.string.tracking_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(contentPi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.tracking_notification_stop),
                stopPi,
            )
            .build()
    }

    private fun updateNotification(paused: Boolean) {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(NOTIFICATION_ID, buildNotification(paused))
    }

    private fun Location.toTrackPoint(): TrackPoint = TrackPoint(
        time = Instant.ofEpochMilli(time),
        lat = latitude,
        lon = longitude,
        altitudeM = if (hasAltitude()) altitude else null,
        speedMps = if (hasSpeed()) speed.toDouble() else null,
        accuracyM = if (hasAccuracy()) accuracy.toDouble() else null,
        source = LocationSource.FUSED,
    )

    companion object {
        const val ACTION_START = "com.skisnow.data.location.START"
        const val ACTION_PAUSE = "com.skisnow.data.location.PAUSE"
        const val ACTION_RESUME = "com.skisnow.data.location.RESUME"
        /** Tear down GPS/FGS only (from LocationTracker.stop after domain finalize starts). */
        const val ACTION_STOP = "com.skisnow.data.location.STOP"
        /** Notification Stop: finalize session (stopTracking=false) then tear down. */
        const val ACTION_FINALIZE = "com.skisnow.data.location.FINALIZE"
        const val EXTRA_SESSION_ID = "session_id"
        private const val CHANNEL_ID = "skisnow_tracking"
        private const val NOTIFICATION_ID = 42
    }
}
