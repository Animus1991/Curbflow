package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.ui.components.ContextualMapContainer
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(navController: NavController, zoneId: String) {
    val zone = MockData.zones.find { it.id == zoneId } ?: return
    var showFeedbackModal by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }

    val probPercent = (zone.probability * 100).toInt()
    val probColor = when {
        zone.probability > 0.6 -> com.example.ui.theme.ProbabilityHigh
        zone.probability > 0.3 -> com.example.ui.theme.ProbabilityMedium
        else -> com.example.ui.theme.ProbabilityLow
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Immersive Map background using reusable container
        ContextualMapContainer(
            modifier = Modifier.fillMaxSize(),
            zones = listOf(zone),
            privateParkings = emptyList(),
            isDriving = true,
            trafficVisible = true,
            mapCenter = Pair(zone.latitude, zone.longitude),
            isFullScreen = isFullScreen,
            onFullScreenToggle = { isFullScreen = !isFullScreen }
        )

        if (!isFullScreen) {
            // Overlay with HUD UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                TopAppBar(
                title = { Text("HUD Mode", style = MaterialTheme.typography.titleLarge) },
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
                // Zone Name & Target
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

                // Giant Probability Target
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

                // Actionable metrics (very large)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricHudBox("ETA", "${zone.expectedTimeToParkMinutes}m", Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    MetricHudBox("WALK", "${zone.walkingTimeToDestinationMinutes}m", Modifier.weight(1f))
                }

                // Fallback & Feedback actions (Massive buttons)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { navController.navigate("private") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant, 
                            contentColor = BrandCyan
                        )
                    ) {
                        Text("PRIVATE FALLBACK", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showFeedbackModal = true },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandCyan, 
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("REPORT PARKING", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                            Text("FOUND SPOT", style = MaterialTheme.typography.titleLarge) 
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showFeedbackModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f), contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        ) { 
                            Text("NO SPOTS", style = MaterialTheme.typography.titleLarge) 
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
