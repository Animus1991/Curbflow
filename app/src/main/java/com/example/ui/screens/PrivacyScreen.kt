package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("TRUST & PRIVACY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeonCyan) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                "Our Commitment", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )
            
            Text(
                "Curbflow is engineered to protect your identity while improving urban mobility. We use zero-knowledge principles for data aggregation.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CDLCard {
                PrivacyRow(
                    icon = Icons.Default.VisibilityOff,
                    title = "Anonymized Signals",
                    description = "Your location is never linked to your identity. All data is blurred before processing.",
                    color = NeonBlue
                )
            }

            CDLCard {
                PrivacyRow(
                    icon = Icons.Default.Fingerprint,
                    title = "No Personal Data",
                    description = "We don't store names, emails, or phone numbers. Your device is just a node in the network.",
                    color = EmeraldLive
                )
            }

            CDLCard {
                PrivacyRow(
                    icon = Icons.Default.GppGood,
                    title = "Open Verification",
                    description = "Our prediction models are based on public mobility science and verifiable sensors.",
                    color = NeonCyan
                )
            }
        }
    }
}
