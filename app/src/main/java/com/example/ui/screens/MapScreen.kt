package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.FeedbackType
import com.example.data.ParkingZone
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory
import kotlinx.coroutines.launch

// --- CDL Immersive Navigation Cockpit v2 (Harmonious Convergence) ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val rankedZones by viewModel.zones.collectAsState()
    val isDriving by viewModel.isDriving.collectAsState()
    val favoriteZones by viewModel.favoriteZones.collectAsState()
    val themeManager = LocalThemeManager.current
    
    var selectedZone by remember { mutableStateOf<ParkingZone?>(null) }
    var isMapLabOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Restored State from Original Utility ---
    var showHeatmap by remember { mutableStateOf(true) }
    var heatmapType by remember { mutableStateOf(HeatmapType.PROBABILITY) }
    var trafficVisible by remember { mutableStateOf(false) }
    var restrictedZonesVisible by remember { mutableStateOf(true) }
    var forecastVisible by remember { mutableStateOf(false) }
    var weatherVisible by remember { mutableStateOf(false) }
    var individualSpotsVisible by remember { mutableStateOf(true) }
    
    var timeTravelValue by remember { mutableStateOf(12f) }
    var selectedDayIndex by remember { mutableStateOf(0) } // 0=Mon, 1=Tue...
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    var probabilityThreshold by remember { mutableStateOf(0f) }

    // --- Destination Search ---
    var searchQuery by remember { mutableStateOf("") }
    var mapCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val searchResults = remember(searchQuery, rankedZones) {
        val q = searchQuery.trim()
        if (q.isBlank()) emptyList()
        else rankedZones.filter {
            it.name.contains(q, ignoreCase = true) || it.area.contains(q, ignoreCase = true)
        }.take(6)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            // 1. Full-Screen Intelligence Map (Integrated with Restored State)
            val filteredZones = if (probabilityThreshold > 0f) rankedZones.filter { it.probability >= probabilityThreshold } else rankedZones
            OsmdroidMapView.RenderMap(
                zones = filteredZones,
                privateParkings = emptyList(),
                isDriving = isDriving,
                showHeatmap = showHeatmap,
                heatmapType = heatmapType,
                trafficVisible = trafficVisible,
                dynamicSpots = emptyList(),
                spotMarkersVisible = individualSpotsVisible,
                restrictedZonesVisible = restrictedZonesVisible,
                forecastVisible = forecastVisible,
                weatherVisible = weatherVisible,
                userLocation = null,
                mapCenter = mapCenter,
                onZoneSelected = { selectedZone = it }
            )

            // 2. Immersive Spotlight Effect
            AnimatedVisibility(
                visible = selectedZone != null,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)).blur(8.dp))
            }

            // 2b. Map Legend (explains heatmap colors)
            AnimatedVisibility(
                visible = showHeatmap && !isDriving && selectedZone == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 130.dp)
            ) {
                MapLegendOverlay(heatmapType = heatmapType)
            }

            // 2c. Live Data Connection Status
            val wsState by com.example.domain.HeatmapWebSocketService.connectionState.collectAsState()
            val (wsLabel, wsColor) = when (wsState) {
                com.example.domain.HeatmapWebSocketService.ConnectionState.CONNECTED -> "LIVE" to EmeraldLive
                com.example.domain.HeatmapWebSocketService.ConnectionState.CONNECTING -> "CONNECTING" to AmberWarning
                com.example.domain.HeatmapWebSocketService.ConnectionState.RECONNECTING -> "RECONNECTING" to AmberWarning
                com.example.domain.HeatmapWebSocketService.ConnectionState.ERROR -> "OFFLINE" to CrimsonDanger
                com.example.domain.HeatmapWebSocketService.ConnectionState.DISCONNECTED -> "OFFLINE" to SmokeGrey
            }
            AnimatedVisibility(
                visible = !isDriving && selectedZone == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp)
            ) {
                Surface(
                    color = wsColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(wsColor, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(wsLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = wsColor)
                    }
                }
            }

            // 3. Cockpit Top Command Deck
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                AnimatedVisibility(
                    visible = !isDriving && selectedZone == null,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Theme Toggle Anchor
                            ThemeToggleAnchor(
                                isDark = themeManager.isDarkTheme,
                                onToggle = { themeManager.toggleTheme() }
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MapCommandAnchor(
                                    icon = Icons.Default.Science,
                                    label = "LAYERS",
                                    onClick = { isMapLabOpen = true },
                                    color = NeonCyan
                                )
                                MapCommandAnchor(
                                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    label = "LIST",
                                    onClick = { navController.navigate("zones") },
                                    color = EmeraldLive
                                )
                                MapCommandAnchor(
                                    icon = Icons.Default.Settings,
                                    label = "MORE",
                                    onClick = { navController.navigate("settings") },
                                    color = SmokeGrey
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        DestinationSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            results = searchResults,
                            onResultSelected = { zone ->
                                mapCenter = zone.latitude to zone.longitude
                                searchQuery = ""
                                selectedZone = zone
                            },
                            onClear = { searchQuery = "" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Active Safety HUD
                AnimatedVisibility(
                    visible = isDriving,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    SafetyLockHUD()
                }
            }

            // 4. Time Travel Quick Glance (Affordance for sliding through time)
            if (!isDriving && selectedZone == null && !isMapLabOpen) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)) {
                    TimeTravelQuickBar(time = timeTravelValue, dayName = days[selectedDayIndex])
                }
            }

            // 5. Decision Cockpit (BottomSheet)
            if (selectedZone != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedZone = null },
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = SlateGrey) },
                    scrimColor = Color.Transparent,
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                ) {
                    CDLDecisionCockpit(
                        zone = selectedZone!!,
                        isFavorite = favoriteZones.contains(selectedZone!!.id),
                        onToggleFavorite = { viewModel.toggleFavorite(selectedZone!!.id) },
                        onStartGuidance = { 
                            val zoneId = selectedZone!!.id
                            selectedZone = null
                            navController.navigate("route/$zoneId") 
                        },
                        onSmartRoute = {
                            val zoneId = selectedZone!!.id
                            selectedZone = null
                            navController.navigate("route/$zoneId")
                        },
                        onFeedback = { 
                            viewModel.submitOutcome(selectedZone!!.id, it)
                            selectedZone = null
                        }
                    )
                }
            }

            // 6. Map Laboratory (Restored Utility in Elite UI)
            if (isMapLabOpen) {
                ModalBottomSheet(
                    onDismissRequest = { isMapLabOpen = false },
                    containerColor = MaterialTheme.colorScheme.background,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = NeonCyan) }
                ) {
                    MapLaboratoryContent(
                        showHeatmap = showHeatmap, onHeatmapToggle = { showHeatmap = it },
                        heatmapType = heatmapType, onHeatmapTypeChange = { heatmapType = it },
                        trafficVisible = trafficVisible, onTrafficToggle = { trafficVisible = it },
                        restrictedVisible = restrictedZonesVisible, onRestrictedToggle = { restrictedZonesVisible = it },
                        forecastVisible = forecastVisible, onForecastToggle = { forecastVisible = it },
                        weatherVisible = weatherVisible, onWeatherToggle = { weatherVisible = it },
                        individualSpotsVisible = individualSpotsVisible, onSpotsToggle = { individualSpotsVisible = it },
                        probabilityThreshold = probabilityThreshold, onThresholdChange = { probabilityThreshold = it },
                        timeTravelValue = timeTravelValue, onTimeChange = { timeTravelValue = it },
                        selectedDayIndex = selectedDayIndex, onDayChange = { selectedDayIndex = it },
                        days = days,
                        viewModel = viewModel,
                        onClose = { isMapLabOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeToggleAnchor(isDark: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode, 
                contentDescription = "Theme", 
                tint = NeonCyan, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isDark) "NIGHT" else "DAY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun MapCommandAnchor(icon: ImageVector, label: String, onClick: () -> Unit, color: Color) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun TimeTravelQuickBar(time: Float, dayName: String = "") {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = if (dayName.isNotEmpty()) "FORECAST: $dayName ${time.toInt()}:00" else "FORECAST: ${time.toInt()}:00",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = NeonCyan
        )
    }
}

