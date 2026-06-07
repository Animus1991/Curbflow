package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.*
import com.example.domain.PredictiveAnalyticsEngine
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MunicipalScreen(navController: NavController) {
    var isFullScreenMap by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CITY INSIGHTS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val mapWeight by animateFloatAsState(
                targetValue = if (isFullScreenMap) 1f else 0.3f,
                label = "mapWeight"
            )

            ContextualMapContainer(
                modifier = Modifier.fillMaxWidth().weight(mapWeight),
                zones = MockData.zones,
                isFullScreen = isFullScreenMap,
                onFullScreenToggle = { isFullScreenMap = !isFullScreenMap }
            )

            if (!isFullScreenMap) {
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.weight(0.7f)
                ) {
                    item {
                        Column {
                            Text("DISTRICT PERFORMANCE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("How smarter parking reduces traffic and emissions across each district.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    item {
                        GreenImpactCard(onViewDetails = { navController.navigate("co2") })
                    }

                    items(MockData.municipalData, key = { it.area }) { data ->
                        PerformanceCard(data)
                    }
                }
            }
        }
    }
}

@Composable
fun GreenImpactCard(onViewDetails: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onViewDetails() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLive.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(EmeraldLive.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text("LESS TIME CIRCLING FOR PARKING", style = MaterialTheme.typography.labelSmall, color = EmeraldLive, fontWeight = FontWeight.Black)
                Text("2.4kg CO₂ SAVED TODAY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text("Tap to see full impact dashboard →", style = MaterialTheme.typography.bodySmall, color = EmeraldLive)
            }
        }
    }
}

@Composable
fun PerformanceCard(data: MunicipalAreaAnalytics) {
    val efficiency = remember(data) { PredictiveAnalyticsEngine.calculateAreaEfficiency(data) }
    val peakHour = remember(data) { PredictiveAnalyticsEngine.predictPeakHour(data.area, data.occupancyRate) }
    val enforcementColor = when (data.enforcementPriority.lowercase()) {
        "critical" -> CrimsonDanger
        "high" -> AmberWarning
        "medium" -> NeonCyan
        else -> EmeraldLive
    }
    
    CDLCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(data.area, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("DISTRICT PERFORMANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CircularEfficiencyMeter(efficiency)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Enforcement badge + Peak hour
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = enforcementColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "${data.enforcementPriority.uppercase()} ENFORCEMENT",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = enforcementColor
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "PEAK IN ${peakHour}H",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("DEMAND FORECAST (2H WINDOW)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        PredictiveProbabilityChart(
            historicalData = listOf(0.4, 0.5, 0.55),
            forecastData = listOf(0.6, 0.65, 0.5)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            KpiGlance("STREETS FULL", "${(data.occupancyRate * 100).toInt()}%", Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
            KpiGlance("AVG TIME TO PARK", "${data.avgTimeToParkMinutes}m", Icons.Default.Speed, Modifier.weight(1f))
        }
    }
}

@Composable
fun CircularEfficiencyMeter(score: Int) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(64.dp),
            color = if (score > 70) EmeraldLive else NeonCyan,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text("$score%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
    }
}

@Composable
fun KpiGlance(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
