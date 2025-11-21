package com.example.myapplication.data.local

class AuthManager(
    private val storage: AuthStorage
) {

    fun saveTokens(accessToken: String, refreshToken: String) {
        storage.saveTokens(accessToken, refreshToken)
    }

    fun saveUserInfo(userId: String, email: String, displayName: String?, isAdmin: Boolean) {
        storage.saveUserInfo(StoredUserInfo(userId, email, displayName, isAdmin))
    }

    fun getAccessToken(): String? = storage.getAccessToken()

    fun getRefreshToken(): String? = storage.getRefreshToken()

    fun getUserId(): String? = storage.getUserInfo()?.id

    fun getUserEmail(): String? = storage.getUserInfo()?.email

    fun getUserName(): String? = storage.getUserInfo()?.displayName

    fun isAdmin(): Boolean = storage.getUserInfo()?.isAdmin ?: false

    fun isLoggedIn(): Boolean {
        val at = getAccessToken()
        val rt = getRefreshToken()
        return !at.isNullOrBlank() && !rt.isNullOrBlank()
    }

    fun clearTokens() {
        storage.clearAll()
    }
}
