package com.example.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.MotionEvent
import com.example.data.DemandLevel
import com.example.data.ParkingZone
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

enum class HeatmapType {
    PROBABILITY, PRICE
}

class HeatmapOverlay(
    private val zones: List<ParkingZone>,
    private val heatmapType: HeatmapType = HeatmapType.PROBABILITY
) : Overlay() {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }
    private val point = Point()

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val projection = mapView.projection
        val radiusMeters = 250.0f

        zones.forEach { zone ->
            projection.toPixels(GeoPoint(zone.latitude, zone.longitude), point)
            
            val projectedRadius = projection.metersToPixels(radiusMeters)
            if (projectedRadius <= 0) return@forEach

            val centerColor = if (heatmapType == HeatmapType.PROBABILITY) {
                when {
                    zone.probability > 0.6 -> android.graphics.Color.argb(200, 0, 230, 118) // High probability - Green
                    zone.probability > 0.3 -> android.graphics.Color.argb(200, 255, 171, 0)  // Medium - Orange
                    else -> android.graphics.Color.argb(200, 255, 23, 68)             // Low - Red
                }
            } else {
                // Mocking a price derived from demand
                val pricePerHour = when (zone.demandLevel) {
                    DemandLevel.EXTREME -> 4.5
                    DemandLevel.HIGH -> 3.5
                    DemandLevel.MEDIUM -> 2.0
                    DemandLevel.LOW -> 0.5 // Cheap
                }
                when {
                    pricePerHour > 4.0 -> android.graphics.Color.argb(200, 255, 0, 0)      // Expensive - Red
                    pricePerHour > 2.0 -> android.graphics.Color.argb(200, 255, 255, 0)    // Moderate - Yellow
                    else -> android.graphics.Color.argb(200, 0, 191, 255)                  // Cheap - Blue
                }
            }
            val edgeColor = android.graphics.Color.TRANSPARENT

            paint.shader = RadialGradient(
                point.x.toFloat(),
                point.y.toFloat(),
                projectedRadius,
                centerColor,
                edgeColor,
                Shader.TileMode.CLAMP
            )

            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), projectedRadius, paint)
        }
    }
}
