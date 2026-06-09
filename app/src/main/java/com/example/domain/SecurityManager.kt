package com.example.domain

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * SecurityManager — Centralized security configuration for CurbFlow AI.
 * 
 * Responsibilities:
 * - Secure token/key storage encrypted with an **Android Keystore-backed** AES-256 key
 *   (hardware-backed where available; key material never leaves the secure element)
 * - Certificate pinning configuration for OkHttp
 * - Secure random key generation
 * - Anonymous device ID (no hardware fingerprinting)
 */
object SecurityManager {

    private const val SECURE_PREFS_NAME = "curbflow_secure_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_SECRET = "user_secret_key"
    private const val KEY_DEVICE_ID = "device_anonymous_id"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val KEYSTORE_ALIAS = "curbflow_master_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    /**
     * Get or create the AES-256 master key in the Android Keystore.
     * Key material is generated and stored inside the secure hardware/TEE —
     * it is never exposed to the app process.
     */
    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun getSecurePrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        val iv = cipher.iv // Keystore generates the IV
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
        val ciphertext = combined.sliceArray(GCM_IV_LENGTH until combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateMasterKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    /**
     * Store an auth token securely (encrypted).
     */
    fun storeAuthToken(context: Context, token: String) {
        getSecurePrefs(context).edit().putString(KEY_AUTH_TOKEN, encrypt(token)).apply()
    }

    /**
     * Retrieve the auth token (decrypted).
     */
    fun getAuthToken(context: Context): String? {
        val encrypted = getSecurePrefs(context).getString(KEY_AUTH_TOKEN, null) ?: return null
        return try { decrypt(encrypted) } catch (_: Exception) { null }
    }

    /**
     * Store the user's encryption secret key (for license plate encryption).
     */
    fun storeUserSecretKey(context: Context, key: ByteArray) {
        val encoded = Base64.encodeToString(key, Base64.NO_WRAP)
        getSecurePrefs(context).edit().putString(KEY_USER_SECRET, encrypt(encoded)).apply()
    }

    /**
     * Retrieve the user's encryption secret key.
     */
    fun getUserSecretKey(context: Context): ByteArray? {
        val encrypted = getSecurePrefs(context).getString(KEY_USER_SECRET, null) ?: return null
        return try {
            Base64.decode(decrypt(encrypted), Base64.NO_WRAP)
        } catch (_: Exception) { null }
    }

    /**
     * Get or generate an anonymous device ID (UUID-based, no hardware fingerprint).
     */
    fun getAnonymousDeviceId(context: Context): String {
        val prefs = getSecurePrefs(context)
        return prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val newId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
            newId
        }
    }

    /**
     * Generate a random 256-bit key for encryption operations.
     */
    fun generateSecretKey(): ByteArray {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        return key
    }

    /**
     * Clear all stored secrets (e.g., on logout).
     */
    fun clearAllSecrets(context: Context) {
        getSecurePrefs(context).edit().clear().apply()
    }

    /**
     * OkHttp Certificate Pinning configuration.
     * In production, pins would be set for api.curbflow.io.
     */
    fun getCertificatePinnerConfig(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.curbflow.io", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .add("api.curbflow.io", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            .build()
    }

    /**
     * Checks if the device environment is secure (basic integrity check placeholder).
     * In production, integrate Google Play Integrity API.
     */
    fun isEnvironmentSecure(): Boolean {
        return true
    }

    /**
     * Create a secure OkHttp client with certificate pinning and timeout configuration.
     */
    fun createSecureOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(getCertificatePinnerConfig())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
