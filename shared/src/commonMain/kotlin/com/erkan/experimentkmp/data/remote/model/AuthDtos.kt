package com.erkan.experimentkmp.data.remote.model

import com.erkan.experimentkmp.domain.model.AuthSession
import com.erkan.experimentkmp.domain.model.CurrentUser
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequestDto(
    val displayName: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class AuthResponseDto(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiresInSeconds: Long,
    val refreshToken: String,
    val refreshTokenExpiresInSeconds: Long,
)

@Serializable
data class CurrentUserResponseDto(
    val id: String,
    val email: String,
    val displayName: String,
)

internal fun AuthResponseDto.toDomain(): AuthSession = AuthSession(
    tokenType = tokenType,
    accessToken = accessToken,
    accessTokenExpiresInSeconds = accessTokenExpiresInSeconds,
    refreshToken = refreshToken,
    refreshTokenExpiresInSeconds = refreshTokenExpiresInSeconds,
)

internal fun CurrentUserResponseDto.toDomain(): CurrentUser = CurrentUser(
    id = id,
    email = email,
    displayName = displayName,
)
