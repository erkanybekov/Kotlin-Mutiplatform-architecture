package com.erkan.experimentkmp.data.remote

import com.erkan.experimentkmp.data.remote.model.AuthResponseDto
import com.erkan.experimentkmp.data.remote.model.CurrentUserResponseDto
import com.erkan.experimentkmp.data.remote.model.LoginRequestDto
import com.erkan.experimentkmp.data.remote.model.RefreshTokenRequestDto
import com.erkan.experimentkmp.data.remote.model.SignupRequestDto
import com.erkan.experimentkmp.data.remote.model.toDomain
import com.erkan.experimentkmp.domain.model.AuthSession
import com.erkan.experimentkmp.domain.model.CurrentUser
import com.erkan.experimentkmp.network.ExperimentKsApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class AuthApi(
    private val httpClient: HttpClient,
) {
    suspend fun signup(
        displayName: String,
        email: String,
        password: String,
    ): AuthSession = httpClient.post("${ExperimentKsApiConfig.BaseUrl}/api/v1/auth/signup") {
        contentType(ContentType.Application.Json)
        setBody(
            SignupRequestDto(
                displayName = displayName,
                email = email,
                password = password,
            ),
        )
    }.body<AuthResponseDto>().toDomain()

    suspend fun login(
        email: String,
        password: String,
    ): AuthSession = httpClient.post("${ExperimentKsApiConfig.BaseUrl}/api/v1/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(
            LoginRequestDto(
                email = email,
                password = password,
            ),
        )
    }.body<AuthResponseDto>().toDomain()

    suspend fun refresh(
        refreshToken: String,
    ): AuthSession = httpClient.post("${ExperimentKsApiConfig.BaseUrl}/api/v1/auth/refresh") {
        contentType(ContentType.Application.Json)
        setBody(
            RefreshTokenRequestDto(
                refreshToken = refreshToken,
            ),
        )
    }.body<AuthResponseDto>().toDomain()

    suspend fun currentUser(accessToken: String): CurrentUser =
        httpClient.get("${ExperimentKsApiConfig.BaseUrl}/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }.body<CurrentUserResponseDto>().toDomain()
}
