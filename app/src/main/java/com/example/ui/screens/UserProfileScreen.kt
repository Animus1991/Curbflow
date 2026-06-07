package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.SubscriptionTier
import com.example.ui.components.ActionButton
import com.example.ui.components.CDLCard
import com.example.ui.components.GlassSurface
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MY PROFILE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (profile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            val user = profile!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Avatar + Name ---
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile avatar",
                        tint = NeonCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    "Member since ${user.joinedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- Subscription Badge ---
                val (tierLabel, tierColor) = when (user.subscriptionTier) {
                    SubscriptionTier.FREE -> "FREE PLAN" to MaterialTheme.colorScheme.onSurfaceVariant
                    SubscriptionTier.COMMUTER -> "COMMUTER" to EmeraldLive
                    SubscriptionTier.PROFESSIONAL -> "PROFESSIONAL" to NeonCyan
                }
                Surface(
                    color = tierColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        tierLabel,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = tierColor
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Reputation Score ---
                ReputationMeter(score = user.reputationScore)

                Spacer(modifier = Modifier.height(32.dp))

                // --- Stats Grid ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileStatCard(
                        icon = Icons.Default.Eco,
                        value = String.format("%.1f kg", user.totalCO2SavedKg),
                        label = "CO₂ SAVED",
                        color = EmeraldLive,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        icon = Icons.Default.LocalParking,
                        value = "${user.totalParkingsFound}",
                        label = "PARKINGS FOUND",
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileStatCard(
                        icon = Icons.Default.Star,
                        value = String.format("%.1f", user.reputationScore),
                        label = "REPUTATION",
                        color = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        icon = Icons.Default.Park,
                        value = "${(user.totalCO2SavedKg / 21.77).toInt()}",
                        label = "TREES EQUIVALENT",
                        color = EmeraldLive,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Quick Actions ---
                ActionButton(
                    text = "UPGRADE PLAN",
                    icon = Icons.Default.Upgrade,
                    onClick = { navController.navigate("subscription") },
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.navigate("bookings") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonBlue.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = NeonBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "MY BOOKINGS",
                        fontWeight = FontWeight.Black,
                        color = NeonBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.navigate("co2") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldLive.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = EmeraldLive)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "VIEW CO₂ IMPACT",
                        fontWeight = FontWeight.Black,
                        color = EmeraldLive
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ReputationMeter(score: Double) {
    val animatedProgress by animateFloatAsState(
        targetValue = (score / 5.0).toFloat(),
        animationSpec = tween(1000),
        label = "reputation"
    )

    CDLCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "COMMUNITY REPUTATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Based on your parking reports accuracy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    score >= 4.0 -> EmeraldLive
                    score >= 3.0 -> AmberWarning
                    else -> CrimsonDanger
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                String.format("%.1f/5", score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = when {
                    score >= 4.0 -> EmeraldLive
                    score >= 3.0 -> AmberWarning
                    else -> CrimsonDanger
                }
            )
        }
    }
}

@Composable
fun ProfileStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    CDLCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
