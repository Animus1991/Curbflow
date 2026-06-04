package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.ui.components.ContextualMapContainer
import com.example.ui.components.FleetDeviceCard
import com.example.ui.components.MiniMapToggleFab
import com.example.ui.components.PrivacyByDesignCard
import com.example.ui.theme.BorderSubtle

@Composable
fun FleetScreen(navController: NavController) {
    var isFullScreenMap by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Map Section
            val mapWeight by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isFullScreenMap) 1f else 0.4f,
                label = "mapWeight"
            )

            ContextualMapContainer(
                modifier = Modifier.fillMaxWidth().weight(mapWeight),
                zones = emptyList(),
                privateParkings = emptyList(),
                isFullScreen = isFullScreenMap,
                onFullScreenToggle = { isFullScreenMap = !isFullScreenMap }
            )

            // List Section
            if (!isFullScreenMap) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(0.6f)
                ) {
                    item {
                        Text(
                            "Active Fleet Network",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrivacyByDesignCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(MockData.fleetContributors, key = { it.id }) { device ->
                        val isOnline = device.deviceStatus == com.example.data.DeviceStatus.ONLINE
                        FleetDeviceCard(
                            vehicleType = "${device.vehicleType.name} - ${device.areaCovered}",
                            status = if (isOnline) "Online" else "Offline",
                            coverageScore = device.coverageScore,
                            dataQuality = "${(device.dataQualityScore * 100).toInt()}%",
                            reward = "Active", // Or replace with something else
                            privacyMode = "Metadata Only"
                        )
                    }
                }
            }
        }
    }
}
