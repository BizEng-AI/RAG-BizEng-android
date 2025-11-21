package com.example.myapplication.data.remote.auth

/** Domain-specific auth exceptions for clean UI messaging */
sealed class AuthException(message: String): Exception(message)
class InvalidCredentialsException: AuthException("Invalid credentials")
class EmailAlreadyExistsException: AuthException("Email already exists")
class IncompleteTokenResponseException: AuthException("Incomplete token response")
class EmptyResponseException: AuthException("Empty server response")
class TokenParseException: AuthException("Failed to parse token response")
class ServerErrorException: AuthException("Server error")
class RateLimitException: AuthException("Too many attempts. Please wait and try again.")
class NetworkErrorException: AuthException("Network error")
class UnknownAuthException: AuthException("Authentication failed")

