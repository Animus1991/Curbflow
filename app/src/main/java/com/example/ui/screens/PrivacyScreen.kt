package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.LegalDisclaimerCard
import com.example.ui.components.PrivacyByDesignCard
import com.example.ui.components.SafetyBanner
import com.example.ui.theme.StrongText

@Composable
fun PrivacyScreen(navController: androidx.navigation.NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Privacy & Legal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        
        PrivacyByDesignCard()
        
        LegalDisclaimerCard()
        
        SafetyBanner()
    }
}
