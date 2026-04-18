package com.erkan.experimentkmp.domain.model

data class AuthSession(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiresInSeconds: Long,
    val refreshToken: String,
    val refreshTokenExpiresInSeconds: Long,
)

data class CurrentUser(
    val id: String,
    val email: String,
    val displayName: String,
)

data class AuthenticatedUserSession(
    val session: AuthSession,
    val user: CurrentUser,
)
