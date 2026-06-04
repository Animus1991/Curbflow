package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.ZoneCard
import com.example.ui.theme.StrongText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneListScreen(navController: NavController, viewModel: MapViewModel = viewModel()) {
    val zones by viewModel.zones.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Search Areas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) { 
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(zones, key = { it.id }) { zone ->
                ZoneCard(
                    zoneName = zone.name,
                    area = zone.area,
                    probability = zone.probability,
                    confidence = zone.confidence,
                    freshnessMinutes = zone.freshnessMinutes,
                    expectedTimeToPark = "${zone.expectedTimeToParkMinutes}m",
                    walkingTime = "5m", // Mocked, would come from engine
                    legalRisk = zone.legalRisk.name.lowercase().replaceFirstChar { it.uppercase() },
                    recommendationScore = "${(zone.probability * 10).toInt()}/10",
                    modifier = Modifier.clickable { navController.navigate("route/${zone.id}") }
                )
            }
        }
    }
}
