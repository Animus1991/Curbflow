package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ProbabilityPulseIndicator
import com.example.ui.theme.BrandCyan

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            "CurbFlow AI",
            "Stop circling. Start parking.",
            "We predict which streets have open spots right now — so you drive straight there.",
            Icons.Default.AutoAwesome
        ),
        OnboardingStep(
            "Color-Coded Map",
            "Easy as a traffic light.",
            "Green glow = spots likely available. Red glow = area is full. No guessing.",
            Icons.Default.MyLocation
        ),
        OnboardingStep(
            "Tap & Go",
            "Pick a zone, we guide you.",
            "Tap any green zone on the map, then hit Navigate. We'll take you there.",
            Icons.Default.TouchApp
        ),
        OnboardingStep(
            "Help Us Help You",
            "Report what you find.",
            "After parking, tap SUCCESS or FULL to improve predictions for everyone. The community makes CurbFlow smarter.",
            Icons.Default.Feedback
        ),
        OnboardingStep(
            "Private Garages",
            "Always have a backup.",
            "If streets are full, book a covered garage instantly. Fixed price, guaranteed spot, and we guide you there.",
            Icons.Default.LocalParking
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                },
                label = "onboarding_step"
            ) { currentStep ->
                val data = steps[currentStep]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (currentStep == 1) {
                        // Visual demo for step 2
                        Row(modifier = Modifier.padding(bottom = 32.dp)) {
                            ProbabilityPulseIndicator(0.9)
                            Spacer(modifier = Modifier.width(16.dp))
                            ProbabilityPulseIndicator(0.1)
                        }
                    } else {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = BrandCyan
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandCyan,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = data.subtitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Progress Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (index == step) BrandCyan else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                }
            }
        }

        // Skip button
        if (step < steps.size - 1) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("SKIP", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = {
                if (step < steps.size - 1) step++ else onFinish()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
        ) {
            Text(
                if (step < steps.size - 1) "NEXT" else "GET STARTED",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector
)
