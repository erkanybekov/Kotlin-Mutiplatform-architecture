package com.erkan.experimentkmp.data.remote

import com.erkan.experimentkmp.domain.model.ChatMessage
import com.erkan.experimentkmp.domain.model.ChatMessageDeleted
import kotlinx.coroutines.flow.Flow

enum class ChatSocketStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
}

sealed interface ChatSocketEvent {
    data class StatusChanged(
        val status: ChatSocketStatus,
    ) : ChatSocketEvent

    data class MessageReceived(
        val message: ChatMessage,
    ) : ChatSocketEvent

    data class MessageDeleted(
        val deletion: ChatMessageDeleted,
    ) : ChatSocketEvent

    data class Failure(
        val reason: String,
    ) : ChatSocketEvent
}

interface ChatSocketClient {
    val events: Flow<ChatSocketEvent>

    suspend fun connect(accessToken: String)

    suspend fun disconnect()

    suspend fun sendMessage(
        roomId: String,
        clientMessageId: String,
        content: String,
    )
}
