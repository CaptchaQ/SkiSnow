package com.skisnow.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skisnow.presentation.map.SkiMapView
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.day == null && !state.notFound -> Text("Loading…")
                state.notFound -> Text("Session not found")
                else -> DetailContent(state)
            }
        }
    }
}

@Composable
private fun DetailContent(state: SessionDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        state.day?.let { day ->
            val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .withZone(ZoneId.systemDefault())
            Text(fmt.format(day.startedAt), style = MaterialTheme.typography.titleLarge)
            Text(
                day.status.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            if (day.endedAt != null) {
                Text(
                    Duration.between(day.startedAt, day.endedAt).toMinutes().toString() + " min",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        StatsRow(state)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(4.dp),
            ) {
                SkiMapView(points = state.points)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Elevation profile", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        ElevationChart(profile = state.elevationProfile)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatsRow(state: SessionDetailUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StatCell("Distance", "%.2f km".format(state.distanceM / 1000.0))
        StatCell("Max", "%.1f km/h".format(state.maxSpeedMps * 3.6))
        StatCell("Vert", "%.0f m".format(state.verticalDropM))
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ElevationChart(profile: List<Pair<Double, Double>>) {
    if (profile.isEmpty()) {
        Text("No altitude data", color = MaterialTheme.colorScheme.outline)
        return
    }
    val maxDist = profile.maxOf { it.first }.coerceAtLeast(1.0)
    val minAlt = profile.minOf { it.second }
    val maxAlt = profile.maxOf { it.second }
    val altRange = (maxAlt - minAlt).coerceAtLeast(1.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(top = 4.dp),
    ) {
        val w = size.width
        val h = size.height
        val path = Path()
        profile.forEachIndexed { i, (distM, altM) ->
            val x = ((distM / maxDist) * w).toFloat()
            val y = (h - ((altM - minAlt) / altRange) * h).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = Color(0xFF00B4D8),
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}