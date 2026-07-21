package com.skisnow.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skisnow.domain.model.Units
import com.skisnow.domain.model.UnitConverter
import com.skisnow.presentation.map.SkiMapView
import com.skisnow.presentation.session.SessionViewModel
import com.skisnow.presentation.settings.SettingsViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun SessionScreen(
    onOpenDetail: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SessionViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.state.collectAsStateWithLifecycle()
    val units = settings.units
    val context = LocalContext.current
    val scroll = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(fine || coarse)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun requestPermissionsOrStart() {
        val needed = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= 33) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        if (!hasLocationPermission()) {
            permissionLauncher.launch(needed.toTypedArray())
        } else {
            viewModel.startIfPermitted(true)
        }
    }

    LaunchedEffect(state.permissionNeeded) {
        if (state.permissionNeeded && !hasLocationPermission()) {
            requestPermissionsOrStart()
        }
    }

    val speedLabel = UnitConverter.speedLabel(units)
    val distLabel = UnitConverter.distanceLabel(units)
    val altLabel = UnitConverter.altitudeLabel(units)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("btn_settings")) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .testTag("ski_map"),
            ) {
                SkiMapView(points = state.points)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.weatherChip?.let { chip ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            ),
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    if (state.weatherError) {
                        Text(
                            text = "Weather unavailable",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when {
                            state.isRecording -> "Recording"
                            state.isPaused -> "Paused"
                            state.isStopping -> "Stopping…"
                            else -> "Ready"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.testTag("session_status"),
                    )
                    Spacer(Modifier.height(8.dp))
                    val distance = UnitConverter.distanceKm(state.liveStats.distanceM, units)
                    val maxSpeed = UnitConverter.speedKmh(state.liveStats.maxSpeedMps, units)
                    val vertical = UnitConverter.altitudeM(state.liveStats.verticalDropM, units)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatCell("%.2f %s".format(distance, distLabel), "Distance")
                        StatCell("%.1f %s".format(maxSpeed, speedLabel), "Max")
                        StatCell("%.0f %s".format(vertical, altLabel), "Vert")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        when {
                            state.isRecording -> {
                                OutlinedButton(
                                    onClick = viewModel::pause,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("btn_pause"),
                                    enabled = !state.isBusy,
                                ) { Text("Pause") }
                                Button(
                                    onClick = viewModel::stop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("btn_stop"),
                                    enabled = !state.isBusy,
                                ) { Text("Stop") }
                            }
                            state.isPaused -> {
                                Button(
                                    onClick = {
                                        if (hasLocationPermission()) viewModel.resume(true)
                                        else requestPermissionsOrStart()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("btn_resume"),
                                    enabled = !state.isBusy,
                                ) { Text("Resume") }
                                OutlinedButton(
                                    onClick = viewModel::stop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("btn_stop"),
                                    enabled = !state.isBusy,
                                ) { Text("Stop") }
                            }
                            state.isStopping -> {
                                Button(
                                    onClick = viewModel::forceReset,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("btn_reset"),
                                    enabled = !state.isBusy,
                                ) { Text("Reset") }
                            }
                            else -> {
                                Button(
                                    onClick = { requestPermissionsOrStart() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 52.dp)
                                        .testTag("btn_start")
                                        .semantics { contentDescription = "Start session" },
                                    enabled = !state.isBusy,
                                ) {
                                    Text(if (state.isBusy) "Starting…" else "Start")
                                }
                            }
                        }
                    }
                    if (state.hasActive || state.isStopping) {
                        TextButton(
                            onClick = viewModel::forceReset,
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("btn_force_reset"),
                        ) { Text("Force reset") }
                    }
                    state.errorMessage?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("error_message"),
                        )
                    }
                }
            }

            Text(
                text = "History",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("history_list"),
            ) {
                items(state.history, key = { it.id.value }) { day ->
                    val fmt = DateTimeFormatter.ofPattern("MMM d HH:mm")
                        .withZone(ZoneId.systemDefault())
                    Text(
                        text = "${fmt.format(day.startedAt)} · ${day.status.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDetail(day.id.value) }
                            .padding(vertical = 8.dp)
                            .testTag("history_row_${day.id.value}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}