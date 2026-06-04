package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.focus.onFocusChanged
import org.json.JSONArray

import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.data.ParkingZone
import com.example.ui.theme.*
import com.example.ui.components.CustomFallbackMap
import com.example.ui.components.MapControlsOverlay
import com.example.ui.components.ZoneTrendChart

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.ParkingScoringEngine
import kotlinx.coroutines.launch

@Composable
fun VelocitySlider(viewModel: MapViewModel) {
    val speedKmh by viewModel.speed.collectAsState()
    val isDriving by viewModel.isDriving.collectAsState()
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Speed: ${speedKmh.toInt()} km/h", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isDriving) {
                Text("DRIVING MODE (UI HIDDEN)", color = BrandCyan, fontWeight = FontWeight.Bold)
            }
        }
        Slider(
            value = speedKmh,
            onValueChange = { viewModel.setSpeed(it) },
            valueRange = 0f..60f,
            colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel = viewModel()) {
    val rankedZones by viewModel.zones.collectAsState()
    val isDriving by viewModel.isDriving.collectAsState()
    val favoriteZones by viewModel.favoriteZones.collectAsState()
    val alertThresholds by viewModel.alertThresholds.collectAsState()
    
    val context = LocalContext.current
    var selectedZone by remember { mutableStateOf<ParkingZone?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var timeSliderValue by remember { mutableStateOf(12f) }
    var selectedDay by remember { mutableStateOf(0) } // Default Monday
    var heatmapVisible by remember { mutableStateOf(true) }
    var heatmapType by remember { mutableStateOf(com.example.ui.components.HeatmapType.PROBABILITY) }
    var trafficVisible by remember { mutableStateOf(false) }
    var probabilityThreshold by remember { mutableStateOf(0.0f) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var mapCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var dynamicSpots by remember { mutableStateOf<List<com.example.api.DynamicSpot>>(emptyList()) }
    var spotMarkersVisible by remember { mutableStateOf(true) }
    var restrictedZonesVisible by remember { mutableStateOf(false) }
    var forecastVisible by remember { mutableStateOf(false) }
    var weatherVisible by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var voiceSearchQuery by remember { mutableStateOf("") }

    var queriedZones by remember { mutableStateOf<List<ParkingZone>>(emptyList()) }
    var isQuerying by remember { mutableStateOf(false) }

    LaunchedEffect(mapCenter) {
        val center = mapCenter ?: Pair(37.9715, 23.7267)
        while(true) {
            dynamicSpots = com.example.api.MockExternalApi.fetchHighProbabilitySpots(center.first, center.second)
            // Also query parking zones using the service
            isQuerying = true
            try {
                queriedZones = com.example.domain.RealTimeParkingService.queryLocation(center.first, center.second)
            } finally {
                isQuerying = false
            }
            kotlinx.coroutines.delay(30_000L)
        }
    }

    val adjustedZones = remember(rankedZones, timeSliderValue, selectedDay, probabilityThreshold) {
        rankedZones.map { zone ->
            val diff = kotlin.math.abs(timeSliderValue - 12f)
            val timeFactor = 1.0 - (diff / 24.0) 
            val dayFactor = if (selectedDay >= 5) 0.8 else 1.0
            zone.copy(probability = (zone.probability * timeFactor * dayFactor).coerceIn(0.0, 1.0))
        }.filter { it.probability >= probabilityThreshold }
    }

    val combinedZones = remember(adjustedZones, queriedZones) {
        adjustedZones + queriedZones
    }
    
    LaunchedEffect(combinedZones, favoriteZones, alertThresholds) {
        val triggeredAlerts = combinedZones.filter { zone ->
            favoriteZones.contains(zone.id) && zone.probability >= (alertThresholds[zone.id] ?: 0.7f)
        }
        if (triggeredAlerts.isNotEmpty()) {
            val names = triggeredAlerts.take(2).joinToString { it.name }
            val suffix = if (triggeredAlerts.size > 2) " and others" else ""
            
            com.example.util.NotificationHelper.sendNotification(
                context = context,
                title = "Parking Alert",
                message = "High probability in: $names$suffix"
            )
            
            snackbarHostState.showSnackbar(
                message = "Alert! High probability in: $names$suffix",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text("Dashboard", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), color = BrandCyan)
                Divider(color = BrandCyan.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                Text("Real-Time Freshness", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp))
                
                MockData.municipalData.forEach { data ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(data.area, fontWeight = FontWeight.Bold)
                        Text("Cruising Pressure: ${(data.cruisingPressure*100).toInt()}% | Occupancy: ${(data.occupancyRate*100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(
                            progress = { (1.0 - data.occupancyRate).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color = ProbabilityHigh,
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Divider(color = BrandCyan.copy(alpha = 0.5f))
                Text("Map Data Freshness", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp))
                
                rankedZones.take(5).forEach { zone ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(zone.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(zone.lastUpdatedLabel, style = MaterialTheme.typography.bodySmall, color = BrandCyan)
                    }
                }
            }
        }
    ) {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isDriving && selectedZone == null) {
                FloatingActionButton(
                    onClick = {
                        val best = ParkingScoringEngine.selectBestZone(rankedZones)
                        if (best != null) navController.navigate("turn_by_turn/${best.id}")
                    },
                    containerColor = BrandCyan,
                    contentColor = Color.Black
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Navigation, contentDescription = "Navigate")
                }
            }
        }
    ) { scaffoldPadding ->
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                if (selectedZone != null) {
                    ZoneBottomSheetContent(
                        zone = selectedZone!!,
                        isFavorite = favoriteZones.contains(selectedZone!!.id),
                        alertThreshold = alertThresholds[selectedZone!!.id],
                        onToggleFavorite = { viewModel.toggleFavorite(selectedZone!!.id) },
                        onThresholdChange = { viewModel.setAlertThreshold(selectedZone!!.id, it) },
                        onNavigate = { navController.navigate("turn_by_turn/${selectedZone!!.id}") },
                        onPrivateFallback = { navController.navigate("private") },
                        onFeedback = { type -> viewModel.submitOutcome(selectedZone!!.id, type) }
                    )
                } else {
                    Box(modifier = Modifier.height(1.dp))
                }
            },
            sheetPeekHeight = if (selectedZone != null && !isFullScreen) 120.dp else 0.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(scaffoldPadding)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            if (!isFullScreen && !isDriving) {
                MapTopSearchPanel(
                    onDashboardClick = { scope.launch { drawerState.open() } },
                    onListClick = { navController.navigate("zones") },
                    onSearch = { query ->
                        try {
                            val coords = query.split(",").map { it.trim().toDoubleOrNull() }
                            if (coords.size == 2 && coords[0] != null && coords[1] != null) {
                                mapCenter = Pair(coords[0]!!, coords[1]!!)
                            } else {
                                val geocoder = android.location.Geocoder(context)
                                val addresses = geocoder.getFromLocationName(query, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    mapCenter = Pair(addresses[0].latitude, addresses[0].longitude)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore exceptions
                        }
                    }
                )
            }

            // Velocity Slider
            if (!isFullScreen) {
                VelocitySlider(viewModel)
            }
            
            // Map Controls
            if (!isFullScreen && !isDriving) {
                MapControlsOverlay(
                    timeSliderValue = timeSliderValue,
                    onTimeChange = { timeSliderValue = it },
                    selectedDay = selectedDay,
                    onDayChange = { selectedDay = it },
                    heatmapVisible = heatmapVisible,
                    onHeatmapToggle = { heatmapVisible = it },
                    heatmapType = heatmapType,
                    onHeatmapTypeChange = { heatmapType = it },
                    trafficVisible = trafficVisible,
                    onTrafficToggle = { trafficVisible = it },
                    probabilityThreshold = probabilityThreshold,
                    onThresholdChange = { probabilityThreshold = it },
                    spotMarkersVisible = spotMarkersVisible,
                    onSpotMarkersToggle = { spotMarkersVisible = it },
                    restrictedZonesVisible = restrictedZonesVisible,
                    onRestrictedZonesToggle = { restrictedZonesVisible = it },
                    forecastVisible = forecastVisible,
                    onForecastToggle = { forecastVisible = it },
                    weatherVisible = weatherVisible,
                    onWeatherToggle = { weatherVisible = it },
                    onFindLocation = {
                        // Mock location or hook up accompanied permissions later
                        userLocation = Pair(37.9715, 23.7267) // Athens Mock
                        mapCenter = Pair(37.9715, 23.7267)
                    }
                )
            }

            // Map
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                com.example.ui.components.OsmdroidMapView.RenderMap(
                    zones = combinedZones,
                    privateParkings = MockData.privateList,
                    isDriving = isDriving,
                    showHeatmap = heatmapVisible,
                    heatmapType = heatmapType,
                    trafficVisible = trafficVisible,
                    dynamicSpots = dynamicSpots,
                    userLocation = userLocation,
                    mapCenter = mapCenter,
                    onZoneSelected = { zone ->
                        selectedZone = zone
                        scope.launch {
                            sheetState.partialExpand()
                        }
                    }
                )
                
                // Map Legend Overlay
                if (!isDriving && heatmapVisible) {
                    com.example.ui.components.MapLegendOverlay(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(bottom = 16.dp, end = 8.dp),
                        heatmapType = heatmapType
                    )
                }

                // Full Screen Toggle
                IconButton(
                    onClick = { isFullScreen = !isFullScreen },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Full Screen Map",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopSearchPanel(onDashboardClick: () -> Unit, onListClick: () -> Unit, onSearch: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("recent_searches", android.content.Context.MODE_PRIVATE) }
    
    var recentSearches by remember {
        mutableStateOf(
            try {
                val jsonArray = JSONArray(prefs.getString("searches", "[]"))
                List(jsonArray.length()) { jsonArray.getString(it) }
            } catch (e: Exception) { emptyList<String>() }
        )
    }

    var isFocused by remember { mutableStateOf(false) }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val newSearches = (listOf(query) + recentSearches).distinct().take(5)
        recentSearches = newSearches
        prefs.edit().putString("searches", JSONArray(newSearches).toString()).apply()
    }

    val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                searchQuery = matches[0]
                addRecentSearch(searchQuery)
                onSearch(searchQuery)
            }
        }
    }

    TopAppBar(
        title = {
            Box {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it 
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {
                        addRecentSearch(searchQuery)
                        onSearch(searchQuery)
                    }),
                    placeholder = { Text("Search neighborhoods...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        IconButton(onClick = {
                            try {
                                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak a location to search..")
                                }
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) { }
                        }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Voice Search", tint = BrandCyan)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).onFocusChanged { isFocused = it.isFocused },
                    singleLine = true,
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                DropdownMenu(
                    expanded = isFocused && recentSearches.isNotEmpty(),
                    onDismissRequest = { isFocused = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    recentSearches.forEach { search ->
                        DropdownMenuItem(
                            text = { Text(search) },
                            onClick = {
                                searchQuery = search
                                addRecentSearch(search)
                                onSearch(search)
                                isFocused = false
                            },
                            leadingIcon = { Icon(Icons.Default.History, "Recent") }
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onDashboardClick) {
                Icon(Icons.Default.List, contentDescription = "Dashboard")
            }
        },
        actions = {
            IconButton(onClick = onListClick) {
                Icon(Icons.Default.List, contentDescription = "View Zones List")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = BrandCyan,
            navigationIconContentColor = BrandCyan
        )
    )
}

@Composable
fun ZoneBottomSheetContent(
    zone: ParkingZone,
    isFavorite: Boolean,
    alertThreshold: Float?,
    onToggleFavorite: () -> Unit,
    onThresholdChange: (Float) -> Unit,
    onNavigate: () -> Unit,
    onPrivateFallback: () -> Unit,
    onFeedback: (com.example.data.FeedbackType) -> Unit
) {
    val probPercent = (zone.probability * 100).toInt()
    val probColor = when {
        zone.probability > 0.6 -> ProbabilityHigh
        zone.probability > 0.3 -> ProbabilityMedium
        else -> ProbabilityLow
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                
                val baseTravelTime = 15 // simulated base travel time in mins
                val estimatedTravelTime = (baseTravelTime * (1.0 + zone.congestionImpact)).toInt()

                Text("ETA: ${estimatedTravelTime}m (Traffic: ${(zone.congestionImpact*100).toInt()}%) | Find limit: ${zone.expectedTimeToParkMinutes}m | Walk: ${zone.walkingTimeToDestinationMinutes}m", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (isFavorite) BrandCyan else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (isFavorite) {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(16.dp)) {
                Text("Alert Threshold: ${(alertThreshold?.times(100))?.toInt() ?: 70}%", style = MaterialTheme.typography.labelMedium, color = BrandCyan)
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Slider(
                    value = alertThreshold ?: 0.7f,
                    onValueChange = onThresholdChange,
                    valueRange = 0f..1f,
                    colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(probColor.copy(alpha = 0.2f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$probPercent%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = probColor,
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Expanded Details
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            MetricBox("CONFIDENCE", "${(zone.confidence * 100).toInt()}%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MetricBox("FRESHNESS", "${zone.freshnessMinutes}m", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            MetricBox("RISK", com.example.util.Formatting.legalRiskLabel(zone.legalRisk), Modifier.weight(1.5f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart showing 24h trend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("24h Trend", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            val context = LocalContext.current
            TextButton(
                onClick = {
                    android.widget.Toast.makeText(context, "Exporting summary report...", android.widget.Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Export Report", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ZoneTrendChart(zone = zone)

        Spacer(modifier = Modifier.height(16.dp))

        // Street-level snapshot
        Text("Street View Snapshot", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        
        val streetImages = remember {
            listOf(
                "https://images.unsplash.com/photo-1590623255106-cfa1dcfe62e9?w=800&q=80",
                "https://images.unsplash.com/photo-1517524008697-84bbe3c3fd98?w=800&q=80",
                "https://images.unsplash.com/photo-1449844908441-8829872d2607?w=800&q=80"
            )
        }
        var currentImageIndex by remember { mutableStateOf(0) }
        
        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            coil.compose.AsyncImage(
                model = streetImages[currentImageIndex],
                contentDescription = "Street Level Snapshot",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        currentImageIndex = (currentImageIndex + 1) % streetImages.size
                    }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${currentImageIndex + 1}/${streetImages.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigate,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.Navigation, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("NAVIGATE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onPrivateFallback,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = BrandCyan)
        ) {
            Text("PRIVATE FALLBACK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}
