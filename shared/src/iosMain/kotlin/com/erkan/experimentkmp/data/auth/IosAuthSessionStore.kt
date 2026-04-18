package com.erkan.experimentkmp.data.auth

import platform.Foundation.NSUserDefaults

class IosAuthSessionStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : AuthSessionStore {
    override fun save(session: StoredAuthSession) {
        userDefaults.setObject(session.tokenType, forKey = KEY_TOKEN_TYPE)
        userDefaults.setObject(session.accessToken, forKey = KEY_ACCESS_TOKEN)
        userDefaults.setDouble(
            session.accessTokenExpiresAtEpochSeconds.toDouble(),
            forKey = KEY_ACCESS_TOKEN_EXPIRES_AT,
        )
        userDefaults.setObject(session.refreshToken, forKey = KEY_REFRESH_TOKEN)
        userDefaults.setDouble(
            session.refreshTokenExpiresAtEpochSeconds.toDouble(),
            forKey = KEY_REFRESH_TOKEN_EXPIRES_AT,
        )
    }

    override fun load(): StoredAuthSession? {
        val tokenType = userDefaults.objectForKey(KEY_TOKEN_TYPE) as? String ?: return null
        val accessToken = userDefaults.objectForKey(KEY_ACCESS_TOKEN) as? String ?: return null
        val refreshToken = userDefaults.objectForKey(KEY_REFRESH_TOKEN) as? String ?: return null
        val accessTokenExpiresAt = userDefaults.doubleForKey(KEY_ACCESS_TOKEN_EXPIRES_AT).toLong()
        val refreshTokenExpiresAt = userDefaults.doubleForKey(KEY_REFRESH_TOKEN_EXPIRES_AT).toLong()
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
        userDefaults.removeObjectForKey(KEY_TOKEN_TYPE)
        userDefaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        userDefaults.removeObjectForKey(KEY_ACCESS_TOKEN_EXPIRES_AT)
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN_EXPIRES_AT)
    }

    private companion object {
        const val KEY_TOKEN_TYPE = "token_type"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at"
    }
}
