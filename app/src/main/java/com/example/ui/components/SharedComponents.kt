package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import com.example.ui.theme.*
import com.example.ui.theme.LocalThemeManager

// --- Map Containers --- //

@Composable
fun ContextualMapContainer(
    modifier: Modifier = Modifier,
    zones: List<com.example.data.ParkingZone> = emptyList(),
    privateParkings: List<com.example.data.PrivateParking> = emptyList(),
    isDriving: Boolean = false,
    showHeatmap: Boolean = false,
    trafficVisible: Boolean = false,
    restrictedZonesVisible: Boolean = false,
    mapCenter: Pair<Double, Double> = Pair(37.9715, 23.7267),
    isFullScreen: Boolean = false,
    onFullScreenToggle: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        com.example.ui.components.OsmdroidMapView.RenderMap(
            zones = zones,
            privateParkings = privateParkings,
            isDriving = isDriving,
            showHeatmap = showHeatmap,
            heatmapType = com.example.ui.components.HeatmapType.PROBABILITY,
            trafficVisible = trafficVisible,
            dynamicSpots = emptyList(),
            spotMarkersVisible = true,
            restrictedZonesVisible = restrictedZonesVisible,
            forecastVisible = false,
            weatherVisible = false,
            userLocation = mapCenter,
            mapCenter = mapCenter,
            onZoneSelected = {}
        )

        // Overlay full-screen toggle
        if (onFullScreenToggle != null) {
            IconButton(
                onClick = onFullScreenToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (isFullScreen) Icons.Default.Close else Icons.Default.Fullscreen,
                    contentDescription = "Toggle Full Screen Map",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MiniMapToggleFab(
    isMapVisible: Boolean,
    onToggle: () -> Unit
) {
    FloatingActionButton(
        onClick = onToggle,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            if (isMapVisible) Icons.Default.Close else Icons.Default.Map,
            contentDescription = "Toggle Mini-Map"
        )
    }
}

// --- App Shell --- //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurbFlowAppShell(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val showBottomNav = currentRoute in listOf("map", "route", "private", "fleet", "city", "privacy")
    
    Scaffold(
        topBar = {
            if (showBottomNav) {
                TopAppBar(
                    title = {
                        Column {
                            Text("CurbFlow AI", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Parking probability & urban freshness", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        val themeManager = LocalThemeManager.current
                        IconButton(onClick = { themeManager.toggleTheme() }) {
                            Icon(
                                imageVector = if (themeManager.isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                CurbFlowBottomNavigation(currentRoute, onNavigate)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun CurbFlowBottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val items = listOf(
            Triple("map", "Map", Icons.Default.Map),
            Triple("route", "Route", Icons.Default.Directions),
            Triple("private", "Private", Icons.Default.LocalParking),
            Triple("fleet", "Fleet", Icons.Default.DirectionsCar),
            Triple("city", "City", Icons.Default.Assessment),
            Triple("privacy", "Privacy", Icons.Default.Security)
        )
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                selected = currentRoute?.startsWith(route) == true, // Handle "route/{zoneId}"
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// --- Cards & Components --- //

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProbabilityBadge(probability: Double) {
    val probPercent = (probability * 100).toInt()
    val (color, text) = when {
        probability > 0.6 -> ProbabilityHigh to "High probability"
        probability > 0.3 -> ProbabilityMedium to "Medium probability"
        probability >= 0.0 -> ProbabilityLow to "Low probability"
        else -> ProbabilityUnknown to "Insufficient data"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FreshnessBadge(freshnessMinutes: Int) {
    val text = when {
        freshnessMinutes <= 1 -> "Fresh signal: 1 min ago"
        freshnessMinutes <= 10 -> "Updated $freshnessMinutes min ago"
        else -> "Stale signal"
    }
    val color = if (freshnessMinutes <= 5) MaterialTheme.colorScheme.secondary else WarningOrange

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Default.AccessTime, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun LegalRiskBadge(riskLevel: String) {
    val (color, icon) = when (riskLevel.lowercase()) {
        "low" -> MaterialTheme.colorScheme.secondary to Icons.Default.CheckCircle
        "medium" -> WarningOrange to Icons.Default.Warning
        "high", "restricted" -> MaterialTheme.colorScheme.error to Icons.Default.Error
        else -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Default.Help
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Legal risk: $riskLevel", color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ConfidenceMeter(confidence: Double) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Confidence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { confidence.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = AccentCyan,
            trackColor = SurfaceElevated
        )
    }
}

@Composable
fun ZoneCard(
    zoneName: String,
    area: String,
    probability: Double,
    confidence: Double,
    freshnessMinutes: Int,
    expectedTimeToPark: String,
    walkingTime: String,
    legalRisk: String,
    recommendationScore: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(zoneName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(area, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(recommendationScore, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProbabilityBadge(probability)
                LegalRiskBadge(legalRisk)
            }
            Spacer(modifier = Modifier.height(8.dp))
            FreshnessBadge(freshnessMinutes)
            Spacer(modifier = Modifier.height(16.dp))
            ConfidenceMeter(confidence)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ETA to Park", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(expectedTimeToPark, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Walking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(walkingTime, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun PrivateParkingCard(
    name: String,
    price: String,
    slots: Int,
    distance: String,
    walkingTime: String,
    rating: Double,
    onReserve: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalParking, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$slots slots left", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(rating.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("$distance away", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("~$walkingTime walk", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onReserve,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reserve", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun FleetDeviceCard(
    vehicleType: String,
    status: String,
    coverageScore: Double,
    dataQuality: String,
    reward: String,
    privacyMode: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(vehicleType, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(status, style = MaterialTheme.typography.bodyMedium, color = if (status.equals("Online", ignoreCase=true)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(reward, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text("Coverage Score: ${(coverageScore * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                LinearProgressIndicator(
                    progress = { coverageScore.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 4.dp).clip(RoundedCornerShape(2.dp)),
                    color = AccentCyan,
                    trackColor = SurfaceElevated
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Quality: $dataQuality", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Privacy: $privacyMode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MunicipalKpiCard(
    title: String,
    value: String,
    trendInfo: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(trendInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SafetyBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = "Safety Warning", tint = WarningOrange)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Drive safely. CurbFlow does not require interaction while the vehicle is moving. Use voice guidance and follow local traffic laws.",
                style = MaterialTheme.typography.bodySmall,
                color = WarningOrange
            )
        }
    }
}

@Composable
fun LegalDisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = "Legal Info", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "CurbFlow AI provides probability-based parking guidance. It does not guarantee, reserve, sell, auction, or privatize public street parking.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PrivacyByDesignCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = "Privacy", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Privacy By Design", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(16.dp))
            PrivacyRow(Icons.Default.VisibilityOff, "Raw video upload", "OFF", MaterialTheme.colorScheme.error)
            PrivacyRow(Icons.Default.Memory, "Edge detection", "ON", MaterialTheme.colorScheme.secondary)
            PrivacyRow(Icons.Default.CloudUpload, "Metadata-only transmission", "Always", MaterialTheme.colorScheme.primary)
            PrivacyRow(Icons.Default.PublicOff, "Public spot ownership", "None", MaterialTheme.colorScheme.onSurfaceVariant)
            PrivacyRow(Icons.Default.NoPhotography, "Face/License plate storage", "Zero (Prototype)", MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun PrivacyRow(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
