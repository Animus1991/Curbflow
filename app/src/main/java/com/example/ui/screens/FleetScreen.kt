package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.DeviceStatus
import com.example.data.FleetContributor
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    navController: NavController,
    viewModel: FleetViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val contributors by viewModel.contributors.collectAsState()
    var isFullScreenMap by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FLEET NETWORK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val mapWeight by animateFloatAsState(
                targetValue = if (isFullScreenMap) 1f else 0.35f,
                label = "mapWeight"
            )

            ContextualMapContainer(
                modifier = Modifier.fillMaxWidth().weight(mapWeight),
                zones = emptyList(),
                isFullScreen = isFullScreenMap,
                onFullScreenToggle = { isFullScreenMap = !isFullScreenMap }
            )

            if (!isFullScreenMap) {
                if (contributors.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(0.65f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No fleet contributors yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Vehicles sharing data will appear here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.weight(0.65f)
                    ) {
                        item {
                            Column {
                                Text("CONTRIBUTOR NETWORK", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Vehicles that anonymously share live parking signals. The more active contributors, the fresher the predictions you see on the map.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        item {
                            FleetStatusBanner(contributors)
                        }

                        item {
                            OutlinedButton(
                                onClick = { navController.navigate("sensors") },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Sensors, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("VIEW LIVE SENSOR FEED", fontWeight = FontWeight.Black, color = NeonCyan, style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        items(contributors, key = { it.id }) { contributor ->
                            ContributorCommandCard(
                                contributor = contributor,
                                onPing = { viewModel.recordHeartbeat(contributor.id) },
                                onSimulate = { viewModel.simulateContribution(contributor.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FleetStatusBanner(contributors: List<FleetContributor>) {
    val activeCount = contributors.count { it.deviceStatus == DeviceStatus.ONLINE }
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(12.dp).background(if (activeCount > 0) EmeraldLive else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("$activeCount ACTIVE • SHARING LIVE PARKING DATA NOW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ContributorCommandCard(
    contributor: FleetContributor,
    onPing: () -> Unit,
    onSimulate: () -> Unit
) {
    CDLCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DevicePulseIndicator(contributor.deviceStatus == DeviceStatus.ONLINE)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contributor.id.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = NeonCyan)
                Text(contributor.areaCovered, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            RewardBadge(contributor.monthlyRewardEstimate)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Device Health Indicator
        val healthStatus = remember(contributor) {
            com.example.domain.FleetTelemetryEngine.checkDeviceHealth(contributor, System.currentTimeMillis())
        }
        val (healthLabel, healthColor) = when (healthStatus) {
            DeviceStatus.ONLINE -> "HEALTHY" to EmeraldLive
            DeviceStatus.DEGRADED -> "DEGRADED" to AmberWarning
            DeviceStatus.MAINTENANCE -> "MAINTENANCE" to NeonCyan
            DeviceStatus.OFFLINE -> "OFFLINE" to CrimsonDanger
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(healthColor, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text("DEVICE: $healthLabel", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = healthColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Last seen: ${contributor.lastHeartbeat}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("DATA QUALITY — HOW RELIABLE THIS VEHICLE'S SIGNALS ARE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { contributor.dataQualityScore.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (contributor.dataQualityScore > 0.7) EmeraldLive else AmberWarning,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSimulate,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("SEND DATA NOW", fontWeight = FontWeight.Black)
            }
            OutlinedButton(
                onClick = onPing,
                modifier = Modifier.weight(0.4f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Text("STATUS", color = NeonCyan, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun DevicePulseIndicator(isOnline: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "scale"
    )
    
    Box(contentAlignment = Alignment.Center) {
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { 
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - (scale - 1f) / 0.4f
                    }
                    .background(EmeraldLive.copy(alpha = 0.5f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (isOnline) EmeraldLive else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
        )
    }
}

@Composable
fun RewardBadge(amount: Double) {
    Surface(
        color = AmberWarning.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Token, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = AmberWarning
            )
        }
    }
}
