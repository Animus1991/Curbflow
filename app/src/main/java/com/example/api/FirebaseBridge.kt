package com.example.api

import android.util.Log

/**
 * A dormant infrastructure layer for Firebase integration.
 * Currently uses mock implementations but is wired across the app
 * for instant activation in the future.
 */
object FirebaseBridge {
    private const val TAG = "FirebaseBridge"
    
    // Toggle this when you decide to decide to connect the real Firebase
    private const val IS_FIREBASE_ENABLED = false

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        if (IS_FIREBASE_ENABLED) {
            // Real Firebase analytics call would go here
            // FirebaseAnalytics.getInstance(context).logEvent(name, bundle)
            Log.d(TAG, "Logging Firebase Event: $name | Params: $params")
        } else {
            Log.d(TAG, "[MOCK] Firebase Event: $name")
        }
    }

    fun getRemoteConfigValue(key: String, defaultValue: String): String {
        if (IS_FIREBASE_ENABLED) {
            // Real RemoteConfig fetch
            return defaultValue
        }
        return defaultValue
    }

    /**
     * Placeholder for Firebase Cloud Messaging token retrieval.
     */
    fun getFcmToken(onComplete: (String?) -> Unit) {
        if (IS_FIREBASE_ENABLED) {
            // FirebaseMessaging.getInstance().token.addOnCompleteListener { ... }
        } else {
            onComplete("mock_fcm_token_ready")
        }
    }
}
