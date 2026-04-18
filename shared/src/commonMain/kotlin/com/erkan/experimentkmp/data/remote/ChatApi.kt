package com.erkan.experimentkmp.data.remote

import com.erkan.experimentkmp.data.remote.model.ChatMessagesPageResponseDto
import com.erkan.experimentkmp.data.remote.model.ChatRoomResponseDto
import com.erkan.experimentkmp.data.remote.model.CreateChatRoomRequestDto
import com.erkan.experimentkmp.data.remote.model.toDomain
import com.erkan.experimentkmp.domain.model.ChatMessage
import com.erkan.experimentkmp.domain.model.ChatRoom
import com.erkan.experimentkmp.network.ExperimentKsApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class ChatApi(
    private val httpClient: HttpClient,
) {
    suspend fun listRooms(accessToken: String): List<ChatRoom> =
        httpClient.get("${ExperimentKsApiConfig.BaseUrl}/api/v1/chat/rooms") {
            bearer(accessToken)
        }.body<List<ChatRoomResponseDto>>().map { it.toDomain() }

    suspend fun createRoom(
        accessToken: String,
        name: String,
    ): ChatRoom = httpClient.post("${ExperimentKsApiConfig.BaseUrl}/api/v1/chat/rooms") {
        bearer(accessToken)
        contentType(ContentType.Application.Json)
        setBody(CreateChatRoomRequestDto(name = name))
    }.body<ChatRoomResponseDto>().toDomain()

    suspend fun joinRoom(
        accessToken: String,
        roomId: String,
    ): ChatRoom = httpClient.post("${ExperimentKsApiConfig.BaseUrl}/api/v1/chat/rooms/$roomId/join") {
        bearer(accessToken)
    }.body<ChatRoomResponseDto>().toDomain()

    suspend fun listMessages(
        accessToken: String,
        roomId: String,
        page: Int = 0,
        size: Int = 50,
    ): List<ChatMessage> = httpClient.get("${ExperimentKsApiConfig.BaseUrl}/api/v1/chat/rooms/$roomId/messages") {
        bearer(accessToken)
        parameter("page", page)
        parameter("size", size)
    }.body<ChatMessagesPageResponseDto>().items.map { it.toDomain() }

    private fun io.ktor.client.request.HttpRequestBuilder.bearer(accessToken: String) {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
    }
}
