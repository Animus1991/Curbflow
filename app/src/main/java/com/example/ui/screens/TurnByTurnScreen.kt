package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.FeedbackType
import com.example.data.MockData
import com.example.ui.components.ParkingConfirmationDialog
import com.example.ui.util.LocalViewModelFactory
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldLive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnByTurnScreen(navController: NavController, zoneId: String, viewModel: MapViewModel = viewModel(factory = LocalViewModelFactory.current)) {
    val zones by viewModel.zones.collectAsState()
    val zone = zones.find { it.id == zoneId } ?: MockData.zones.find { it.id == zoneId } ?: return
    val probPercent = (zone.probability * 100).toInt()
    
    var isListening by remember { mutableStateOf(false) }
    var voiceFeedbackMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Arrival countdown timer
    var countdownSeconds by remember { mutableIntStateOf(zone.expectedTimeToParkMinutes * 60) }
    var showArrivalConfirmation by remember { mutableStateOf(false) }
    var arrivalDialogDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds--
        }
        if (!arrivalDialogDismissed) {
            showArrivalConfirmation = true
        }
    }
    val countdownMin = countdownSeconds / 60
    val countdownSec = countdownSeconds % 60

    // Reroute suggestion: show if probability drops below 30%
    var showRerouteSuggestion by remember { mutableStateOf(false) }
    val currentProbability = zone.probability
    LaunchedEffect(currentProbability) {
        if (currentProbability < 0.30) {
            showRerouteSuggestion = true
        }
    }
    // Find next best alternative zone
    val alternativeZone = remember(zones) {
        zones.filter { it.id != zoneId }.maxByOrNull { it.probability }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandCyan, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("NAVIGATING", style = MaterialTheme.typography.titleLarge, color = BrandCyan, letterSpacing = 2.sp)
            }

            // Direction arrow
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.KeyboardArrowUp, 
                    contentDescription = "Go Straight", 
                    tint = BrandCyan, 
                    modifier = Modifier.size(200.dp)
                )
                Text("In 200m", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black)
                Text("Arrive at ${zone.name}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Bottom Info Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CHANCE OF SPOT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$probPercent%", style = MaterialTheme.typography.displaySmall, color = BrandCyan, fontWeight = FontWeight.Bold)
                    }
                    // Countdown timer
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ARRIVING IN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            String.format("%d:%02d", countdownMin, countdownSec),
                            style = MaterialTheme.typography.displaySmall,
                            color = if (countdownSeconds < 60) EmeraldLive else MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ETA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${zone.expectedTimeToParkMinutes}m", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Reroute suggestion banner
            if (showRerouteSuggestion && alternativeZone != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().clickable {
                        showRerouteSuggestion = false
                        navController.navigate("turn/${alternativeZone.id}") {
                            popUpTo("turn/$zoneId") { inclusive = true }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PROBABILITY DROPPED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = AmberWarning)
                            Text("Tap to reroute to ${alternativeZone.name} (${(alternativeZone.probability * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Floating Voice Assistant Button
        FloatingActionButton(
            onClick = {
                if (!isListening) {
                    isListening = true
                    voiceFeedbackMsg = "Listening..."
                    scope.launch {
                        delay(2500)
                        // Simulate random voice recognition result
                        val found = (0..1).random() == 1
                        val feedback = if (found) FeedbackType.FOUND_PARKING else FeedbackType.SPOT_TAKEN
                        voiceFeedbackMsg = if (found) "Heard: 'Spot Found' \u2714" else "Heard: 'Spot Taken' \u2718"
                        viewModel.submitOutcome(zoneId, feedback)
                        delay(2000)
                        isListening = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .size(80.dp),
            containerColor = if (isListening) CrimsonDanger else BrandCyan,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Voice Assistant", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimary)
        }

        // Arrival Parking Confirmation Dialog
        if (showArrivalConfirmation) {
            ParkingConfirmationDialog(
                zoneName = zone.name,
                onConfirmParked = { _ ->
                    viewModel.submitOutcome(zoneId, FeedbackType.FOUND_PARKING)
                    showArrivalConfirmation = false
                    arrivalDialogDismissed = true
                    navController.popBackStack("map", inclusive = false)
                },
                onSpotTaken = { _ ->
                    viewModel.submitOutcome(zoneId, FeedbackType.SPOT_TAKEN)
                    showArrivalConfirmation = false
                    arrivalDialogDismissed = true
                    showRerouteSuggestion = true
                },
                onDismiss = {
                    showArrivalConfirmation = false
                    arrivalDialogDismissed = true
                }
            )
        }

        // Voice Overlay
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .clickable(enabled = false) {}, // intercept clicks
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "micPulse"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(CircleShape)
                        .background(CrimsonDanger.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice recognition active", tint = CrimsonDanger, modifier = Modifier.size(64.dp))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = voiceFeedbackMsg,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (voiceFeedbackMsg == "Listening...") {
                        Text("Say 'Spot Found' or 'Spot Taken'", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
