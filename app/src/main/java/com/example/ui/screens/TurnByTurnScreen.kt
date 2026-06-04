package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
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
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnByTurnScreen(navController: NavController, zoneId: String, viewModel: MapViewModel = viewModel()) {
    val zone = MockData.zones.find { it.id == zoneId } ?: return
    val probPercent = (zone.probability * 100).toInt()
    
    var isListening by remember { mutableStateOf(false) }
    var voiceFeedbackMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandCyan, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("HUD TURN-BY-TURN", style = MaterialTheme.typography.titleLarge, color = BrandCyan, letterSpacing = 2.sp)
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
                        Text("PROBABILITY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$probPercent%", style = MaterialTheme.typography.displaySmall, color = BrandCyan, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ETA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${zone.expectedTimeToParkMinutes}m", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
                        voiceFeedbackMsg = "Recognized: 'Spot Taken'"
                        viewModel.submitOutcome(zoneId, FeedbackType.SPOT_TAKEN)
                        delay(1500)
                        isListening = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .size(80.dp),
            containerColor = if (isListening) Color.Red else BrandCyan,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Voice Assistant", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimary)
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
                        .background(Color.Red.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
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
