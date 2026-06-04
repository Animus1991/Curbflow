package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.example.data.ParkingZone
import com.example.ui.theme.BrandCyan
import androidx.compose.ui.graphics.Brush
import kotlin.random.Random

@Composable
fun ZoneTrendChart(zone: ParkingZone) {
    // Generate mock historical data (24 hours) based on current probability
    val chartData = remember(zone.id) {
        val baseProb = zone.probability.toFloat() * 100f
        val entries = (0..23).map { hour ->
            val randomFluctuation = Random.nextFloat() * 40f - 20f // +/- 20%
            val prob = (baseProb + randomFluctuation).coerceIn(0f, 100f)
            FloatEntry(x = hour.toFloat(), y = prob)
        }
        entryModelOf(entries)
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text("24h Availability Trends (Occupancy %)", style = MaterialTheme.typography.titleMedium, color = BrandCyan, modifier = Modifier.padding(bottom = 8.dp))
        
        ProvideChartStyle {
            val backgroundBrush = Brush.verticalGradient(
                colors = listOf(BrandCyan.copy(alpha = 0.4f), Color.Transparent)
            )
            val lineChart = lineChart(
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = BrandCyan.toArgb(),
                        lineBackgroundShader = DynamicShaders.fromBrush(brush = backgroundBrush)
                    )
                )
            )
            
            Chart(
                chart = lineChart,
                model = chartData,
                startAxis = rememberStartAxis(title = "Probability %"),
                bottomAxis = rememberBottomAxis(title = "Hour"),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
    }
}
