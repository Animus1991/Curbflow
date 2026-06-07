package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.LocalThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("curbflow_prefs", Context.MODE_PRIVATE)
    var shareOutcome by remember { mutableStateOf(prefs.getBoolean("share_outcome", true)) }
    var receiveAlerts by remember { mutableStateOf(prefs.getBoolean("receive_alerts", false)) }
    var usePrivateFallback by remember { mutableStateOf(prefs.getBoolean("use_private_garages", true)) }
    val themeManager = LocalThemeManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Settings & Privacy") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandCyan)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = BrandCyan)
        )

        Column(modifier = Modifier
            .padding(16.dp)
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
            Text("Privacy by Design", style = MaterialTheme.typography.titleMedium, color = BrandCyan)
            Spacer(modifier = Modifier.height(8.dp))
            Text("We do not sell, reserve, or guarantee public street parking.", color = MaterialTheme.colorScheme.onBackground)
            Text("Your location remains private. Edge detection ensures raw video is never uploaded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            SettingToggle("Dark Mode", themeManager.isDarkTheme) { themeManager.toggleTheme() }
            SettingToggle("Share Parking Outcome", shareOutcome) { shareOutcome = it; prefs.edit().putBoolean("share_outcome", it).apply() }
            SettingToggle("Receive Parking Alerts", receiveAlerts) { receiveAlerts = it; prefs.edit().putBoolean("receive_alerts", it).apply() }
            SettingToggle("Suggest Private Garages", usePrivateFallback) { usePrivateFallback = it; prefs.edit().putBoolean("use_private_garages", it).apply() }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text("NOTIFICATIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            var highProbAlerts by remember { mutableStateOf(prefs.getBoolean("notif_high_prob", true)) }
            var bookingReminders by remember { mutableStateOf(prefs.getBoolean("notif_booking", true)) }
            var weeklyReport by remember { mutableStateOf(prefs.getBoolean("notif_weekly_co2", false)) }

            SettingToggle("High-Probability Spot Alerts", highProbAlerts) { highProbAlerts = it; prefs.edit().putBoolean("notif_high_prob", it).apply() }
            SettingToggle("Booking Reminders", bookingReminders) { bookingReminders = it; prefs.edit().putBoolean("notif_booking", it).apply() }
            SettingToggle("Weekly CO₂ Impact Report", weeklyReport) { weeklyReport = it; prefs.edit().putBoolean("notif_weekly_co2", it).apply() }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text("DATA FRESHNESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Maximum acceptable data age before marking as stale", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            var freshnessIndex by remember { mutableIntStateOf(prefs.getInt("freshness_pref", 1)) }
            val freshnessOptions = listOf("5 min", "10 min", "15 min")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                freshnessOptions.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = freshnessIndex == idx,
                        onClick = { freshnessIndex = idx; prefs.edit().putInt("freshness_pref", idx).apply() },
                        label = { Text(label, fontWeight = if (freshnessIndex == idx) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandCyan.copy(alpha = 0.15f),
                            selectedLabelColor = BrandCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text("ACCESSIBILITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            var colorblindMode by remember { mutableStateOf(prefs.getBoolean("colorblind_mode", false)) }
            var highContrast by remember { mutableStateOf(prefs.getBoolean("high_contrast", false)) }
            var largeText by remember { mutableStateOf(prefs.getBoolean("large_text", false)) }

            SettingToggle("Colorblind Mode (shapes + labels)", colorblindMode) { colorblindMode = it; prefs.edit().putBoolean("colorblind_mode", it).apply() }
            SettingToggle("High Contrast UI", highContrast) { highContrast = it; prefs.edit().putBoolean("high_contrast", it).apply() }
            SettingToggle("Larger Text", largeText) { largeText = it; prefs.edit().putBoolean("large_text", it).apply() }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("profile") }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("My Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("View your stats, reputation, and CO\u2082 impact", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("subscription") }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = BrandCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Subscription Plans", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Upgrade for real-time data and smart routing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        prefs.edit().putBoolean("onboarding_complete", false).apply()
                        navController.navigate("onboarding") {
                            popUpTo("map") { inclusive = true }
                        }
                    }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Replay Introduction", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("View the welcome guide again", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("privacy") }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = BrandCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Trust & Privacy Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("How we protect your identity and data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = "Performance", tint = MaterialTheme.colorScheme.onSecondaryContainer) 
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Map Engine", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                        Text("OpenStreetMap · Optimized Rendering", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Legal Disclaimer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("CurbFlow AI provides probability-based parking guidance using recent mobility signals. It does not guarantee public street parking availability and does not reserve or sell public road space.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CurbFlow AI v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingToggle(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = BrandCyan, checkedTrackColor = BrandCyan.copy(alpha=0.3f)))
    }
}
