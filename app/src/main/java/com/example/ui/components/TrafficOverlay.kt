package com.example.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class TrafficOverlay : Overlay() {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
    }
    private val p1 = Point()
    private val p2 = Point()

    // Mock traffic segments around Athens center
    private val mockTrafficSegments = listOf(
        Pair(GeoPoint(37.975, 23.73), GeoPoint(37.971, 23.725)) to android.graphics.Color.RED,
        Pair(GeoPoint(37.971, 23.725), GeoPoint(37.968, 23.722)) to android.graphics.Color.YELLOW,
        Pair(GeoPoint(37.976, 23.72), GeoPoint(37.972, 23.728)) to android.graphics.Color.argb(200, 255, 69, 0), // Orange
        Pair(GeoPoint(37.965, 23.735), GeoPoint(37.970, 23.730)) to android.graphics.Color.RED
    )

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection

        mockTrafficSegments.forEach { (segment, color) ->
            projection.toPixels(segment.first, p1)
            projection.toPixels(segment.second, p2)

            paint.color = color
            canvas.drawLine(p1.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat(), paint)
        }
    }
}
