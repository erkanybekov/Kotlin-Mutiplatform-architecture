package com.erkan.experimentkmp.data.remote

import com.erkan.experimentkmp.data.remote.model.ChatMessageResponseDto
import com.erkan.experimentkmp.data.remote.model.SendChatMessageRequestDto
import com.erkan.experimentkmp.data.remote.model.toDomain
import com.erkan.experimentkmp.domain.model.ChatMessageDeleted
import com.erkan.experimentkmp.logging.AppLogger
import com.erkan.experimentkmp.network.AppJson
import com.erkan.experimentkmp.network.ExperimentKsApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

class KtorChatSocketClient(
    private val httpClient: HttpClient,
    private val appLogger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ChatSocketClient {
    private val eventFlow = MutableSharedFlow<ChatSocketEvent>(extraBufferCapacity = 32)
    override val events: Flow<ChatSocketEvent> = eventFlow.asSharedFlow()

    private val sessionMutex = Mutex()
    private var session: DefaultClientWebSocketSession? = null
    private var incomingJob: Job? = null

    override suspend fun connect(accessToken: String) {
        sessionMutex.withLock {
            if (session != null) return
            eventFlow.tryEmit(ChatSocketEvent.StatusChanged(ChatSocketStatus.CONNECTING))
            appLogger.append(
                level = "DEBUG",
                category = "network",
                message = "--> WS CONNECT /ws/chat",
            )

            val createdSession = httpClient.webSocketSession {
                url.takeFrom(ExperimentKsApiConfig.ChatSocketUrl)
                url.parameters.append("access_token", accessToken)
                headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            session = createdSession
            appLogger.append(
                level = "INFO",
                category = "network",
                message = "<-- WS CONNECTED /ws/chat",
            )
            incomingJob?.cancel()
            incomingJob = scope.launch {
                observeIncoming(createdSession)
            }
            eventFlow.tryEmit(ChatSocketEvent.StatusChanged(ChatSocketStatus.CONNECTED))
        }
    }

    override suspend fun disconnect() {
        val activeSession = sessionMutex.withLock {
            val current = session
            session = null
            incomingJob?.cancel()
            incomingJob = null
            current
        }

        appLogger.append(
            level = "DEBUG",
            category = "network",
            message = "--> WS CLOSE /ws/chat",
        )
        activeSession?.close(
            CloseReason(
                CloseReason.Codes.NORMAL,
                "Client disconnect",
            ),
        )
        eventFlow.tryEmit(ChatSocketEvent.StatusChanged(ChatSocketStatus.DISCONNECTED))
    }

    override suspend fun sendMessage(
        roomId: String,
        clientMessageId: String,
        content: String,
    ) {
        val activeSession = sessionMutex.withLock { session }
            ?: error("Chat socket is not connected.")

        val payload = SendChatMessageRequestDto(
            action = "SEND_MESSAGE",
            roomId = roomId,
            clientMessageId = clientMessageId,
            content = content,
        )
        val encodedPayload = AppJson.encodeToString(payload)
        appLogger.append(
            level = "DEBUG",
            category = "network",
            message = "--> WS /ws/chat",
            details = encodedPayload,
        )
        activeSession.send(Frame.Text(encodedPayload))
    }

    private suspend fun observeIncoming(
        activeSession: DefaultClientWebSocketSession,
    ) {
        try {
            for (frame in activeSession.incoming) {
                if (frame !is Frame.Text) continue
                val payload = frame.readText()
                appLogger.append(
                    level = "DEBUG",
                    category = "network",
                    message = "<-- WS /ws/chat",
                    details = payload,
                )
                parseEvent(payload)?.let { event ->
                    eventFlow.emit(event)
                }
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            appLogger.append(
                level = "ERROR",
                category = "network",
                message = "<-- WS ERROR /ws/chat",
                details = error.message ?: error::class.simpleName,
            )
            eventFlow.emit(
                ChatSocketEvent.Failure(
                    error.message ?: "Chat connection failed.",
                ),
            )
        } finally {
            sessionMutex.withLock {
                if (session === activeSession) {
                    session = null
                }
                incomingJob = null
            }
            eventFlow.emit(ChatSocketEvent.StatusChanged(ChatSocketStatus.DISCONNECTED))
        }
    }

    private fun parseEvent(payload: String): ChatSocketEvent? {
        return runCatching {
            val element = AppJson.parseToJsonElement(payload)
            val messageObject = element.findMessageObject()
            if (messageObject != null) {
                ChatSocketEvent.MessageReceived(
                    AppJson.decodeFromJsonElement<ChatMessageResponseDto>(messageObject).toDomain(),
                )
            } else {
                element.findDeletedMessage()?.let { deletion ->
                    ChatSocketEvent.MessageDeleted(deletion)
                }
            }
        }.getOrNull()
    }
}

private fun JsonElement.findMessageObject(): JsonObject? = when (this) {
    is JsonObject -> when {
        looksLikeChatMessage() -> this
        else -> listOf("payload", "data", "message")
            .asSequence()
            .mapNotNull { key -> this[key]?.findMessageObject() }
            .firstOrNull()
    }

    is JsonArray -> asSequence()
        .mapNotNull { item -> item.findMessageObject() }
        .firstOrNull()

    else -> null
}

private fun JsonObject.looksLikeChatMessage(): Boolean {
    val content = this["content"]
    val roomId = this["roomId"]
    val senderUserId = this["senderUserId"]
    return content != null && roomId != null && senderUserId != null
}

private fun JsonElement.findDeletedMessage(): ChatMessageDeleted? = when (this) {
    is JsonObject -> when {
        eventName().isMessageDeletedEventName() -> {
            eventPayloadObject()?.toDeletedMessage()
                ?: toDeletedMessage()
        }

        else -> listOf("payload", "data", "message")
            .asSequence()
            .mapNotNull { key -> this[key]?.findDeletedMessage() }
            .firstOrNull()
    }

    is JsonArray -> asSequence()
        .mapNotNull { item -> item.findDeletedMessage() }
        .firstOrNull()

    else -> null
}

private fun JsonObject.eventName(): String? = listOf("event", "type", "action", "name")
    .asSequence()
    .mapNotNull { key -> this[key]?.jsonPrimitive?.contentOrNull }
    .firstOrNull()

private fun String?.isMessageDeletedEventName(): Boolean =
    this?.lowercase()?.replace('_', '.') == "message.deleted"

private fun JsonObject.eventPayloadObject(): JsonObject? = listOf("payload", "data", "message")
    .asSequence()
    .mapNotNull { key -> this[key] as? JsonObject }
    .firstOrNull()

private fun JsonObject.toDeletedMessage(): ChatMessageDeleted? {
    val roomId = this["roomId"]?.jsonPrimitive?.contentOrNull ?: return null
    val messageId = this["messageId"]?.jsonPrimitive?.contentOrNull
        ?: this["id"]?.jsonPrimitive?.contentOrNull
        ?: return null
    return ChatMessageDeleted(
        roomId = roomId,
        messageId = messageId,
    )
}
