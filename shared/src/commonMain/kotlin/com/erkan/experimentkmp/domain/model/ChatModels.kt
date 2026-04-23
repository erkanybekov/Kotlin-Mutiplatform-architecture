package com.erkan.experimentkmp.domain.model

data class ChatRoom(
    val id: String,
    val name: String,
    val createdByUserId: String,
    val joinedAt: String?,
    val lastActivityAt: String?,
    val lastMessagePreview: String?,
    val memberCount: Long,
    val createdAt: String?,
    val updatedAt: String?,
)

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderUserId: String,
    val clientMessageId: String,
    val content: String,
    val createdAt: String?,
    val updatedAt: String?,
)

data class ChatMessageDeleted(
    val roomId: String,
    val messageId: String,
)
