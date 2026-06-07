package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandCyan
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun PredictiveProbabilityChart(
    historicalData: List<Double>,
    forecastData: List<Double>,
    modifier: Modifier = Modifier
) {
    val entries = remember(historicalData, forecastData) {
        val allData = historicalData + forecastData
        allData.mapIndexed { index, value -> entryOf(index, value.toFloat()) }
    }
    
    val entryModel = remember(entries) { entryModelOf(entries) }

    Chart(
        chart = lineChart(),
        model = entryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    )
}
