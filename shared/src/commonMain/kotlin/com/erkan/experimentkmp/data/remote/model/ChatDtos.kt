package com.erkan.experimentkmp.data.remote.model

import com.erkan.experimentkmp.domain.model.ChatMessage
import com.erkan.experimentkmp.domain.model.ChatRoom
import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRoomRequestDto(
    val name: String,
)

@Serializable
data class InviteChatRoomMemberRequestDto(
    val email: String,
)

@Serializable
data class SendChatMessageRequestDto(
    val action: String,
    val roomId: String,
    val clientMessageId: String,
    val content: String,
)

@Serializable
data class ChatRoomResponseDto(
    val id: String,
    val name: String,
    val createdByUserId: String,
    val joinedAt: String? = null,
    val lastActivityAt: String? = null,
    val lastMessagePreview: String? = null,
    val memberCount: Long = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ChatMessageResponseDto(
    val id: String,
    val roomId: String,
    val senderUserId: String,
    val clientMessageId: String = "",
    val content: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ChatMessagesPageResponseDto(
    val items: List<ChatMessageResponseDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
)

internal fun ChatRoomResponseDto.toDomain(): ChatRoom = ChatRoom(
    id = id,
    name = name,
    createdByUserId = createdByUserId,
    joinedAt = joinedAt,
    lastActivityAt = lastActivityAt,
    lastMessagePreview = lastMessagePreview,
    memberCount = memberCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun ChatMessageResponseDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    roomId = roomId,
    senderUserId = senderUserId,
    clientMessageId = clientMessageId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
