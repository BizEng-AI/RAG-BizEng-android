package com.example.myapplication.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================================
// AUTHENTICATION DTOs
// ============================================================================

@Serializable
data class RegisterReq(
    val email: String,
    val password: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("group_number") val groupNumber: String? = null
)

@Serializable
data class LoginReq(
    val email: String,
    val password: String
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserDto? = null  // Optional - present in login/register, not in refresh
) {
    // Validation helper
    fun isValid(): Boolean = !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()

    fun getValidatedAccessToken(): String =
        accessToken ?: throw IllegalStateException("Access token is missing from server response")

    fun getValidatedRefreshToken(): String =
        refreshToken ?: throw IllegalStateException("Refresh token is missing from server response")
}

@Serializable
data class UserDto(
    val id: String,  // Server returns String UUID, not Int
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
    val roles: List<String> = emptyList()
)

@Serializable
data class RefreshReq(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class ProfileDto(
    val id: String,  // Server returns String UUID, not Int
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("group_number") val groupNumber: String? = null,
    val roles: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

