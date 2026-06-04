package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.data.ParkingZone
import com.example.data.PrivateParking
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CustomFallbackMap : MapProvider {
    @Composable
    override fun RenderMap(
        zones: List<ParkingZone>,
        privateParkings: List<PrivateParking>,
        isDriving: Boolean,
        showHeatmap: Boolean,
        heatmapType: HeatmapType,
        trafficVisible: Boolean,
        dynamicSpots: List<com.example.api.DynamicSpot>,
        spotMarkersVisible: Boolean,
        restrictedZonesVisible: Boolean,
        forecastVisible: Boolean,
        weatherVisible: Boolean,
        userLocation: Pair<Double, Double>?,
        mapCenter: Pair<Double, Double>?,
        onZoneSelected: (ParkingZone) -> Unit
    ) {
        val haptic = LocalHapticFeedback.current
        val scale = if (isDriving) 1.5f else 1f
        
        val pulseState = rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "MapPulse"
        )

        // Precompute the hexagon path once at origin
        val hexPath = remember {
            val hexRadius = 30f
            val path = Path()
            for (i in 0 until 6) {
                val angle = 2.0 * PI / 6 * (i + 0.5)
                val x = hexRadius * cos(angle).toFloat()
                val y = hexRadius * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            path
        }
        
        // Cache colors to avoid creating new objects inside draw loop
        val highProbColor = ProbabilityHigh
        val medProbColor = ProbabilityMedium
        val lowProbColor = ProbabilityLow

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .pointerInput(zones) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        
                        // Scale coordinates linearly
                        // X maps [23.72, 23.78] -> [0.1*w, 0.9*w]
                        // Y maps [37.95, 37.995] -> [0.9*h, 0.1*h]
                        var minDistance = Float.MAX_VALUE
                        var closestZone: ParkingZone? = null
                        
                        zones.forEach { zone ->
                            val lonFraction = (zone.longitude.toFloat() - 23.72f) / 0.06f
                            val latFraction = (37.995f - zone.latitude.toFloat()) / 0.045f
                            val px = w * (0.1f + 0.8f * lonFraction)
                            val py = h * (0.1f + 0.8f * latFraction)
                            
                            val dx = offset.x - px
                            val dy = offset.y - py
                            val dist = dx*dx + dy*dy
                            if(dist < 10000f && dist < minDistance) {
                                minDistance = dist
                                closestZone = zone
                            }
                        }
                        
                        closestZone?.let { zone ->
                            if (zone.probability > 0.6) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            else haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onZoneSelected(zone)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val currentPulse = pulseState.value

            // Draw dark grid lines (roads)
            for (i in 1..9) {
                drawLine(
                    color = Color.DarkGray.copy(alpha = 0.3f),
                    start = Offset(0f, h * i / 10),
                    end = Offset(w, h * i / 10),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.DarkGray.copy(alpha = 0.3f),
                    start = Offset(w * i / 10, 0f),
                    end = Offset(w * i / 10, h),
                    strokeWidth = 2f
                )
            }

            zones.forEach { zone ->
                val probColor = when {
                    zone.probability > 0.6 -> highProbColor
                    zone.probability > 0.3 -> medProbColor
                    else -> lowProbColor
                }

                val lonFraction = (zone.longitude.toFloat() - 23.72f) / 0.06f
                val latFraction = (37.995f - zone.latitude.toFloat()) / 0.045f
                val px = w * (0.1f + 0.8f * lonFraction)
                val py = h * (0.1f + 0.8f * latFraction)

                // Draw pulsing ring
                drawCircle(
                    color = probColor.copy(alpha = 0.3f),
                    radius = 40f * currentPulse,
                    center = Offset(px, py)
                )

                // Draw hexagon using precalculated path
                translate(left = px, top = py) {
                    drawPath(
                        path = hexPath,
                        color = probColor.copy(alpha = 0.5f)
                    )
                    drawPath(
                        path = hexPath,
                        color = probColor,
                        style = Stroke(width = 4f)
                    )
                }
            }
            
            // Draw private parkings
            privateParkings.forEach { p ->
                 val hash = kotlin.math.abs(p.name.hashCode())
                 val lon = 23.72f + (hash % 60) * 0.001f
                 val lat = 37.95f + ((hash ushr 8) % 45) * 0.001f
                 val lonFraction = (lon - 23.72f) / 0.06f
                 val latFraction = (37.995f - lat) / 0.045f
                 val px = w * (0.1f + 0.8f * lonFraction)
                 val py = h * (0.1f + 0.8f * latFraction)
                 
                 drawCircle(
                     color = BrandCyan,
                     radius = 15f,
                     center = Offset(px, py)
                 )
            }
        }
    }
}
