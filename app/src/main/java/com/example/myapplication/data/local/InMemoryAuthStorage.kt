package com.example.myapplication.data.local

class InMemoryAuthStorage : AuthStorage {

    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var userInfo: StoredUserInfo? = null

    override fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override fun getAccessToken(): String? = accessToken

    override fun getRefreshToken(): String? = refreshToken

    override fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    override fun saveUserInfo(info: StoredUserInfo) {
        userInfo = info
    }

    override fun getUserInfo(): StoredUserInfo? = userInfo

    override fun clearUserInfo() {
        userInfo = null
    }

    override fun clearAll() {
        clearTokens()
        clearUserInfo()
    }
}

