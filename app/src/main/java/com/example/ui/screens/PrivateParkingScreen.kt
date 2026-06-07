package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.data.MockData
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateParkingScreen(navController: NavController) {
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isFullScreenMap by remember { mutableStateOf(false) }

    // Auto-dismiss toast after 3 seconds
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            toastMessage = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PRIVATE GARAGES", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
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
                privateParkings = MockData.privateList,
                isFullScreen = isFullScreenMap,
                onFullScreenToggle = { isFullScreenMap = !isFullScreenMap }
            )

            if (!isFullScreenMap) {
                Column(modifier = Modifier.weight(0.65f)) {
                    if (toastMessage != null) {
                        Surface(
                            color = NeonCyan.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = toastMessage!!,
                                color = NeonCyan,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (MockData.privateList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LocalParking, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No garages available nearby", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Private parking options will appear here when found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    "AVAILABLE GARAGES",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Indoor & covered parking you can book now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            val cheapestPrice = MockData.privateList.minOf { it.pricePerHour }
                            itemsIndexed(MockData.privateList, key = { _, p -> p.id }) { _, p ->
                                PrivateParkingCommandCard(
                                    name = p.name,
                                    area = p.area,
                                    price = "${p.pricePerHour}€",
                                    slots = p.availableSlots,
                                    totalSlots = p.totalSlots,
                                    walkTime = "${p.walkingMinutes}m",
                                    rating = p.rating,
                                    isCheapest = p.pricePerHour == cheapestPrice,
                                    onNavigate = { navController.navigate("booking/${p.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateParkingCommandCard(
    name: String,
    area: String,
    slots: Int,
    totalSlots: Int = slots,
    price: String,
    walkTime: String,
    rating: Double,
    isCheapest: Boolean = false,
    onNavigate: () -> Unit
) {
    val occupancy = if (totalSlots > 0) slots.toFloat() / totalSlots.toFloat() else 0f
    val occupancyColor = when {
        occupancy > 0.5f -> EmeraldLive
        occupancy > 0.2f -> AmberWarning
        else -> CrimsonDanger
    }

    CDLCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    if (isCheapest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = EmeraldLive.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("BEST PRICE", style = MaterialTheme.typography.labelSmall, color = EmeraldLive, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
                Text("$area • $price/HR • $walkTime WALK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Text(" $rating", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("$slots/$totalSlots", style = MaterialTheme.typography.labelSmall, color = occupancyColor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Availability meter
                LinearProgressIndicator(
                    progress = { occupancy },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = occupancyColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onNavigate,
                modifier = Modifier.width(100.dp).height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
            ) {
                Text("BOOK", fontWeight = FontWeight.Black)
            }
        }
    }
}
