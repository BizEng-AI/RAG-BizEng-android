package com.example.myapplication.data.local

data class StoredUserInfo(
    val id: String,
    val email: String,
    val displayName: String?,
    val isAdmin: Boolean
)

interface AuthStorage {
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()

    fun saveUserInfo(info: StoredUserInfo)
    fun getUserInfo(): StoredUserInfo?
    fun clearUserInfo()

    fun clearAll()
}

