package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore

class EncryptedAuthStorage(context: Context) : AuthStorage {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ADMIN = "is_admin"
        private const val TAG = "EncryptedAuthStorage"
    }

    private val prefs: SharedPreferences = createPrefs(context)

    private fun createPrefs(context: Context): SharedPreferences {
        fun buildMasterKey(): MasterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        fun buildPrefs(masterKey: MasterKey): SharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        return runCatching { buildPrefs(buildMasterKey()) }
            .recoverCatching { err ->
                Log.w(TAG, "Encrypted prefs corrupted, resetting", err)
                resetCorruptedStore(context)
                buildPrefs(buildMasterKey())
            }
            .getOrThrow()
    }

    private fun resetCorruptedStore(context: Context) {
        context.deleteSharedPreferences(PREFS_NAME)
        context.deleteFile("${PREFS_NAME}__androidx_security_crypto_encrypted_prefs_key_keyset__")
        context.deleteFile("${PREFS_NAME}__androidx_security_crypto_encrypted_prefs_value_keyset__")
        runCatching {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        }.onFailure { Log.w(TAG, "Failed to delete master key", it) }
    }

    override fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    override fun saveUserInfo(info: StoredUserInfo) {
        prefs.edit()
            .putString(KEY_USER_ID, info.id)
            .putString(KEY_USER_EMAIL, info.email)
            .putString(KEY_USER_NAME, info.displayName)
            .putBoolean(KEY_USER_ADMIN, info.isAdmin)
            .apply()
    }

    override fun getUserInfo(): StoredUserInfo? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, null)
        val isAdmin = prefs.getBoolean(KEY_USER_ADMIN, false)
        return StoredUserInfo(id, email, name, isAdmin)
    }

    override fun clearUserInfo() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_ADMIN)
            .apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }
}
