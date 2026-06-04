package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandCyan
import kotlin.math.sin

@Composable
fun ProbabilityChart(modifier: Modifier = Modifier) {
    val curveColor = BrandCyan
    Canvas(modifier = modifier.fillMaxWidth().height(100.dp)) {
        val width = size.width
        val height = size.height
        val maxPoints = 24
        
        val points = buildList {
            for (i in 0 until maxPoints) {
                // Generate a mock probability wave
                val probability = 0.5 + 0.3 * sin((i / 24f) * Math.PI * 2) + Math.random() * 0.1
                add(probability.toFloat().coerceIn(0f, 1f))
            }
        }

        val path = Path()
        points.forEachIndexed { index, prop ->
            val x = (index.toFloat() / (maxPoints - 1)) * width
            val y = height - (prop * height) // Invert Y since 0 is top
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = curveColor,
            style = Stroke(width = 4f)
        )
        
        // Draw grid lines
        for (i in 1..4) {
            val y = height * (i / 4f)
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 2f
            )
        }
    }
}
