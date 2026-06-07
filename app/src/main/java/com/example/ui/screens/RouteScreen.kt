package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.ui.components.ContextualMapContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldLive
import com.example.ui.util.LocalViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    navController: NavController,
    zoneId: String,
    viewModel: MapViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val zones by viewModel.zones.collectAsState()
    val zone = zones.find { it.id == zoneId } ?: MockData.zones.find { it.id == zoneId } ?: return
    var showFeedbackModal by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }

    val probPercent = (zone.probability * 100).toInt()
    val probColor = when {
        zone.probability > 0.6 -> com.example.ui.theme.ProbabilityHigh
        zone.probability > 0.3 -> com.example.ui.theme.ProbabilityMedium
        else -> com.example.ui.theme.ProbabilityLow
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ContextualMapContainer(
            modifier = Modifier.fillMaxSize(),
            zones = listOf(zone),
            privateParkings = emptyList(),
            isDriving = true,
            trafficVisible = true,
            userLocation = Pair(zone.latitude, zone.longitude),
            isFullScreen = isFullScreen,
            onFullScreenToggle = { isFullScreen = !isFullScreen }
        )

        if (!isFullScreen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                TopAppBar(
                title = { Text("Route to Parking", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(36.dp)) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, 
                    titleContentColor = BrandCyan, 
                    navigationIconContentColor = BrandCyan
                )
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Column {
                    Text(
                        text = "TARGET ZONE", 
                        style = MaterialTheme.typography.labelLarge, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = zone.name, 
                        style = MaterialTheme.typography.displayMedium, 
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = probColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(32.dp),
                    border = androidx.compose.foundation.BorderStroke(4.dp, probColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$probPercent%", 
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp), 
                            color = probColor,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "AVAILABILITY CHANCE", 
                            style = MaterialTheme.typography.titleLarge, 
                            color = probColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Route Comparison Card
                val directEta = zone.expectedTimeToParkMinutes
                val smartEta = directEta + 2
                val directProb = probPercent
                val smartProb = (zone.probability * 1.5).coerceAtMost(0.95) * 100

                var useSmartRoute by remember { mutableStateOf(true) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("ROUTE COMPARISON", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Direct route option
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (!useSmartRoute) BrandCyan.copy(alpha = 0.08f) else Color.Transparent,
                            border = BorderStroke(if (!useSmartRoute) 1.5.dp else 1.dp, if (!useSmartRoute) BrandCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            onClick = { useSmartRoute = false }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Route, contentDescription = null, tint = if (!useSmartRoute) BrandCyan else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Direct Route", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("${directEta}m drive • ${directProb}% chance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (!useSmartRoute) {
                                    Surface(color = BrandCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                        Text("SELECTED", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = BrandCyan)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Smart route option
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (useSmartRoute) EmeraldLive.copy(alpha = 0.08f) else Color.Transparent,
                            border = BorderStroke(if (useSmartRoute) 1.5.dp else 1.dp, if (useSmartRoute) EmeraldLive.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            onClick = { useSmartRoute = true }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = if (useSmartRoute) EmeraldLive else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Smart Route", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldLive)
                                    Text("+${smartEta - directEta}m drive • ${smartProb.toInt()}% chance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (useSmartRoute) {
                                    Surface(color = EmeraldLive.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                        Text("RECOMMENDED", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = EmeraldLive)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Smart route passes through high-probability streets for better parking odds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricHudBox("ETA", "${if (useSmartRoute) smartEta else directEta}m", Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    MetricHudBox("WALK", "${zone.walkingTimeToDestinationMinutes}m", Modifier.weight(1f))
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { navController.navigate("turn_by_turn/${zone.id}") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandCyan, 
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("START NAVIGATION", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }

                    // Driving mode button
                    OutlinedButton(
                        onClick = { navController.navigate("driving/${zone.id}") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, EmeraldLive.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DriveEta, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("DRIVING MODE — SAFETY UI", style = MaterialTheme.typography.titleSmall, color = EmeraldLive, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate("private") },
                            modifier = Modifier.weight(1f).height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, BrandCyan.copy(alpha = 0.5f))
                        ) {
                            Text("PRIVATE GARAGE", style = MaterialTheme.typography.titleSmall, color = BrandCyan, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showFeedbackModal = true },
                            modifier = Modifier.weight(1f).height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Text("REPORT SPOT", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (showFeedbackModal) {
                AlertDialog(
                    onDismissRequest = { showFeedbackModal = false },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    title = { Text("Report Outcome", style = MaterialTheme.typography.headlineSmall) },
                    text = { Text("Help calibrate the model by reporting what you found.", style = MaterialTheme.typography.bodyLarge) },
                    confirmButton = {
                        Button(
                            onClick = { showFeedbackModal = false }, 
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, contentColor = MaterialTheme.colorScheme.onSecondary),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).height(64.dp)
                        ) { 
                            Text("I FOUND A SPOT", style = MaterialTheme.typography.titleLarge) 
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showFeedbackModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger.copy(alpha = 0.9f), contentColor = MaterialTheme.colorScheme.onError),
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        ) { 
                            Text("NO SPOTS AVAILABLE", style = MaterialTheme.typography.titleLarge) 
                        }
                    }
                )
            }
        }
        }
    }
}

@Composable
fun MetricHudBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}
