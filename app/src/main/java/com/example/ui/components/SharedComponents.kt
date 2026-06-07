package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FleetContributor
import com.example.data.ParkingZone
import com.example.data.PrivateParking
import com.example.ui.theme.*
import androidx.compose.foundation.BorderStroke

// --- Curbflow Design Language (CDL) Architectural Foundation ---

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(12.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) // Glass-morphic baseline
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
    ) {
        content()
    }
}

@Composable
fun ContextualMapContainer(
    modifier: Modifier = Modifier,
    zones: List<ParkingZone> = emptyList(),
    privateParkings: List<PrivateParking> = emptyList(),
    isDriving: Boolean = false,
    showHeatmap: Boolean = true,
    trafficVisible: Boolean = true,
    restrictedZonesVisible: Boolean = true,
    userLocation: Pair<Double, Double> = 37.9715 to 23.7267,
    isFullScreen: Boolean = false,
    onFullScreenToggle: (() -> Unit)? = null
) {
    Box(modifier = modifier
        .clip(RoundedCornerShape(if (isFullScreen) 0.dp else 32.dp))
        .border(if (isFullScreen) 0.dp else 1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp))
    ) {
        OsmdroidMapView.RenderMap(
            zones = zones,
            privateParkings = privateParkings,
            isDriving = isDriving,
            showHeatmap = showHeatmap,
            heatmapType = HeatmapType.PROBABILITY,
            trafficVisible = trafficVisible,
            dynamicSpots = emptyList(),
            spotMarkersVisible = true,
            restrictedZonesVisible = restrictedZonesVisible,
            forecastVisible = false,
            weatherVisible = false,
            userLocation = userLocation,
            mapCenter = userLocation,
            onZoneSelected = {}
        )

        if (onFullScreenToggle != null) {
            Surface(
                onClick = onFullScreenToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
            ) {
                Icon(
                    if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Toggle Full Screen",
                    modifier = Modifier.padding(14.dp),
                    tint = NeonCyan
                )
            }
        }
    }
}

@Composable
fun CurbFlowAppShell(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = { CurbFlowBottomNavigation(currentRoute, onNavigate) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun CurbFlowBottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    // Floating Glass Navigation Island
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(84.dp)
    ) {
        GlassSurface(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = listOf(
                    Triple("map", "Map", Icons.Default.Explore),
                    Triple("fleet", "Fleet", Icons.Default.Hub),
                    Triple("city", "City", Icons.Default.StackedLineChart),
                    Triple("private", "Garages", Icons.Default.LocalParking),
                    Triple("profile", "Profile", Icons.Default.Person)
                )
                items.forEach { (route, label, icon) ->
                    val isSelected = currentRoute?.startsWith(route) == true
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(route) }
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                // Glow Affordance behind active icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                                        .blur(10.dp)
                                )
                            }
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(if (isSelected) 30.dp else 24.dp)
                                    .graphicsLayer {
                                        scaleX = if (isSelected) 1.1f else 1f
                                        scaleY = if (isSelected) 1.1f else 1f
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProbabilityPulseIndicator(probability: Double, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "alpha"
    )
    
    val color = when {
        probability > 0.7 -> EmeraldLive
        probability > 0.4 -> AmberWarning
        else -> CrimsonDanger
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(72.dp)) {
        // High-Fidelity Glow
        Box(
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer { 
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha
                }
                .background(color, CircleShape)
        )
        // Solid Core with Affinity border
        Surface(
            modifier = Modifier.size(20.dp),
            shape = CircleShape,
            color = color,
            shadowElevation = 10.dp,
            border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        ) {}
    }
}

@Composable
fun CDLCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

@Composable
fun ProbabilityBadge(probability: Double) {
    val (label, color) = when {
        probability > 0.7 -> "LIKELY OPEN" to EmeraldLive
        probability > 0.4 -> "LIMITED" to AmberWarning
        else -> "UNLIKELY" to CrimsonDanger
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Status Indicator in badge
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
                    .shadow(4.dp, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text.uppercase(), 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun ConfidenceMeter(confidence: Double) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PREDICTION ACCURACY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { confidence.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = NeonCyan,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ZoneCard(
    name: String,
    area: String,
    probability: Double,
    modifier: Modifier = Modifier
) {
    CDLCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(area.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ProbabilityBadge(probability)
        }
    }
}

@Composable
fun MunicipalKpiCard(title: String, value: String, narrative: String, modifier: Modifier = Modifier) {
    CDLCard(modifier = modifier) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = NeonCyan)
        Spacer(modifier = Modifier.height(8.dp))
        Text(narrative, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PrivacyRow(icon: ImageVector, title: String, description: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
