package com.example.api

import android.util.Log
import com.example.domain.SecurityManager
import okhttp3.*

/**
 * Professional WebSocket service for real-time heatmap updates.
 * Ready for the live production endpoint.
 */
class ParkingWebSocketService(private val client: OkHttpClient) {
    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder()
            .url(LIVE_ENDPOINT)
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to live stream")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Receiving: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: " + t.message)
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnect() {
        webSocket?.close(1000, "User logout")
    }

    companion object {
        private const val LIVE_ENDPOINT = "wss://api.curbflow.io/v1/heatmap"

        fun createDefaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .certificatePinner(SecurityManager.getCertificatePinnerConfig())
                .build()
        }
    }
}
