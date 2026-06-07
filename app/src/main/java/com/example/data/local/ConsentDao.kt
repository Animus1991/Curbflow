package com.example.data.local

import androidx.room.*
import com.example.data.UserConsent

@Dao
interface ConsentDao {
    @Query("SELECT * FROM user_consents WHERE userId = :userId AND isValid = 1")
    suspend fun getValidConsents(userId: String): List<UserConsent>

    @Query("SELECT * FROM user_consents WHERE userId = :userId AND scope = :scope AND isValid = 1 LIMIT 1")
    suspend fun getConsentForScope(userId: String, scope: String): UserConsent?

    @Upsert
    suspend fun upsert(consent: UserConsent)

    @Query("UPDATE user_consents SET isValid = 0 WHERE userId = :userId AND scope = :scope")
    suspend fun revokeConsent(userId: String, scope: String)

    @Query("UPDATE user_consents SET isValid = 0 WHERE userId = :userId")
    suspend fun revokeAllConsents(userId: String)

    @Query("DELETE FROM user_consents WHERE grantedAt < :cutoff")
    suspend fun deleteExpiredConsents(cutoff: Long)
}
