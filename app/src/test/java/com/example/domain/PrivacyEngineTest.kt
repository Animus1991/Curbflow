package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyEngineTest {

    private val testKey = ByteArray(32) { it.toByte() }

    @Test
    fun `anonymizeLocation produces H3-style index`() {
        val h3Index = PrivacyEngine.anonymizeLocation(37.9838, 23.7275)
        assertTrue("Index should start with '8'", h3Index.startsWith("8"))
        assertTrue("Index should be non-trivial length", h3Index.length > 8)
    }

    @Test
    fun `anonymizeLocation is deterministic for same coordinates`() {
        val a = PrivacyEngine.anonymizeLocation(37.9838, 23.7275)
        val b = PrivacyEngine.anonymizeLocation(37.9838, 23.7275)
        assertEquals(a, b)
    }

    @Test
    fun `anonymizeLocation differs for different cells`() {
        val a = PrivacyEngine.anonymizeLocation(37.9838, 23.7275)
        val b = PrivacyEngine.anonymizeLocation(38.5000, 24.0000)
        assertNotEquals(a, b)
    }

    @Test
    fun `encryptLicensePlate round-trips correctly`() {
        val plate = "ABC-1234"
        val encrypted = PrivacyEngine.encryptLicensePlate(plate, testKey)
        val decrypted = PrivacyEngine.decryptLicensePlate(encrypted, testKey)
        assertEquals(plate, decrypted)
    }

    @Test
    fun `encryptLicensePlate never stores plaintext`() {
        val plate = "ABC-1234"
        val encrypted = PrivacyEngine.encryptLicensePlate(plate, testKey)
        assertFalse("Ciphertext must not contain plaintext", encrypted.contains(plate))
        assertNotEquals(plate, encrypted)
    }

    @Test
    fun `encryption produces unique ciphertext per call (random IV)`() {
        val plate = "ABC-1234"
        val e1 = PrivacyEngine.encryptLicensePlate(plate, testKey)
        val e2 = PrivacyEngine.encryptLicensePlate(plate, testKey)
        assertNotEquals("Each encryption must use a fresh IV", e1, e2)
    }

    @Test
    fun `validateEdgePayload rejects PII keys`() {
        assertFalse(PrivacyEngine.validateEdgePayload(mapOf("h3_index" to "8c1", "image" to "blob")))
        assertFalse(PrivacyEngine.validateEdgePayload(mapOf("device_id" to "x")))
        assertTrue(PrivacyEngine.validateEdgePayload(mapOf("h3_index" to "8c1", "confidence" to 0.9)))
    }

    @Test
    fun `consent expires after one year`() {
        val now = System.currentTimeMillis()
        val twoYearsAgo = now - (2L * 365 * 24 * 60 * 60 * 1000)
        assertFalse(PrivacyEngine.isConsentValid(twoYearsAgo, 1, 1))
        assertTrue(PrivacyEngine.isConsentValid(now, 1, 1))
    }

    @Test
    fun `consent invalid for outdated version`() {
        val now = System.currentTimeMillis()
        assertFalse(PrivacyEngine.isConsentValid(now, 1, 2))
        assertTrue(PrivacyEngine.isConsentValid(now, 2, 2))
    }

    @Test
    fun `TTL for real-time events is 20 minutes`() {
        val before = System.currentTimeMillis()
        val ttl = PrivacyEngine.calculateTTL(PrivacyEngine.DataCategory.REAL_TIME_EVENT)
        val expected = before + 20 * 60 * 1000L
        assertTrue("TTL should be ~20 min ahead", ttl in expected..(expected + 5000))
    }
}
