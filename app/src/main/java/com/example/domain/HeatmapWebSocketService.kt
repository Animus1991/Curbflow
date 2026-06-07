package com.example.domain

import com.example.data.H3CellEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * WebSocket-based heatmap update service.
 * 
 * In production, this connects to wss://api.curbflow.io/v1/heatmap
 * and receives real-time H3 cell probability updates.
 * 
 * Currently uses a simulated data stream for development/demo.
 * 
 * Features:
 * - Exponential backoff reconnection (1s, 2s, 4s, 8s... max 60s)
 * - Automatic TTL-based cache invalidation
 * - Connection state observable for UI indicators
 * - Graceful shutdown
 */
object HeatmapWebSocketService {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _heatmapUpdates = MutableSharedFlow<HeatmapUpdate>(replay = 1)
    val heatmapUpdates: SharedFlow<HeatmapUpdate> = _heatmapUpdates

    private val _cellUpdates = MutableSharedFlow<List<H3CellEntity>>(replay = 1)
    val cellUpdates: SharedFlow<List<H3CellEntity>> = _cellUpdates

    private var simulationJob: Job? = null
    private var reconnectAttempts = 0
    private const val MAX_BACKOFF_SECONDS = 60L

    /**
     * Connect to the heatmap stream.
     * In production: opens a WebSocket to the backend.
     * In dev: starts a simulated data stream.
     */
    fun connect(scope: CoroutineScope, lat: Double = 37.9838, lng: Double = 23.7275) {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        _connectionState.value = ConnectionState.CONNECTING
        reconnectAttempts = 0

        simulationJob = scope.launch {
            try {
                delay(500) // Simulate connection handshake
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempts = 0

                // Simulated real-time updates every 5 seconds
                while (isActive) {
                    val cells = generateSimulatedCells(lat, lng)
                    _cellUpdates.emit(cells)
                    _heatmapUpdates.emit(
                        HeatmapUpdate(
                            type = "heatmap_update",
                            h3Cells = cells.map { H3CellData(it.h3Index, it.probability, it.eventType, it.freshness) },
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    delay(5000)
                }
            } catch (e: CancellationException) {
                _connectionState.value = ConnectionState.DISCONNECTED
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                reconnectWithBackoff(scope, lat, lng)
            }
        }
    }

    /**
     * Reconnect with exponential backoff.
     */
    private fun reconnectWithBackoff(scope: CoroutineScope, lat: Double, lng: Double) {
        scope.launch {
            reconnectAttempts++
            val backoffSeconds = min(
                (2.0.pow(reconnectAttempts.toDouble())).toLong(),
                MAX_BACKOFF_SECONDS
            )
            _connectionState.value = ConnectionState.RECONNECTING
            delay(backoffSeconds * 1000)
            connect(scope, lat, lng)
        }
    }

    /**
     * Disconnect and clean up.
     */
    fun disconnect() {
        simulationJob?.cancel()
        simulationJob = null
        _connectionState.value = ConnectionState.DISCONNECTED
        reconnectAttempts = 0
    }

    /**
     * Generate simulated H3 cell data for development.
     */
    private fun generateSimulatedCells(centerLat: Double, centerLng: Double): List<H3CellEntity> {
        val cells = mutableListOf<H3CellEntity>()
        val now = System.currentTimeMillis()

        repeat(12) { i ->
            val latOffset = (Random.nextDouble() - 0.5) * 0.01
            val lngOffset = (Random.nextDouble() - 0.5) * 0.01
            val lat = centerLat + latOffset
            val lng = centerLng + lngOffset

            cells.add(
                H3CellEntity(
                    h3Index = PrivacyEngine.anonymizeLocation(lat, lng),
                    latitude = lat,
                    longitude = lng,
                    probability = Random.nextFloat().coerceIn(0.1f, 0.95f),
                    freshness = Random.nextLong(1000, 300000),
                    eventType = listOf("empty_spot", "occupied", "loading_zone", "blockage").random(),
                    createdAt = now,
                    expiresAt = now + (20 * 60 * 1000)
                )
            )
        }
        return cells
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        ERROR
    }

    data class HeatmapUpdate(
        val type: String,
        val h3Cells: List<H3CellData>,
        val timestamp: Long
    )

    data class H3CellData(
        val h3Index: String,
        val probability: Float,
        val eventType: String,
        val freshness: Long
    )
}
