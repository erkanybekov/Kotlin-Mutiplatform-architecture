package com.erkan.experimentkmp.data.auth

import com.erkan.experimentkmp.domain.model.AuthSession

data class StoredAuthSession(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiresAtEpochSeconds: Long,
    val refreshToken: String,
    val refreshTokenExpiresAtEpochSeconds: Long,
) {
    fun isAccessTokenUsable(nowEpochSeconds: Long, leewaySeconds: Long = 60L): Boolean =
        accessTokenExpiresAtEpochSeconds > nowEpochSeconds + leewaySeconds

    fun canRefresh(nowEpochSeconds: Long, leewaySeconds: Long = 60L): Boolean =
        refreshTokenExpiresAtEpochSeconds > nowEpochSeconds + leewaySeconds

    fun toDomain(nowEpochSeconds: Long): AuthSession = AuthSession(
        tokenType = tokenType,
        accessToken = accessToken,
        accessTokenExpiresInSeconds = (accessTokenExpiresAtEpochSeconds - nowEpochSeconds)
            .coerceAtLeast(0L),
        refreshToken = refreshToken,
        refreshTokenExpiresInSeconds = (refreshTokenExpiresAtEpochSeconds - nowEpochSeconds)
            .coerceAtLeast(0L),
    )

    companion object {
        fun from(
            session: AuthSession,
            nowEpochSeconds: Long,
        ): StoredAuthSession = StoredAuthSession(
            tokenType = session.tokenType,
            accessToken = session.accessToken,
            accessTokenExpiresAtEpochSeconds = nowEpochSeconds + session.accessTokenExpiresInSeconds,
            refreshToken = session.refreshToken,
            refreshTokenExpiresAtEpochSeconds = nowEpochSeconds + session.refreshTokenExpiresInSeconds,
        )
    }
}
