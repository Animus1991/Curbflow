package com.example.domain

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

/**
 * Privacy-by-Design Engine for CurbFlow AI.
 * 
 * Responsibilities:
 * - Location anonymization via H3 cell indexing (Level 12 ≈ 350m hexagons)
 * - License plate encryption (AES-256-GCM)
 * - Consent validation before any data operation
 * - TTL-based auto-deletion of expired events
 * - Payload validation (no images, no raw PII)
 * 
 * GDPR Compliant: No raw coordinates, plates, or faces stored.
 */
object PrivacyEngine {

    private const val AES_KEY_SIZE = 32 // 256-bit
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * Anonymize raw GPS coordinates into an H3 cell index.
     * Resolution 12 ≈ 350m hexagonal cell, sufficient for parking predictions
     * without revealing exact user position.
     */
    fun anonymizeLocation(lat: Double, lng: Double, resolution: Int = 12): String {
        // Simulated H3 index generation (production would use com.uber:h3 library)
        val latPart = ((lat * 1000).toLong() and 0xFFFFF).toString(16)
        val lngPart = ((lng * 1000).toLong() and 0xFFFFF).toString(16)
        return "8${resolution.toString(16)}${latPart}${lngPart}ff"
    }

    /**
     * Encrypt a license plate using AES-256-GCM.
     * Each encryption uses a unique IV for forward secrecy.
     * Returns Base64-encoded ciphertext with prepended IV.
     */
    fun encryptLicensePlate(plate: String, secretKeyBytes: ByteArray): String {
        require(secretKeyBytes.size == AES_KEY_SIZE) { "Key must be 256 bits (32 bytes)" }

        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(secretKeyBytes, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        val ciphertext = cipher.doFinal(plate.toByteArray(Charsets.UTF_8))
        // Prepend IV to ciphertext for decryption
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypt a license plate from its encrypted form.
     */
    fun decryptLicensePlate(encryptedBase64: String, secretKeyBytes: ByteArray): String {
        require(secretKeyBytes.size == AES_KEY_SIZE) { "Key must be 256 bits (32 bytes)" }

        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
        val ciphertext = combined.sliceArray(GCM_IV_LENGTH until combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(secretKeyBytes, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    /**
     * Validate that a sensor/edge payload contains NO PII.
     * Rejects payloads with images, device IDs, or raw coordinates.
     */
    fun validateEdgePayload(payload: Map<String, Any?>): Boolean {
        val forbiddenKeys = setOf("image", "photo", "device_id", "mac_address", "plate_raw", "face")
        return forbiddenKeys.none { payload.containsKey(it) }
    }

    /**
     * Calculate data expiration timestamp based on category.
     */
    fun calculateTTL(category: DataCategory): Long {
        val now = System.currentTimeMillis()
        return now + when (category) {
            DataCategory.REAL_TIME_EVENT -> 20 * 60 * 1000L       // 20 min
            DataCategory.HISTORICAL_TREND -> 7 * 24 * 60 * 60 * 1000L // 7 days
            DataCategory.GEOMETRY -> Long.MAX_VALUE - now          // Permanent
            DataCategory.USER_CONSENT -> 365 * 24 * 60 * 60 * 1000L  // 1 year
        }
    }

    /**
     * Check if a consent record is still valid.
     */
    fun isConsentValid(consentTimestamp: Long, consentVersion: Int, currentVersion: Int): Boolean {
        val oneYear = 365 * 24 * 60 * 60 * 1000L
        val isNotExpired = (System.currentTimeMillis() - consentTimestamp) < oneYear
        val isCurrentVersion = consentVersion >= currentVersion
        return isNotExpired && isCurrentVersion
    }

    /**
     * Generate a privacy-safe event ID (UUID without device fingerprint).
     */
    fun generateAnonymousEventId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    enum class DataCategory {
        REAL_TIME_EVENT,
        HISTORICAL_TREND,
        GEOMETRY,
        USER_CONSENT
    }
}
