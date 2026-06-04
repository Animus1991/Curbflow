package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.BrandCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var shareOutcome by remember { mutableStateOf(true) }
    var receiveAlerts by remember { mutableStateOf(false) }
    var usePrivateFallback by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Settings & Privacy") },
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
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            SettingToggle("Share Parking Outcome", shareOutcome) { shareOutcome = it }
            SettingToggle("Receive Parking Alerts", receiveAlerts) { receiveAlerts = it }
            SettingToggle("Use Private Parking Fallback", usePrivateFallback) { usePrivateFallback = it }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = "Performance", tint = MaterialTheme.colorScheme.onSecondaryContainer) 
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Performance Diagnostics", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                        Text("Status: Optimized Rendering Active (60fps Map)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Legal Disclaimer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("CurbFlow AI provides probability-based parking guidance using recent mobility signals. It does not guarantee public street parking availability and does not reserve or sell public road space.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
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
