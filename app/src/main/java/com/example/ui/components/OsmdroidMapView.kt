package com.example.ui.components

import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ParkingZone
import com.example.data.PrivateParking
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import com.example.ui.theme.LocalThemeManager

object OsmdroidMapView : MapProvider {
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
        val context = LocalContext.current
        
        // Initialize osmdroid configuration before creating MapView
        remember {
            Configuration.getInstance().apply {
                load(context, PreferenceManager.getDefaultSharedPreferences(context))
                userAgentValue = context.packageName
            }
            true // Dummy value just to run once
        }

        val isDarkTheme = LocalThemeManager.current.isDarkTheme

        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
                controller.setZoom(if (isDriving) 16.0 else 14.5)
                controller.setCenter(GeoPoint(37.9715, 23.7267))
            }
        }

        // Apply theme color filter inside update or during creation, but remember doesn't react to it if we don't re-apply
        // Better to apply in factory and update


        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                if (isDarkTheme) {
                    val inverseMatrix = android.graphics.ColorMatrix().apply {
                        set(floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                        ))
                    }
                    val tintMatrix = android.graphics.ColorMatrix().apply {
                        set(floatArrayOf(
                            0.8f, 0f, 0f, 0f, 0f,
                            0f, 0.9f, 0f, 0f, 0f,
                            0f, 0f, 1.1f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))
                    }
                    inverseMatrix.postConcat(tintMatrix)
                    view.overlayManager.tilesOverlay.setColorFilter(android.graphics.ColorMatrixColorFilter(inverseMatrix))
                } else {
                    view.overlayManager.tilesOverlay.setColorFilter(null)
                }

                view.overlays.clear()
                view.overlays.add(view.overlayManager.tilesOverlay)

                // Add the heatmap overlay
                if (showHeatmap) {
                    view.overlays.add(HeatmapOverlay(zones, heatmapType))
                }

                if (trafficVisible) {
                    view.overlays.add(TrafficOverlay())
                }
                
                userLocation?.let { (lat, lon) ->
                    val userMarker = Marker(view).apply {
                        position = GeoPoint(lat, lon)
                        title = "Your Location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
                    }
                    view.overlays.add(userMarker)
                }

                // Animate to mapCenter if provided, otherwise userLocation
                val targetCenter = mapCenter ?: userLocation
                targetCenter?.let { (lat, lon) ->
                    view.controller.animateTo(GeoPoint(lat, lon))
                }

                zones.forEach { zone ->
                    val marker = Marker(view).apply {
                        position = GeoPoint(zone.latitude, zone.longitude)
                        title = zone.name
                        
                        val forecastStr = if (forecastVisible) {
                            val forecastProb = (zone.probability * 0.8 + 0.1).coerceIn(0.0, 1.0)
                            " | 4h Forecast: ${(forecastProb * 100).toInt()}%"
                        } else ""
                        snippet = "Probability: ${(zone.probability * 100).toInt()}% • ETA ${zone.expectedTimeToParkMinutes}m$forecastStr"
                        
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        // Make the marker barely visible or completely invisible so it is just a click target,
                        // or we can just keep it visible as a pin.
                        // Let's keep it as an invisible touch target over the heatmap core.
                        alpha = 0.0f
                        setOnMarkerClickListener { _, _ ->
                            onZoneSelected(zone)
                            // center nicely
                            view.controller.animateTo(GeoPoint(zone.latitude, zone.longitude))
                            true
                        }
                    }
                    view.overlays.add(marker)
                }
                
                // Add transit stations
                val transitStations = listOf(
                    Pair("Syntagma Metro", GeoPoint(37.9755, 23.7348)),
                    Pair("Monastiraki Metro", GeoPoint(37.9761, 23.7258)),
                    Pair("Syngrou Fix Metro", GeoPoint(37.9644, 23.7262)),
                    Pair("Evangelismos Metro", GeoPoint(37.9760, 23.7460))
                )
                transitStations.forEach { (name, point) ->
                    val marker = Marker(view).apply {
                        position = point
                        title = name
                        snippet = "Alternative Commuting Option"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = context.getDrawable(android.R.drawable.ic_menu_directions) // Train/bus icon approximation
                    }
                    view.overlays.add(marker)
                }

                privateParkings.forEach { p ->
                    val hash = kotlin.math.abs(p.name.hashCode())
                    val lon = 23.72f + (hash % 60) * 0.001f
                    val lat = 37.95f + ((hash ushr 8) % 45) * 0.001f
                    
                    val marker = Marker(view).apply {
                        position = GeoPoint(lat.toDouble(), lon.toDouble())
                        title = p.name
                        snippet = "Private Garage • Available"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        // Use a distinct icon for private parking if available, utilizing default for now
                    }
                    view.overlays.add(marker)
                }

                if (spotMarkersVisible) {
                    val clusterOverlay = object : org.osmdroid.views.overlay.Overlay() {
                        override fun draw(canvas: android.graphics.Canvas?, view: MapView?, shadow: Boolean) {
                            if (shadow || canvas == null || view == null) return
                            val zoom = view.zoomLevelDouble
                            val projection = view.projection
                            
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLUE
                                alpha = 180
                                style = android.graphics.Paint.Style.FILL
                                isAntiAlias = true
                            }
                            
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            
                            if (zoom < 15.5) {
                                // Group into 1 big cluster for simplicity (mock dynamic clustering)
                                if (dynamicSpots.isNotEmpty()) {
                                    val avgLat = dynamicSpots.map { it.latitude }.average()
                                    val avgLon = dynamicSpots.map { it.longitude }.average()
                                    val point = projection.toPixels(GeoPoint(avgLat, avgLon), null)
                                    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 60f, paint)
                                    canvas.drawText("${dynamicSpots.size}", point.x.toFloat(), point.y.toFloat() + 10f, textPaint)
                                }
                            } else {
                                // Draw individual spots
                                dynamicSpots.forEach { spot ->
                                    val point = projection.toPixels(GeoPoint(spot.latitude, spot.longitude), null)
                                    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 20f, paint)
                                }
                            }
                        }
                    }
                    view.overlays.add(clusterOverlay)
                }

                if (restrictedZonesVisible) {
                    // Draw a mock restricted zone
                    val restrictedPoly = Polygon().apply {
                        points = listOf(
                            GeoPoint(37.973, 23.725),
                            GeoPoint(37.973, 23.728),
                            GeoPoint(37.971, 23.728),
                            GeoPoint(37.971, 23.725)
                        )
                        fillPaint.color = android.graphics.Color.argb(50, 255, 0, 0)
                        outlinePaint.color = android.graphics.Color.RED
                        outlinePaint.strokeWidth = 2f
                        title = "Restricted Zone (No Parking)"
                    }
                    view.overlays.add(restrictedPoly)
                }

                if (weatherVisible) {
                    val weatherOverlay = object : org.osmdroid.views.overlay.Overlay() {
                        override fun draw(canvas: android.graphics.Canvas?, view: MapView?, shadow: Boolean) {
                            if (shadow || canvas == null || view == null) return
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(70, 0, 0, 0) // Dimming overlay for rain
                                style = android.graphics.Paint.Style.FILL
                            }
                            canvas.drawRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), paint)

                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 60f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                            }
                            canvas.drawText("🌧️ HEAVY RAIN - Low Visibility", view.width / 2f, view.height / 5f, textPaint)
                        }
                    }
                    view.overlays.add(weatherOverlay)
                }

                view.invalidate()
            }
        )
        
        DisposableEffect(Unit) {
            onDispose {
                mapView.onDetach()
            }
        }
    }
}
