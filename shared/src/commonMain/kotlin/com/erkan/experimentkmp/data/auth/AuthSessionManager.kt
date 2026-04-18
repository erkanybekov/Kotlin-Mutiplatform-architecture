package com.erkan.experimentkmp.data.auth

import com.erkan.experimentkmp.data.remote.AuthApi
import com.erkan.experimentkmp.domain.model.AuthSession
import com.erkan.experimentkmp.domain.model.AuthenticatedUserSession
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class AuthSessionManager(
    private val authApi: AuthApi,
    private val authSessionStore: AuthSessionStore,
    private val nowEpochSecondsProvider: () -> Long = { Clock.System.now().epochSeconds },
) {
    private val sessionMutex = Mutex()
    private var storedSession: StoredAuthSession? = authSessionStore.load()

    suspend fun restoreAuthenticatedUserSession(): AuthenticatedUserSession? {
        val initialSession = sessionMutex.withLock {
            storedSession ?: return null
            validStoredSessionLocked() ?: return null
        }

        return runCatching {
            val user = authApi.currentUser(initialSession.accessToken)
            AuthenticatedUserSession(
                session = initialSession.toDomain(nowEpochSeconds()),
                user = user,
            )
        }.recoverCatching { error ->
            if (!error.isUnauthorized()) throw error

            val refreshedSession = sessionMutex.withLock {
                refreshStoredSessionLocked() ?: throw AuthenticationRequiredException()
            }
            val user = authApi.currentUser(refreshedSession.accessToken)
            AuthenticatedUserSession(
                session = refreshedSession.toDomain(nowEpochSeconds()),
                user = user,
            )
        }.getOrNull()
    }

    suspend fun persistSession(authSession: AuthSession) {
        sessionMutex.withLock {
            persistLocked(authSession)
        }
    }

    suspend fun <T> withFreshAccessToken(
        block: suspend (String) -> T,
    ): T {
        val currentSession = sessionMutex.withLock {
            validStoredSessionLocked() ?: throw AuthenticationRequiredException()
        }

        return try {
            block(currentSession.accessToken)
        } catch (error: Throwable) {
            if (!error.isUnauthorized()) throw error

            val refreshedSession = sessionMutex.withLock {
                refreshStoredSessionLocked() ?: throw AuthenticationRequiredException()
            }
            block(refreshedSession.accessToken)
        }
    }

    suspend fun clearSession() {
        sessionMutex.withLock {
            clearLocked()
        }
    }

    private suspend fun validStoredSessionLocked(): StoredAuthSession? {
        val current = storedSession ?: return null
        return if (current.isAccessTokenUsable(nowEpochSeconds())) {
            current
        } else {
            refreshStoredSessionLocked()
        }
    }

    private suspend fun refreshStoredSessionLocked(): StoredAuthSession? {
        val current = storedSession ?: return null
        if (!current.canRefresh(nowEpochSeconds())) {
            clearLocked()
            return null
        }

        return runCatching {
            authApi.refresh(current.refreshToken)
        }.getOrNull()?.let { refreshedSession ->
            persistLocked(refreshedSession)
        } ?: run {
            clearLocked()
            null
        }
    }

    private fun persistLocked(authSession: AuthSession): StoredAuthSession {
        val stored = StoredAuthSession.from(
            session = authSession,
            nowEpochSeconds = nowEpochSeconds(),
        )
        storedSession = stored
        authSessionStore.save(stored)
        return stored
    }

    private fun clearLocked() {
        storedSession = null
        authSessionStore.clear()
    }

    private fun nowEpochSeconds(): Long = nowEpochSecondsProvider()
}

private fun Throwable.isUnauthorized(): Boolean =
    this is ClientRequestException && response.status == HttpStatusCode.Unauthorized

class AuthenticationRequiredException(
    message: String = "Authentication is required.",
) : IllegalStateException(message)
