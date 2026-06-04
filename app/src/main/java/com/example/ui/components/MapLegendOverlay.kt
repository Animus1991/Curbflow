package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MapLegendOverlay(
    modifier: Modifier = Modifier,
    heatmapType: HeatmapType
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (heatmapType == HeatmapType.PROBABILITY) "Availability" else "Price Demand",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            if (heatmapType == HeatmapType.PROBABILITY) {
                LegendRow(color = Color(0, 230, 118), label = "High (>60%)")
                LegendRow(color = Color(255, 171, 0), label = "Medium (30%-60%)")
                LegendRow(color = Color(255, 23, 68), label = "Low (<30%)")
            } else {
                LegendRow(color = Color(255, 0, 0), label = "Expensive")
                LegendRow(color = Color(255, 255, 0), label = "Moderate")
                LegendRow(color = Color(0, 191, 255), label = "Cheap")
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
