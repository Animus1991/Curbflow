package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.ui.components.ContextualMapContainer
import com.example.ui.components.MiniMapToggleFab
import com.example.ui.components.PrivateParkingCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.StrongText

@Composable
fun PrivateParkingScreen(navController: NavController) {
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isFullScreenMap by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Map Section
            val mapWeight by animateFloatAsState(
                targetValue = if (isFullScreenMap) 1f else 0.4f,
                label = "mapWeight"
            )
            
            ContextualMapContainer(
                modifier = Modifier.fillMaxWidth().weight(mapWeight),
                zones = emptyList(),
                privateParkings = MockData.privateList,
                isFullScreen = isFullScreenMap,
                onFullScreenToggle = { isFullScreenMap = !isFullScreenMap }
            )

            // List Section
            if (!isFullScreenMap) {
                Column(modifier = Modifier.weight(0.6f)) {
                    if (toastMessage != null) {
                        Text(
                            text = toastMessage!!,
                            color = BrandCyan,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "Private Garages",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        itemsIndexed(MockData.privateList, key = { _, p -> p.id }) { index, p ->
                            PrivateParkingCard(
                                name = p.name,
                                price = "${p.pricePerHour}€ / hr",
                                slots = p.availableSlots,
                                distance = "${p.distanceMeters}m",
                                walkingTime = "3 min",
                                rating = 4.5,
                                onReserve = { toastMessage = "Reservation confirmed for ${p.name}" }
                            )
                        }
                    }
                }
            }
        }
    }
}