@Composable
fun SafetyLockHUD() {
    GlassSurface(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.border(BorderStroke(1.5.dp, CrimsonDanger.copy(alpha = 0.4f)), RoundedCornerShape(32.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Drive mode active", tint = CrimsonDanger, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("DRIVE MODE • MAP LOCKED", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapLaboratoryContent(
    showHeatmap: Boolean, onHeatmapToggle: (Boolean) -> Unit,
    heatmapType: HeatmapType, onHeatmapTypeChange: (HeatmapType) -> Unit,
    trafficVisible: Boolean, onTrafficToggle: (Boolean) -> Unit,
    restrictedVisible: Boolean, onRestrictedToggle: (Boolean) -> Unit,
    forecastVisible: Boolean, onForecastToggle: (Boolean) -> Unit,
    weatherVisible: Boolean = false, onWeatherToggle: (Boolean) -> Unit = {},
    individualSpotsVisible: Boolean, onSpotsToggle: (Boolean) -> Unit,
    probabilityThreshold: Float = 0f, onThresholdChange: (Float) -> Unit = {},
    timeTravelValue: Float, onTimeChange: (Float) -> Unit,
    selectedDayIndex: Int, onDayChange: (Int) -> Unit,
    days: List<String>,
    viewModel: MapViewModel,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
        Text("MAP LAYERS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = NeonCyan)
        Text("Choose what to show on the map and preview future hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 1. Layer Toggles (The "Status Chips" pattern)
        Text("ACTIVE LAYERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabChip("Heatmap", showHeatmap, { onHeatmapToggle(it) })
            LabChip("Traffic", trafficVisible, { onTrafficToggle(it) })
            LabChip("Restricted", restrictedVisible, { onRestrictedToggle(it) })
            LabChip("Spots", individualSpotsVisible, { onSpotsToggle(it) })
            LabChip("Forecast", forecastVisible, { onForecastToggle(it) })
            LabChip("Weather", weatherVisible, { onWeatherToggle(it) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Probability Filter
        Text("AVAILABILITY FILTER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (probabilityThreshold > 0f) "Show only zones above ${(probabilityThreshold * 100).toInt()}% chance"
            else "Showing all zones (no filter)",
            style = MaterialTheme.typography.bodyMedium, color = NeonCyan
        )
        Slider(
            value = probabilityThreshold,
            onValueChange = onThresholdChange,
            valueRange = 0f..0.9f,
            colors = SliderDefaults.colors(thumbColor = EmeraldLive, activeTrackColor = EmeraldLive)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Time Travel Simulation
        Text("TIME TRAVEL SIMULATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Predict availability for ${days[selectedDayIndex]} at ${timeTravelValue.toInt()}:00", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
        Slider(
            value = timeTravelValue,
            onValueChange = onTimeChange,
            valueRange = 0f..23f,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days.size) { index ->
                FilterChip(
                    selected = selectedDayIndex == index,
                    onClick = { onDayChange(index) },
                    label = { Text(days[index]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("SIMULATE DRIVING SPEED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Text("Above 15 km/h the map switches to hands-free safe mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VelocitySlider(viewModel)
        
        Spacer(modifier = Modifier.height(48.dp))
        ActionButton(text = "APPLY & CLOSE", icon = Icons.Default.Check, onClick = onClose)
    }
}

@Composable
fun LabChip(label: String, active: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = active,
        onClick = { onToggle(!active) },
        label = { Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black) },
        leadingIcon = if (active) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
            selectedLabelColor = NeonCyan,
            selectedLeadingIconColor = NeonCyan,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = NeonCyan,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
            enabled = true,
            selected = active
        )
    )
}

@Composable
fun CDLDecisionCockpit(
    zone: ParkingZone,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onStartGuidance: () -> Unit,
    onSmartRoute: () -> Unit = {},
    onFeedback: (FeedbackType) -> Unit
) {
    val freshnessColor = when {
        zone.freshnessMinutes < 5 -> EmeraldLive
        zone.freshnessMinutes < 15 -> AmberWarning
        else -> CrimsonDanger
    }
    val freshnessLabel = zone.lastUpdatedLabel

    Column(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoGraph, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BEST MATCH FOR YOU", style = MaterialTheme.typography.labelSmall, color = EmeraldLive, fontWeight = FontWeight.Black)
                }
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) CrimsonDanger else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Data Freshness Badge ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(8.dp).background(freshnessColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Updated $freshnessLabel",
                style = MaterialTheme.typography.labelSmall,
                color = freshnessColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Active users indicator
            Icon(Icons.Default.Groups, contentDescription = "Others nearby", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "${zone.activeUsers} others heading here",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- Legal Risk Warning ---
        if (zone.legalRisk == com.example.data.LegalRisk.HIGH || zone.legalRisk == com.example.data.LegalRisk.RESTRICTED) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CrimsonDanger.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CrimsonDanger.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Legal risk", tint = CrimsonDanger, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (zone.legalRisk == com.example.data.LegalRisk.RESTRICTED)
                            "RESTRICTED ZONE — Parking may be prohibited or time-limited"
                        else
                            "HIGH ENFORCEMENT — Increased ticket risk in this area",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonDanger
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricGlance("AVAILABILITY", if (zone.probability > 0.7) "HIGH" else if (zone.probability > 0.4) "MED" else "LOW", if (zone.probability > 0.7) EmeraldLive else if (zone.probability > 0.4) AmberWarning else CrimsonDanger, Modifier.weight(1f))
            MetricGlance("ETA", "${zone.expectedTimeToParkMinutes}m", NeonCyan, Modifier.weight(1f))
            MetricGlance("WALK", "${zone.walkingTimeToDestinationMinutes}m", NeonBlue, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        ZoneTrendChart(zone)
        Spacer(modifier = Modifier.height(16.dp))
        ConfidenceMeter(zone.confidence)
        Spacer(modifier = Modifier.height(24.dp))

        ActionButton(
            text = "NAVIGATE HERE",
            icon = Icons.Default.NearMe,
            onClick = onStartGuidance
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Smart Route Suggestion ---
        OutlinedButton(
            onClick = onSmartRoute,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, EmeraldLive.copy(alpha = 0.5f))
        ) {
            Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("TRY SMART ROUTE — HIGHER CHANCE", fontWeight = FontWeight.Black, color = EmeraldLive, style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text("HOW DID IT GO? TAP TO HELP OTHERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            OutcomeButton(text = "SUCCESS", icon = Icons.Default.CheckCircle, color = EmeraldLive) { onFeedback(FeedbackType.FOUND_PARKING) }
            OutcomeButton(text = "FULL", icon = Icons.Default.Cancel, color = CrimsonDanger) { onFeedback(FeedbackType.SPOT_TAKEN) }
            OutcomeButton(text = "PRIVATE", icon = Icons.Default.LocalParking, color = NeonBlue) { onFeedback(FeedbackType.CHOSE_PRIVATE_PARKING) }
        }
    }
}

@Composable
fun MetricGlance(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OutcomeButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.5.dp, color.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = text, tint = color, modifier = Modifier.size(32.dp))
            }
        }
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
    }
}

@Composable
fun DestinationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<ParkingZone>,
    onResultSelected: (ParkingZone) -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search destination or area…") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (results.isNotEmpty()) {
                    Column {
                        results.forEach { zone ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onResultSelected(zone) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(zone.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(zone.area, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                ProbabilityBadge(zone.probability)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("No matching areas found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun VelocitySlider(viewModel: MapViewModel) {
    val speedKmh by viewModel.speed.collectAsState()
    Column {
        Text("CURRENT SPEED: ${speedKmh.toInt()} KM/H", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Slider(
            value = speedKmh,
            onValueChange = { viewModel.setSpeed(it) },
            valueRange = 0f..60f,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
