package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.data.SensorEvent
import com.example.data.SensorEventType
import com.example.ui.components.CDLCard
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorFeedScreen(
    navController: NavController,
    mapViewModel: MapViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    // Collect live sensor events into a list
    val events = remember { mutableStateListOf<SensorEvent>() }
    val listState = rememberLazyListState()

    // Seed with static mock events
    LaunchedEffect(Unit) {
        if (events.isEmpty()) {
            events.addAll(MockData.sensorEvents)
        }
    }

    // Collect live events from simulation service
    LaunchedEffect(Unit) {
        mapViewModel.sensorEvents.collectLatest { event ->
            events.add(0, event) // Newest first
            if (events.size > 100) events.removeLast() // Cap at 100
            listState.animateScrollToItem(0)
        }
    }

    // Filter state
    var selectedFilter by remember { mutableStateOf<SensorEventType?>(null) }
    val filteredEvents = if (selectedFilter == null) events else events.filter { it.eventType == selectedFilter }

    // Count events in last 5 minutes
    val recentCount = remember(events.size) {
        events.count { it.freshnessSeconds < 300 }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SENSOR FEED",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Live counter banner ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = NeonCyan.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NeonCyan.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Sensors,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "$recentCount events in last 5 min",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                        Text(
                            "${events.size} total events collected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Live indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmeraldLive, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = EmeraldLive
                    )
                }
            }

            // --- Filter chips ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All", fontWeight = if (selectedFilter == null) FontWeight.Black else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                        selectedLabelColor = NeonCyan
                    ),
                    border = if (selectedFilter == null)
                        BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                    else
                        FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = MaterialTheme.colorScheme.outline)
                )

                val chipFilters = listOf(
                    SensorEventType.EMPTY_SPOT_PROBABILITY to "Empty",
                    SensorEventType.OCCUPIED to "Occupied",
                    SensorEventType.ILLEGAL_PARKING_RISK to "Illegal",
                    SensorEventType.BLOCKAGE to "Blockage"
                )

                chipFilters.forEach { (type, label) ->
                    val (_, chipColor) = getEventVisuals(type)
                    val isSelected = selectedFilter == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = if (isSelected) null else type },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.15f),
                            selectedLabelColor = chipColor
                        ),
                        border = if (isSelected)
                            BorderStroke(1.dp, chipColor.copy(alpha = 0.4f))
                        else
                            FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = MaterialTheme.colorScheme.outline)
                    )
                }
            }

            // --- Event list ---
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Sensors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No events matching filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Live events will appear as sensors detect changes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            SensorEventCard(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorEventCard(event: SensorEvent) {
    val (icon, color) = getEventVisuals(event.eventType)
    val zoneName = remember(event.zoneId) {
        MockData.zones.find { it.id == event.zoneId }?.name ?: "Zone ${event.zoneId}"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = event.eventType.name,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    getEventLabel(event.eventType),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    zoneName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "via ${event.sourceType.name.lowercase().replace('_', ' ')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        formatFreshness(event.freshnessSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Confidence
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${(event.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    "confidence",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getEventVisuals(type: SensorEventType): Pair<ImageVector, Color> {
    return when (type) {
        SensorEventType.EMPTY_SPOT_PROBABILITY -> Icons.Default.CheckCircle to EmeraldLive
        SensorEventType.OCCUPIED -> Icons.Default.Block to CrimsonDanger
        SensorEventType.BLOCKAGE -> Icons.Default.Warning to AmberWarning
        SensorEventType.LOADING_ZONE -> Icons.Default.LocalShipping to NeonBlue
        SensorEventType.ILLEGAL_PARKING_RISK -> Icons.Default.Gavel to Color(0xFFAB47BC) // Purple
        SensorEventType.ROADWORK -> Icons.Default.Construction to AmberWarning
        SensorEventType.STALE_SIGNAL -> Icons.Default.SignalWifiOff to SlateGrey
    }
}

private fun getEventLabel(type: SensorEventType): String {
    return when (type) {
        SensorEventType.EMPTY_SPOT_PROBABILITY -> "Empty Spot Detected"
        SensorEventType.OCCUPIED -> "Spot Occupied"
        SensorEventType.BLOCKAGE -> "Blockage Reported"
        SensorEventType.LOADING_ZONE -> "Loading Zone Active"
        SensorEventType.ILLEGAL_PARKING_RISK -> "Illegal Parking Risk"
        SensorEventType.ROADWORK -> "Roadwork Ahead"
        SensorEventType.STALE_SIGNAL -> "Stale Signal"
    }
}

private fun formatFreshness(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}s ago"
        seconds < 3600 -> "${seconds / 60}m ago"
        else -> "${seconds / 3600}h ago"
    }
}
