package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandCyan

@Composable
fun MapControlsOverlay(
    timeSliderValue: Float,
    onTimeChange: (Float) -> Unit,
    selectedDay: Int,
    onDayChange: (Int) -> Unit,
    heatmapVisible: Boolean,
    onHeatmapToggle: (Boolean) -> Unit,
    heatmapType: HeatmapType,
    onHeatmapTypeChange: (HeatmapType) -> Unit,
    trafficVisible: Boolean,
    onTrafficToggle: (Boolean) -> Unit,
    probabilityThreshold: Float,
    onThresholdChange: (Float) -> Unit,
    spotMarkersVisible: Boolean,
    onSpotMarkersToggle: (Boolean) -> Unit,
    restrictedZonesVisible: Boolean,
    onRestrictedZonesToggle: (Boolean) -> Unit,
    forecastVisible: Boolean,
    onForecastToggle: (Boolean) -> Unit,
    weatherVisible: Boolean,
    onWeatherToggle: (Boolean) -> Unit,
    onFindLocation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Find Location & Heatmap Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Heatmap Layer", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = heatmapVisible,
                    onCheckedChange = onHeatmapToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan, checkedTrackColor = BrandCyan.copy(alpha = 0.5f))
                )
                IconButton(onClick = onFindLocation) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Find My Location", tint = BrandCyan)
                }
            }
            
            // Heatmap Type Selection & Traffic
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Type: ", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = heatmapType == HeatmapType.PROBABILITY,
                        onClick = { onHeatmapTypeChange(HeatmapType.PROBABILITY) },
                        label = { Text("Availability", style = MaterialTheme.typography.labelSmall) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = heatmapType == HeatmapType.PRICE,
                        onClick = { onHeatmapTypeChange(HeatmapType.PRICE) },
                        label = { Text("Price", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Traffic", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = trafficVisible,
                        onCheckedChange = onTrafficToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan, checkedTrackColor = BrandCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.scale(0.8f) // Make it smaller
                    )
                }
            }

            // Additional Layers
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = spotMarkersVisible,
                        onClick = { onSpotMarkersToggle(!spotMarkersVisible) },
                        label = { Text("Individual Spots", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                item {
                    FilterChip(
                        selected = restrictedZonesVisible,
                        onClick = { onRestrictedZonesToggle(!restrictedZonesVisible) },
                        label = { Text("Restricted", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                item {
                    FilterChip(
                        selected = forecastVisible,
                        onClick = { onForecastToggle(!forecastVisible) },
                        label = { Text("Forecast (4h)", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                item {
                    FilterChip(
                        selected = weatherVisible,
                        onClick = { onWeatherToggle(!weatherVisible) },
                        label = { Text("Weather", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day of week selector
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days.size) { index ->
                    FilterChip(
                        selected = selectedDay == index,
                        onClick = { onDayChange(index) },
                        label = { Text(days[index], style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Time of Day: ${timeSliderValue.toInt()}:00", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = timeSliderValue,
                onValueChange = onTimeChange,
                valueRange = 0f..24f,
                steps = 23,
                colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Probability Filter: >${(probabilityThreshold * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = probabilityThreshold,
                onValueChange = onThresholdChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan),
                modifier = Modifier.height(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = BrandCyan.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Data Freshness Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Data Freshness", style = MaterialTheme.typography.labelMedium, color = BrandCyan)
                    Text("Satellite: Just now | Fleet: 1m ago", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                androidx.compose.material3.Surface(
                    color = com.example.ui.theme.ProbabilityHigh.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "98% CONFIDENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.example.ui.theme.ProbabilityHigh,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
