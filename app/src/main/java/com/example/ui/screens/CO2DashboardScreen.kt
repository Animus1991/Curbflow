package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.MockData
import com.example.domain.PredictiveAnalyticsEngine
import com.example.ui.components.CDLCard
import com.example.ui.theme.*
import com.example.ui.util.LocalViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CO2DashboardScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val profile by viewModel.profile.collectAsState()
    val totalCO2 = profile?.totalCO2SavedKg ?: 0.0
    val totalParkings = profile?.totalParkingsFound ?: 0

    // Animate the counter
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedCO2 by animateFloatAsState(
        targetValue = if (animationPlayed) totalCO2.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "co2Counter"
    )

    LaunchedEffect(Unit) { animationPlayed = true }

    // Weekly mock data (simulated savings per day)
    val weeklyData = remember {
        listOf(0.3, 0.5, 0.8, 0.4, 0.6, 0.9, 0.3)
    }
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Area breakdown using municipal data
    val areaBreakdown = remember {
        MockData.municipalData.map { area ->
            val zones = MockData.zones.filter { it.area == area.area }
            val avgSaved = zones.sumOf { zone ->
                PredictiveAnalyticsEngine.calculateCO2Savings(zone, 3)
            }
            area.area to avgSaved
        }.sortedByDescending { it.second }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CO₂ IMPACT",
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Hero: Animated CO₂ counter ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = EmeraldLive.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, EmeraldLive.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(EmeraldLive.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = "CO₂ Savings",
                            tint = EmeraldLive,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        String.format("%.1f kg", animatedCO2),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = EmeraldLive
                    )
                    Text(
                        "CO₂ EMISSIONS PREVENTED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tree equivalence
                    val treesEquivalent = (totalCO2 / 21.77).coerceAtLeast(0.0)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Park,
                                contentDescription = null,
                                tint = EmeraldLive,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Equivalent to ${String.format("%.2f", treesEquivalent)} trees absorbing CO₂ for a year",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Weekly Trend Chart ---
            CDLCard {
                Text(
                    "WEEKLY TREND",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "CO₂ saved per day this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                WeeklyBarChart(
                    data = weeklyData,
                    labels = weekDays,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total this week: ${String.format("%.1f", weeklyData.sum())} kg",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLive
                    )
                    Text(
                        "Avg: ${String.format("%.2f", weeklyData.average())} kg/day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Area Breakdown ---
            CDLCard {
                Text(
                    "SAVINGS BY AREA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Estimated CO₂ prevented per smart parking session",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                val maxSaved = areaBreakdown.maxOfOrNull { it.second } ?: 1.0
                areaBreakdown.forEach { (area, saved) ->
                    AreaBreakdownRow(
                        area = area,
                        savedKg = saved,
                        fraction = (saved / maxSaved).toFloat()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Milestones ---
            CDLCard {
                Text(
                    "MILESTONES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Achievements unlocked through eco-parking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                val milestones = listOf(
                    Triple(1.0, "First Gram", Icons.Default.EmojiEvents),
                    Triple(2.0, "Eco Starter", Icons.Default.Spa),
                    Triple(5.0, "Green Commuter", Icons.Default.Park),
                    Triple(10.0, "Carbon Hero", Icons.Default.Stars),
                    Triple(25.0, "Planet Saver", Icons.Default.Public)
                )

                milestones.forEach { (threshold, title, icon) ->
                    val achieved = totalCO2 >= threshold
                    MilestoneRow(
                        icon = icon,
                        title = title,
                        threshold = "${threshold.toInt()} kg",
                        achieved = achieved
                    )
                    if (threshold != 25.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Stats Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImpactStatCard(
                    icon = Icons.Default.LocalParking,
                    value = "$totalParkings",
                    label = "Smart parkings",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                ImpactStatCard(
                    icon = Icons.Default.Timer,
                    value = "${totalParkings * 8}",
                    label = "Minutes saved",
                    color = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Share button ---
            Button(
                onClick = { /* Share intent would go here */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldLive,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "SHARE YOUR IMPACT",
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.max()
    val barColor = EmeraldLive
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val heightFraction by animateFloatAsState(
                    targetValue = (value / maxVal).toFloat(),
                    animationSpec = tween(800, delayMillis = index * 80),
                    label = "bar$index"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        String.format("%.1f", value),
                        style = MaterialTheme.typography.labelSmall,
                        color = barColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .fillMaxHeight(heightFraction)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(barColor, barColor.copy(alpha = 0.4f))
                                ),
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AreaBreakdownRow(area: String, savedKg: Double, fraction: Float) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(800),
        label = "areaBar"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                area,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                String.format("%.2f kg", savedKg),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldLive
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
            color = EmeraldLive,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun MilestoneRow(
    icon: ImageVector,
    title: String,
    threshold: String,
    achieved: Boolean
) {
    val tintColor = if (achieved) EmeraldLive else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val bgColor = if (achieved) EmeraldLive.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (achieved) EmeraldLive.copy(alpha = 0.15f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (achieved) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                threshold,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (achieved) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Achieved",
                tint = EmeraldLive,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ImpactStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    CDLCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
