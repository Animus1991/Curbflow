package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.ParkingZone
import com.example.data.PrivateParking
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

object UrbanMapView : MapProvider {
    @Composable
    override fun RenderMap(
        zones: List<ParkingZone>,
        privateParkings: List<PrivateParking>,
        isDriving: Boolean,
        showHeatmap: Boolean,
        heatmapType: HeatmapType,
        trafficVisible: Boolean,
        dynamicSpots: List<com.example.api.DynamicSpot>,
        spotMarkersVisible: Boolean,
        restrictedZonesVisible: Boolean,
        forecastVisible: Boolean,
        weatherVisible: Boolean,
        userLocation: Pair<Double, Double>?,
        mapCenter: Pair<Double, Double>?,
        onZoneSelected: (ParkingZone) -> Unit
    ) {
        val athens = LatLng(37.9715, 23.7267)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(athens, if (isDriving) 15f else 13f)
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            zones.forEach { zone ->
                val markerState = rememberMarkerState(position = LatLng(zone.latitude, zone.longitude))
                Marker(
                    state = markerState,
                    title = zone.name,
                    snippet = "Probability: ${(zone.probability * 100).toInt()}%",
                    onClick = {
                        onZoneSelected(zone)
                        false
                    }
                )
            }
            privateParkings.forEach { p ->
                val hash = kotlin.math.abs(p.name.hashCode())
                val lon = 23.72f + (hash % 60) * 0.001f
                val lat = 37.95f + ((hash ushr 8) % 45) * 0.001f
                val markerState = rememberMarkerState(position = LatLng(lat.toDouble(), lon.toDouble()))
                Marker(
                    state = markerState,
                    title = p.name,
                    snippet = "Private Fallback"
                )
            }
        }
    }
}
