package com.example.myapplication.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

object AuthPrefsKeys {
    val TOKEN = stringPreferencesKey("auth_token")
    val EMAIL = stringPreferencesKey("user_email")
    val NAME = stringPreferencesKey("user_name")
}

data class AuthState(val token: String?, val email: String?, val name: String?)

class AuthDataStore(private val context: Context) {

    val authFlow: Flow<AuthState> = context.authDataStore.data.map { prefs: Preferences ->
        AuthState(
            token = prefs[AuthPrefsKeys.TOKEN],
            email = prefs[AuthPrefsKeys.EMAIL],
            name = prefs[AuthPrefsKeys.NAME]
        )
    }

    suspend fun saveAuth(token: String, email: String, name: String?) {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefsKeys.TOKEN] = token
            prefs[AuthPrefsKeys.EMAIL] = email
            name?.let { prefs[AuthPrefsKeys.NAME] = it }
        }
    }

    suspend fun clearAuth() {
        context.authDataStore.edit { prefs ->
            prefs.remove(AuthPrefsKeys.TOKEN)
            prefs.remove(AuthPrefsKeys.EMAIL)
            prefs.remove(AuthPrefsKeys.NAME)
        }
    }
}

