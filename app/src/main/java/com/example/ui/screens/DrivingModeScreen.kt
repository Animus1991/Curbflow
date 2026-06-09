package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory
import com.example.util.VoiceGuidanceEngine
import kotlinx.coroutines.delay

/**
 * DrivingModeScreen — Safety-first navigation UI.
 * 
 * Activated when user is driving (speed > 10 km/h simulated).
 * Features:
 * - Large, high-contrast elements for minimal distraction
 * - Voice guidance simulation
 * - Auto-lock for fine interactions
 * - Giant "Navigate to Best Spot" button (48dp+ touch target)
 * - Real-time probability update
 * - Exit button when stopped
 */
@Composable
fun DrivingModeScreen(
    navController: NavController,
    zoneId: String,
    viewModel: MapViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val zones by viewModel.zones.collectAsState()
    val zone = zones.find { it.id == zoneId } ?: MockData.zones.find { it.id == zoneId } ?: return
    val probPercent = (zone.probability * 100).toInt()

    // Simulated speed state
    var simulatedSpeed by remember { mutableIntStateOf(35) }
    var etaSeconds by remember { mutableIntStateOf(zone.expectedTimeToParkMinutes * 60) }
    var voiceMessage by remember { mutableStateOf("Navigating to ${zone.name}") }
    var showVoiceOverlay by remember { mutableStateOf(true) }

    // Real Text-to-Speech engine — speaks every guidance message aloud
    val context = LocalContext.current
    val voiceEngine = remember { VoiceGuidanceEngine(context) }
    DisposableEffect(Unit) {
        onDispose { voiceEngine.shutdown() }
    }
    LaunchedEffect(voiceMessage) {
        voiceEngine.speak(voiceMessage)
    }

    // Countdown ETA
    LaunchedEffect(Unit) {
        while (etaSeconds > 0) {
            delay(1000)
            etaSeconds--
            // Simulate speed variation
            simulatedSpeed = (25..50).random()
        }
        voiceMessage = "You have arrived. Look for parking on your right."
    }

    // Voice guidance events
    LaunchedEffect(etaSeconds) {
        when {
            etaSeconds == zone.expectedTimeToParkMinutes * 60 - 5 -> {
                voiceMessage = "High probability zone ahead in ${zone.expectedTimeToParkMinutes} minutes"
                showVoiceOverlay = true
            }
            etaSeconds == 60 -> {
                voiceMessage = "Arriving in 1 minute. Prepare to slow down."
                showVoiceOverlay = true
            }
            etaSeconds == 30 -> {
                voiceMessage = "Almost there. Spot likely on your right."
                showVoiceOverlay = true
            }
            etaSeconds == 0 -> {
                voiceMessage = "You have arrived. Look for parking now."
                showVoiceOverlay = true
            }
        }
    }

    // Auto-hide voice overlay
    LaunchedEffect(showVoiceOverlay) {
        if (showVoiceOverlay) {
            delay(4000)
            showVoiceOverlay = false
        }
    }

    // Pulsing animation for the probability indicator
    val infiniteTransition = rememberInfiniteTransition(label = "driving_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulse"
    )

    val probColor = when {
        zone.probability > 0.7 -> EmeraldLive
        zone.probability > 0.4 -> AmberWarning
        else -> CrimsonDanger
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF161B22),
                        Color(0xFF0D1117)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Safety header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed indicator
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${simulatedSpeed} km/h", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Driving mode badge
                Surface(
                    color = EmeraldLive.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(EmeraldLive, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DRIVING MODE", color = EmeraldLive, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Center: Giant probability display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "PARKING CHANCE",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$probPercent%",
                    modifier = Modifier.scale(pulseScale),
                    color = probColor,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    zone.name,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // ETA countdown
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ARRIVING IN", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                val min = etaSeconds / 60
                val sec = etaSeconds % 60
                Text(
                    if (etaSeconds > 0) String.format("%d:%02d", min, sec) else "ARRIVED",
                    color = if (etaSeconds < 30) EmeraldLive else Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Bottom: Action buttons (large for driving safety)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Navigate button
                Button(
                    onClick = {
                        navController.navigate("turn_by_turn/$zoneId") {
                            popUpTo("driving/$zoneId") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("NAVIGATE TO SPOT", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }

                // Exit driving mode
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXIT DRIVING MODE", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Voice guidance overlay
        if (showVoiceOverlay) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .padding(horizontal = 32.dp),
                color = NeonCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Voice guidance", tint = NeonCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        voiceMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // UI Lock indicator
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            color = Color.Transparent
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "UI simplified for driving safety",
                    color = Color.White.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
