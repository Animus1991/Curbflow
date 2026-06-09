package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Real voice guidance using the Android TextToSpeech engine.
 *
 * Production features:
 * - Lazy initialization with readiness state
 * - QUEUE_FLUSH for urgent messages (reroute), QUEUE_ADD for sequential guidance
 * - Graceful no-op if TTS engine unavailable on device
 * - Must call [shutdown] when the owning screen is disposed to free resources
 */
class VoiceGuidanceEngine(context: Context) {

    private var isReady = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    /**
     * Speak a guidance message. Urgent messages (e.g. reroute alerts)
     * interrupt the current utterance; normal guidance queues up.
     */
    fun speak(message: String, urgent: Boolean = false) {
        if (!isReady) return
        val queueMode = if (urgent) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(message, queueMode, null, message.hashCode().toString())
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
