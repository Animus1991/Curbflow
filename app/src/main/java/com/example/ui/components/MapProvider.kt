package com.example.ui.components

import androidx.compose.runtime.Composable
import com.example.data.ParkingZone
import com.example.data.PrivateParking

interface MapProvider {
    @Composable
    fun RenderMap(
        zones: List<ParkingZone>,
        privateParkings: List<PrivateParking>,
        isDriving: Boolean,
        showHeatmap: Boolean = true,
        heatmapType: HeatmapType = HeatmapType.PROBABILITY,
        trafficVisible: Boolean = false,
        dynamicSpots: List<com.example.api.DynamicSpot> = emptyList(),
        spotMarkersVisible: Boolean = true,
        restrictedZonesVisible: Boolean = false,
        forecastVisible: Boolean = false,
        weatherVisible: Boolean = false,
        userLocation: Pair<Double, Double>? = null,
        mapCenter: Pair<Double, Double>? = null,
        onZoneSelected: (ParkingZone) -> Unit
    )
}
