package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.components.CDLCard
import com.example.ui.components.PrivacyRow
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        containerColor = NavyDeep,
        topBar = {
            TopAppBar(
                title = { Text("HELP CENTER", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeonCyan) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDeep)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "How to use CurbFlow", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )

            CDLCard {
                PrivacyRow(
                    icon = Icons.Default.Info,
                    title = "Reading the Map",
                    description = "Green pulses indicate high probability zones. Red pulses suggest the area is likely full or restricted.",
                    color = EmeraldLive
                )
            }

            CDLCard {
                PrivacyRow(
                    icon = Icons.Default.HelpCenter,
                    title = "Decision Hub",
                    description = "Tap any pulse to open the decision cockpit. From there you can initiate guidance or report what you found.",
                    color = NeonBlue
                )
            }

            CDLCard {
                PrivacyRow(
                    icon = Icons.Default.SupportAgent,
                    title = "Live Support",
                    description = "Our network monitors are always verifying signals. Report outcomes to improve accuracy for everyone.",
                    color = AmberWarning
                )
            }
        }
    }
}
