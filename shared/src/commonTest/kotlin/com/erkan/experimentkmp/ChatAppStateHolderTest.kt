package com.erkan.experimentkmp

import com.erkan.experimentkmp.data.remote.ChatApi
import com.erkan.experimentkmp.domain.model.ChatMessage
import com.erkan.experimentkmp.logging.InMemoryAppLogger
import com.erkan.experimentkmp.network.configureSharedHttpClient
import com.erkan.experimentkmp.presentation.chat.ChatMessageItemUi
import com.erkan.experimentkmp.presentation.chat.ChatRoomItemUi
import com.erkan.experimentkmp.presentation.chat.applyDeletedMessageToUi
import com.erkan.experimentkmp.presentation.chat.mergeIncomingMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ChatAppStateHolderTest {
    @Test
    fun websocketEchoWithoutClientMessageIdReplacesPendingMessage() {
        val pendingMessage = ChatMessageItemUi(
            id = "local-1",
            clientMessageId = "local-1",
            senderLabel = "You",
            body = "Hello from mobile",
            timeLabel = "Now",
            isMine = true,
            deliveryLabel = "Sent",
            sortKey = "2026-04-23T09:00:00Z",
        )

        val mergedMessages = listOf(pendingMessage).mergeIncomingMessage(
            incomingMessage = ChatMessage(
                id = "server-message-1",
                roomId = "room-1",
                senderUserId = "user-1",
                clientMessageId = "",
                content = "Hello from mobile",
                createdAt = "2026-04-23T09:00:01Z",
                updatedAt = "2026-04-23T09:00:01Z",
            ),
            currentUserId = "user-1",
        )

        assertEquals(1, mergedMessages.size)
        assertEquals("server-message-1", mergedMessages.single().id)
        assertEquals("Hello from mobile", mergedMessages.single().body)
        assertNull(mergedMessages.single().deliveryLabel)
    }

    @Test
    fun applyDeletedMessageToUiRemovesMessageAndRollsPreviewBack() {
        val result = applyDeletedMessageToUi(
            rooms = listOf(
                ChatRoomItemUi(
                    id = "room-1",
                    name = "General",
                    preview = "My latest message",
                    activityLabel = "Active 09:01",
                    memberCountLabel = "2 members",
                    isSelected = true,
                    sortKey = "2026-04-23T09:01:00Z",
                ),
            ),
            selectedRoomId = "room-1",
            messages = listOf(
                ChatMessageItemUi(
                    id = "message-1",
                    clientMessageId = "",
                    senderLabel = "teammate",
                    body = "Teammate hello",
                    timeLabel = "09:00",
                    isMine = false,
                    deliveryLabel = null,
                    sortKey = "2026-04-23T09:00:00Z",
                ),
                ChatMessageItemUi(
                    id = "message-2",
                    clientMessageId = "",
                    senderLabel = "You",
                    body = "My latest message",
                    timeLabel = "09:01",
                    isMine = true,
                    deliveryLabel = null,
                    sortKey = "2026-04-23T09:01:00Z",
                ),
            ),
            roomId = "room-1",
            messageId = "message-2",
        )

        assertEquals(listOf("message-1"), result.messages.map { message -> message.id })
        assertEquals("Teammate hello", result.rooms.single().preview)
        assertFalse(result.shouldRefreshRooms)
    }

    @Test
    fun deleteMessageUsesRoomAndMessageDeleteEndpoint() = runTest {
        var capturedMethod: HttpMethod? = null
        var capturedPath: String? = null
        val chatApi = ChatApi(
            HttpClient(
                MockEngine { request ->
                    capturedMethod = request.method
                    capturedPath = request.url.encodedPath
                    respond(
                        content = "",
                        status = HttpStatusCode.NoContent,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                },
            ) {
                configureSharedHttpClient(InMemoryAppLogger())
            },
        )

        chatApi.deleteMessage(
            accessToken = "access-token",
            roomId = "room-1",
            messageId = "message-2",
        )

        assertEquals(HttpMethod.Delete, capturedMethod)
        assertEquals("/api/v1/chat/rooms/room-1/messages/message-2", capturedPath)
    }
}
