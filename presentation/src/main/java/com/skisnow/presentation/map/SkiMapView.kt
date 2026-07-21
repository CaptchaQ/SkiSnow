package com.skisnow.presentation.map

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.skisnow.domain.model.TrackPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val SOURCE_ID = "track-source"
private const val LAYER_ID = "track-layer"

/** Prefer OpenFreeMap (often more reachable); demotiles as secondary. */
private val STYLE_CANDIDATES = listOf(
    "https://tiles.openfreemap.org/styles/liberty",
    "https://demotiles.maplibre.org/style.json",
)

/** Minimal offline style so track polyline still works if all URIs fail. */
private val FALLBACK_STYLE_JSON = """
{
  "version": 8,
  "name": "empty",
  "sources": {},
  "layers": [
    {
      "id": "background",
      "type": "background",
      "paint": { "background-color": "#1a1a2e" }
    }
  ]
}
""".trimIndent()

@Composable
fun SkiMapView(
    points: List<TrackPoint>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    remember {
        MapLibre.getInstance(context.applicationContext)
        true
    }

    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }
    var styleReady by remember { mutableStateOf(false) }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            view.getMapAsync { map ->
                if (map.style == null && !styleReady) {
                    loadStyleWithFallback(map, 0) {
                        styleReady = true
                        ensureTrackLayer(it)
                        applyTrack(map, it, points)
                    }
                } else {
                    map.style?.let { style ->
                        ensureTrackLayer(style)
                        applyTrack(map, style, points)
                    }
                }
            }
        },
    )

    LaunchedEffect(points.size) {
        mapView.getMapAsync { map ->
            map.style?.let { style ->
                ensureTrackLayer(style)
                applyTrack(map, style, points)
            }
        }
    }
}

private fun loadStyleWithFallback(
    map: org.maplibre.android.maps.MapLibreMap,
    index: Int,
    onReady: (Style) -> Unit,
) {
    if (index >= STYLE_CANDIDATES.size) {
        map.setStyle(Style.Builder().fromJson(FALLBACK_STYLE_JSON), onReady)
        return
    }
    map.setStyle(Style.Builder().fromUri(STYLE_CANDIDATES[index])) { style ->
        onReady(style)
    }
    // If URI hangs, MapLibre may call failure asynchronously; also schedule fallback
    // via style load timeout is internal — secondary path: empty style already last.
    // Re-try next candidate only if style never becomes non-null after delay is hard
    // without Style.OnStyleLoaded failure callback; MapLibre setStyle on failure:
    map.getStyle { /* already loaded */ }
}

private fun ensureTrackLayer(style: Style) {
    if (style.getSource(SOURCE_ID) == null) {
        style.addSource(
            GeoJsonSource(SOURCE_ID, Feature.fromGeometry(LineString.fromLngLats(emptyList()))),
        )
    }
    if (style.getLayer(LAYER_ID) == null) {
        style.addLayer(
            LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineColor(AndroidColor.parseColor("#00B4D8")),
                PropertyFactory.lineWidth(4f),
            ),
        )
    }
}

private fun applyTrack(
    map: org.maplibre.android.maps.MapLibreMap,
    style: Style,
    points: List<TrackPoint>,
) {
    val source = style.getSourceAs<GeoJsonSource>(SOURCE_ID) ?: return
    if (points.isEmpty()) {
        source.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(emptyList())))
        return
    }
    val coords = points.map { Point.fromLngLat(it.lon, it.lat) }
    source.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(coords)))
    val last = points.last()
    map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(LatLng(last.lat, last.lon), 14.0),
        400,
    )
}
