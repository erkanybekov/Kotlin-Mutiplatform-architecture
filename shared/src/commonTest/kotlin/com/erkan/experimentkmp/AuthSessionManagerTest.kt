package com.erkan.experimentkmp

import com.erkan.experimentkmp.data.auth.AuthSessionManager
import com.erkan.experimentkmp.data.auth.AuthSessionStore
import com.erkan.experimentkmp.data.auth.StoredAuthSession
import com.erkan.experimentkmp.data.remote.AuthApi
import com.erkan.experimentkmp.logging.InMemoryAppLogger
import com.erkan.experimentkmp.network.configureSharedHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthSessionManagerTest {
    @Test
    fun restoreAuthenticatedUserSessionRefreshesExpiredAccessToken() = runTest {
        val store = InMemoryAuthSessionStore(
            StoredAuthSession(
                tokenType = "Bearer",
                accessToken = "expired-access",
                accessTokenExpiresAtEpochSeconds = 900L,
                refreshToken = "refresh-token",
                refreshTokenExpiresAtEpochSeconds = 8_000L,
            ),
        )

        var refreshCallCount = 0
        var currentUserAuthorization: String? = null
        val authApi = AuthApi(
            HttpClient(
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/api/v1/auth/refresh" -> {
                            refreshCallCount += 1
                            assertEquals(HttpMethod.Post, request.method)
                            respondJson(
                                """
                                {
                                  "tokenType": "Bearer",
                                  "accessToken": "fresh-access",
                                  "accessTokenExpiresInSeconds": 3600,
                                  "refreshToken": "fresh-refresh",
                                  "refreshTokenExpiresInSeconds": 7200
                                }
                                """.trimIndent(),
                            )
                        }

                        "/api/v1/users/me" -> {
                            currentUserAuthorization = request.headers[HttpHeaders.Authorization]
                            respondJson(
                                """
                                {
                                  "id": "user-1",
                                  "email": "erkan@example.com",
                                  "displayName": "Erkan"
                                }
                                """.trimIndent(),
                            )
                        }

                        else -> error("Unexpected path: ${request.url.encodedPath}")
                    }
                },
            ) {
                configureSharedHttpClient(InMemoryAppLogger())
            },
        )

        val manager = AuthSessionManager(
            authApi = authApi,
            authSessionStore = store,
            nowEpochSecondsProvider = { 1_000L },
        )

        val restoredSession = manager.restoreAuthenticatedUserSession()

        assertNotNull(restoredSession)
        assertEquals("fresh-access", restoredSession.session.accessToken)
        assertEquals("fresh-refresh", restoredSession.session.refreshToken)
        assertEquals("Erkan", restoredSession.user.displayName)
        assertEquals(1, refreshCallCount)
        assertEquals("Bearer fresh-access", currentUserAuthorization)
        assertEquals("fresh-access", store.load()?.accessToken)
        assertEquals("fresh-refresh", store.load()?.refreshToken)
    }

    @Test
    fun restoreAuthenticatedUserSessionClearsStoreWhenRefreshTokenExpired() = runTest {
        val store = InMemoryAuthSessionStore(
            StoredAuthSession(
                tokenType = "Bearer",
                accessToken = "expired-access",
                accessTokenExpiresAtEpochSeconds = 900L,
                refreshToken = "expired-refresh",
                refreshTokenExpiresAtEpochSeconds = 950L,
            ),
        )

        var callCount = 0
        val authApi = AuthApi(
            HttpClient(
                MockEngine {
                    callCount += 1
                    respond(
                        content = "",
                        status = HttpStatusCode.InternalServerError,
                    )
                },
            ) {
                configureSharedHttpClient(InMemoryAppLogger())
            },
        )

        val manager = AuthSessionManager(
            authApi = authApi,
            authSessionStore = store,
            nowEpochSecondsProvider = { 1_000L },
        )

        val restoredSession = manager.restoreAuthenticatedUserSession()

        assertNull(restoredSession)
        assertNull(store.load())
        assertEquals(0, callCount)
    }
}

private fun MockRequestHandleScope.respondJson(
    content: String,
) = respond(
    content = content,
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
)

private class InMemoryAuthSessionStore(
    initialSession: StoredAuthSession? = null,
) : AuthSessionStore {
    private var storedSession: StoredAuthSession? = initialSession

    override fun save(session: StoredAuthSession) {
        storedSession = session
    }

    override fun load(): StoredAuthSession? = storedSession

    override fun clear() {
        storedSession = null
    }
}
