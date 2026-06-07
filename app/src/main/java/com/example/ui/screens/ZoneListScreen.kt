package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.LegalRisk
import com.example.data.ParkingZone
import com.example.ui.components.ZoneCard
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory

enum class ZoneSortOption(val label: String) {
    PROBABILITY("Best Chance"),
    FRESHNESS("Freshest"),
    ETA("Fastest ETA"),
    LEGAL_RISK("Lowest Risk")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneListScreen(
    navController: NavController, 
    viewModel: MapViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val zones by viewModel.zones.collectAsState()
    var sortOption by remember { mutableStateOf(ZoneSortOption.PROBABILITY) }

    val sortedZones = remember(zones, sortOption) {
        when (sortOption) {
            ZoneSortOption.PROBABILITY -> zones.sortedByDescending { it.probability }
            ZoneSortOption.FRESHNESS -> zones.sortedBy { it.freshnessMinutes }
            ZoneSortOption.ETA -> zones.sortedBy { it.expectedTimeToParkMinutes }
            ZoneSortOption.LEGAL_RISK -> zones.sortedBy { it.legalRisk.ordinal }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("PARKING AREAS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (zones.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No parking areas detected yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Data is loading or unavailable for your area", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sort chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ZoneSortOption.entries.toList()) { option ->
                            FilterChip(
                                selected = sortOption == option,
                                onClick = { sortOption = option },
                                label = { Text(option.label, fontWeight = if (sortOption == option) FontWeight.Black else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                                    selectedLabelColor = NeonCyan
                                ),
                                border = if (sortOption == option)
                                    BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                                else
                                    FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                }

                items(sortedZones, key = { it.id }) { zone ->
                    EnhancedZoneCard(
                        zone = zone,
                        onClick = { navController.navigate("route/${zone.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedZoneCard(zone: ParkingZone, onClick: () -> Unit) {
    val freshnessColor = when {
        zone.freshnessMinutes < 5 -> EmeraldLive
        zone.freshnessMinutes < 15 -> AmberWarning
        else -> CrimsonDanger
    }
    val legalColor = when (zone.legalRisk) {
        LegalRisk.LOW -> EmeraldLive
        LegalRisk.MEDIUM -> AmberWarning
        LegalRisk.HIGH -> CrimsonDanger
        LegalRisk.RESTRICTED -> CrimsonDanger
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(zone.area, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Probability badge
                Surface(
                    color = if (zone.probability > 0.6) EmeraldLive.copy(alpha = 0.12f) else AmberWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "${(zone.probability * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (zone.probability > 0.6) EmeraldLive else AmberWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: freshness + legal risk + ETA
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Freshness
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(freshnessColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(zone.lastUpdatedLabel, style = MaterialTheme.typography.labelSmall, color = freshnessColor)
                }
                // Legal risk
                Surface(
                    color = legalColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        zone.legalRisk.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = legalColor
                    )
                }
                // ETA
                Text("ETA ${zone.expectedTimeToParkMinutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
