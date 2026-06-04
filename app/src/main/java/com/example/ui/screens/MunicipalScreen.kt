package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.ui.components.ContextualMapContainer
import com.example.ui.components.MunicipalKpiCard
import com.example.ui.theme.StrongText
import com.example.ui.theme.MutedText

@Composable
fun MunicipalScreen(navController: NavController) {
    var isFullScreenMap by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Map Section
            val mapWeight by animateFloatAsState(
                targetValue = if (isFullScreenMap) 1f else 0.4f,
                label = "mapWeight"
            )

            ContextualMapContainer(
                modifier = Modifier.fillMaxWidth().weight(mapWeight),
                zones = MockData.zones,
                privateParkings = emptyList(),
                isDriving = false,
                showHeatmap = true,
                trafficVisible = true,
                restrictedZonesVisible = true,
                isFullScreen = isFullScreenMap,
                onFullScreenToggle = { isFullScreenMap = !isFullScreenMap }
            )

            // List Section
            if (!isFullScreenMap) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(0.6f)
                ) {
                    item {
                        Text(
                            "City Intelligence",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Understand curb pressure, illegal parking risk, and cruising patterns locally.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(MockData.municipalData, key = { it.area }) { data ->
                        Text(data.area, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MunicipalKpiCard(
                                title = "Occupancy",
                                value = "${(data.occupancyRate * 100).toInt()}%",
                                trendInfo = "Peak time",
                                modifier = Modifier.weight(1f)
                            )
                            MunicipalKpiCard(
                                title = "Illegal Risk",
                                value = "${(data.illegalParkingRisk * 100).toInt()}%",
                                trendInfo = "Monitor",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MunicipalKpiCard(
                                title = "Cruising",
                                value = "${(data.cruisingPressure * 100).toInt()}%",
                                trendInfo = "${data.avgTimeToParkMinutes}m avg to park",
                                modifier = Modifier.weight(1f)
                            )
                            MunicipalKpiCard(
                                title = "CO2 Impact",
                                value = "${data.estimatedCO2ImpactKg}kg",
                                trendInfo = "Daily est.",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
