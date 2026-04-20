package com.erkan.experimentkmp.data.auth

import android.content.Context
import android.content.SharedPreferences

class AndroidAuthSessionStore(
    context: Context,
) : AuthSessionStore {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun save(session: StoredAuthSession) {
        preferences.edit()
            .putString(KEY_TOKEN_TYPE, session.tokenType)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, session.accessTokenExpiresAtEpochSeconds)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_REFRESH_TOKEN_EXPIRES_AT, session.refreshTokenExpiresAtEpochSeconds)
            .apply()
    }

    override fun load(): StoredAuthSession? {
        val tokenType = preferences.getString(KEY_TOKEN_TYPE, null) ?: return null
        val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val accessTokenExpiresAt = preferences.getLong(KEY_ACCESS_TOKEN_EXPIRES_AT, -1L)
        val refreshTokenExpiresAt = preferences.getLong(KEY_REFRESH_TOKEN_EXPIRES_AT, -1L)
        if (accessTokenExpiresAt <= 0L || refreshTokenExpiresAt <= 0L) return null

        return StoredAuthSession(
            tokenType = tokenType,
            accessToken = accessToken,
            accessTokenExpiresAtEpochSeconds = accessTokenExpiresAt,
            refreshToken = refreshToken,
            refreshTokenExpiresAtEpochSeconds = refreshTokenExpiresAt,
        )
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "experimentkmp_auth_session"
        const val KEY_TOKEN_TYPE = "token_type"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"
    }
}
