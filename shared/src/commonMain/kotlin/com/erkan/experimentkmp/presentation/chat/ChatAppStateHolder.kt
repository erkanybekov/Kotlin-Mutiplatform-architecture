package com.erkan.experimentkmp.presentation.chat

import com.erkan.experimentkmp.data.auth.AuthSessionManager
import com.erkan.experimentkmp.data.auth.AuthenticationRequiredException
import com.erkan.experimentkmp.data.remote.AuthApi
import com.erkan.experimentkmp.data.remote.ChatApi
import com.erkan.experimentkmp.data.remote.ChatSocketClient
import com.erkan.experimentkmp.data.remote.ChatSocketEvent
import com.erkan.experimentkmp.data.remote.ChatSocketStatus
import com.erkan.experimentkmp.domain.model.AuthenticatedUserSession
import com.erkan.experimentkmp.domain.model.ChatMessage
import com.erkan.experimentkmp.domain.model.ChatMessageDeleted
import com.erkan.experimentkmp.domain.model.ChatRoom
import com.erkan.experimentkmp.logging.AppLogger
import com.erkan.experimentkmp.platform.randomUuidString
import com.erkan.experimentkmp.presentation.shared.ObservationHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ChatAppStateHolder(
    private val authApi: AuthApi,
    private val authSessionManager: AuthSessionManager,
    private val chatApi: ChatApi,
    private val chatSocketClient: ChatSocketClient,
    private val appLogger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(ChatAppUiState())
    val state: StateFlow<ChatAppUiState> = _state.asStateFlow()

    val currentState: ChatAppUiState
        get() = state.value

    private var session: AuthenticatedUserSession? = null
    private var hasAttemptedRestore = false

    init {
        scope.launch {
            chatSocketClient.events.collectLatest(::handleSocketEvent)
        }
    }

    fun load() {
        if (session == null) {
            if (hasAttemptedRestore) return
            hasAttemptedRestore = true
            scope.launch {
                restoreSessionIfPossible()
            }
            return
        }

        if (state.value.rooms.isEmpty() && !state.value.isLoadingRooms) {
            refreshRooms()
        }
        scope.launch { ensureSocketConnected() }
    }

    fun switchAuthMode(mode: ChatAuthMode) {
        if (mode == state.value.authMode || state.value.isAuthenticating) return
        _state.update { current ->
            current.copy(
                authMode = mode,
                displayNameError = null,
                emailError = null,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

    fun updateDisplayName(value: String) {
        _state.update { current ->
            current.copy(
                displayName = value,
                displayNameError = null,
                errorMessage = null,
            )
        }
    }

    fun updateEmail(value: String) {
        _state.update { current ->
            current.copy(
                email = value,
                emailError = null,
                errorMessage = null,
            )
        }
    }

    fun updatePassword(value: String) {
        _state.update { current ->
            current.copy(
                password = value,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

    fun updateNewRoomName(value: String) {
        _state.update { current ->
            current.copy(
                newRoomName = value,
                newRoomNameError = null,
                errorMessage = null,
            )
        }
    }

    fun updateComposerText(value: String) {
        _state.update { current ->
            current.copy(
                composerText = value,
                errorMessage = null,
            )
        }
    }

    fun dismissError() {
        _state.update { current ->
            current.copy(errorMessage = null)
        }
    }

    fun authenticate() {
        val current = state.value
        if (current.isAuthenticating) return

        val displayNameError = if (current.authMode == ChatAuthMode.SIGNUP && current.displayName.isBlank()) {
            "Enter your display name."
        } else {
            null
        }
        val emailError = if (!current.email.contains('@')) {
            "Enter a valid email address."
        } else {
            null
        }
        val passwordError = if (current.password.length < 8) {
            "Password must be at least 8 characters."
        } else {
            null
        }

        if (displayNameError != null || emailError != null || passwordError != null) {
            _state.update { state ->
                state.copy(
                    displayNameError = displayNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                )
            }
            return
        }

        _state.update { state ->
            state.copy(
                isAuthenticating = true,
                displayNameError = null,
                emailError = null,
                passwordError = null,
                errorMessage = null,
            )
        }

        scope.launch {
            try {
                val authSession = when (current.authMode) {
                    ChatAuthMode.LOGIN -> authApi.login(
                        email = current.email.trim(),
                        password = current.password,
                    )

                    ChatAuthMode.SIGNUP -> authApi.signup(
                        displayName = current.displayName.trim(),
                        email = current.email.trim(),
                        password = current.password,
                    )
                }
                val user = authApi.currentUser(authSession.accessToken)
                authSessionManager.persistSession(authSession)
                val authenticatedSession = AuthenticatedUserSession(
                    session = authSession,
                    user = user,
                )
                session = authenticatedSession
                _state.value = ChatAppUiState(
                    authMode = ChatAuthMode.LOGIN,
                    email = authenticatedSession.user.email,
                    isAuthenticated = true,
                    currentUserDisplayName = authenticatedSession.user.displayName,
                    currentUserEmail = authenticatedSession.user.email,
                )
                ensureSocketConnected()
                refreshRooms()
            } catch (error: Throwable) {
                _state.update { state ->
                    state.copy(
                        isAuthenticating = false,
                        errorMessage = error.message ?: "Could not authenticate.",
                    )
                }
            }
        }
    }

    fun logout() {
        session = null
        scope.launch {
            runCatching { chatSocketClient.disconnect() }
            authSessionManager.clearSession()
            _state.value = ChatAppUiState(
                authMode = ChatAuthMode.LOGIN,
                email = state.value.email,
            )
        }
    }

    fun refreshRooms() {
        val authenticatedSession = session ?: return
        if (state.value.isLoadingRooms) return

        _state.update { current ->
            current.copy(
                isLoadingRooms = true,
                errorMessage = null,
            )
        }

        scope.launch {
            try {
                val rooms = authSessionManager.withFreshAccessToken { accessToken ->
                    chatApi.listRooms(accessToken)
                }
                val sortedRooms = rooms.sortedByDescending { it.sortKey() }
                val selectedRoomId = state.value.selectedRoomId
                    ?.takeIf { selected -> sortedRooms.any { it.id == selected } }
                    ?: sortedRooms.firstOrNull()?.id

                _state.update { current ->
                    current.copy(
                        isLoadingRooms = false,
                        isAuthenticated = true,
                        currentUserDisplayName = authenticatedSession.user.displayName,
                        currentUserEmail = authenticatedSession.user.email,
                        rooms = sortedRooms.toUi(selectedRoomId),
                        selectedRoomId = selectedRoomId,
                        messages = if (selectedRoomId == null) emptyList() else current.messages,
                    )
                }

                if (selectedRoomId != null) {
                    loadMessages(selectedRoomId)
                } else {
                    _state.update { current ->
                        current.copy(
                            isLoadingMessages = false,
                            messages = emptyList(),
                        )
                    }
                }
            } catch (error: Throwable) {
                if (handleAuthenticationFailure(error)) return@launch
                _state.update { current ->
                    current.copy(
                        isLoadingRooms = false,
                        errorMessage = error.message ?: "Could not load rooms.",
                    )
                }
            }
        }
    }

    fun createRoom() {
        session ?: return
        val roomName = state.value.newRoomName.trim()
        if (roomName.isBlank()) {
            _state.update { current ->
                current.copy(newRoomNameError = "Enter a room name.")
            }
            return
        }
        if (state.value.isCreatingRoom) return

        _state.update { current ->
            current.copy(
                isCreatingRoom = true,
                newRoomNameError = null,
                errorMessage = null,
            )
        }

        scope.launch {
            try {
                val createdRoom = authSessionManager.withFreshAccessToken { accessToken ->
                    chatApi.createRoom(
                        accessToken = accessToken,
                        name = roomName,
                    )
                }
                _state.update { current ->
                    val updatedRooms = (current.rooms + createdRoom.toUi(
                        isSelected = true,
                    )).distinctBy { it.id }
                        .sortedByDescending { it.sortKey }
                    current.copy(
                        isCreatingRoom = false,
                        newRoomName = "",
                        rooms = updatedRooms.markSelected(createdRoom.id),
                        selectedRoomId = createdRoom.id,
                    )
                }
                loadMessages(createdRoom.id)
            } catch (error: Throwable) {
                if (handleAuthenticationFailure(error)) return@launch
                _state.update { current ->
                    current.copy(
                        isCreatingRoom = false,
                        errorMessage = error.message ?: "Could not create room.",
                    )
                }
            }
        }
    }

    fun selectRoom(roomId: String) {
        if (roomId == state.value.selectedRoomId && state.value.messages.isNotEmpty()) return
        _state.update { current ->
            current.copy(
                selectedRoomId = roomId,
                rooms = current.rooms.markSelected(roomId),
                errorMessage = null,
            )
        }
        loadMessages(roomId)
    }

    fun sendMessage() {
        session ?: return
        val roomId = state.value.selectedRoomId
        if (roomId.isNullOrBlank()) {
            _state.update { current ->
                current.copy(errorMessage = "Create or select a room first.")
            }
            return
        }

        val content = state.value.composerText.trim()
        if (content.isBlank()) return

        val clientMessageId = randomUuidString()
        val pendingMessage = ChatMessageItemUi(
            id = clientMessageId,
            clientMessageId = clientMessageId,
            senderLabel = "You",
            body = content,
            timeLabel = "Now",
            isMine = true,
            deliveryLabel = "Sending",
            sortKey = Clock.System.now().toString(),
        )

        _state.update { current ->
            current.copy(
                composerText = "",
                messages = (current.messages + pendingMessage).sortedBy { it.sortKey },
                rooms = current.rooms
                    .updatePreview(
                        roomId = roomId,
                        preview = content,
                        sortKey = pendingMessage.sortKey,
                    )
                    .markSelected(roomId),
                errorMessage = null,
            )
        }

        scope.launch {
            try {
                ensureSocketConnected()
                chatSocketClient.sendMessage(
                    roomId = roomId,
                    clientMessageId = clientMessageId,
                    content = content,
                )
                updateMessageDelivery(
                    clientMessageId = clientMessageId,
                    deliveryLabel = "Sent",
                )
            } catch (error: Throwable) {
                if (handleAuthenticationFailure(error)) return@launch
                updateMessageDelivery(
                    clientMessageId = clientMessageId,
                    deliveryLabel = "Failed",
                )
                _state.update { current ->
                    current.copy(
                        errorMessage = error.message ?: "Could not send message.",
                    )
                }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        session ?: return
        val roomId = state.value.selectedRoomId ?: return
        val targetMessage = state.value.messages.firstOrNull { message ->
            message.id == messageId
        } ?: return

        if (!targetMessage.canBeDeleted()) return

        setMessageDeleting(
            messageId = messageId,
            isDeleting = true,
        )

        scope.launch {
            try {
                authSessionManager.withFreshAccessToken { accessToken ->
                    chatApi.deleteMessage(
                        accessToken = accessToken,
                        roomId = roomId,
                        messageId = messageId,
                    )
                }
                applyMessageDeletion(
                    roomId = roomId,
                    messageId = messageId,
                )
            } catch (error: Throwable) {
                if (handleAuthenticationFailure(error)) return@launch
                setMessageDeleting(
                    messageId = messageId,
                    isDeleting = false,
                )
                _state.update { current ->
                    current.copy(
                        errorMessage = error.message ?: "Could not delete message.",
                    )
                }
            }
        }
    }

    fun watch(block: (ChatAppUiState) -> Unit): ObservationHandle {
        block(state.value)
        val job: Job = scope.launch {
            state.collectLatest(block)
        }
        return object : ObservationHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }

    private fun loadMessages(roomId: String) {
        val authenticatedSession = session ?: return

        _state.update { current ->
            current.copy(
                isLoadingMessages = true,
                selectedRoomId = roomId,
                rooms = current.rooms.markSelected(roomId),
                errorMessage = null,
            )
        }

        scope.launch {
            try {
                val messages = authSessionManager.withFreshAccessToken { accessToken ->
                    chatApi.joinRoom(
                        accessToken = accessToken,
                        roomId = roomId,
                    )
                    chatApi.listMessages(
                        accessToken = accessToken,
                        roomId = roomId,
                    )
                }
                ensureSocketConnected()
                _state.update { current ->
                    current.copy(
                        isLoadingMessages = false,
                        messages = messages
                            .sortedBy { it.sortKey() }
                            .map { it.toUi(authenticatedSession.user.id) },
                    )
                }
            } catch (error: Throwable) {
                if (handleAuthenticationFailure(error)) return@launch
                _state.update { current ->
                    current.copy(
                        isLoadingMessages = false,
                        errorMessage = error.message ?: "Could not load messages.",
                    )
                }
            }
        }
    }

    private suspend fun ensureSocketConnected() {
        session ?: return
        try {
            authSessionManager.withFreshAccessToken { accessToken ->
                chatSocketClient.connect(accessToken)
            }
        } catch (error: Throwable) {
            if (handleAuthenticationFailure(error)) return
            appLogger.append(
                level = "ERROR",
                category = "chat",
                message = "Socket connect failed",
                details = error.message ?: error::class.simpleName,
            )
            _state.update { current ->
                current.copy(
                    connectionState = ChatConnectionUiState.DISCONNECTED,
                    errorMessage = error.message ?: "Could not connect to chat.",
                )
            }
        }
    }

    private suspend fun restoreSessionIfPossible() {
        try {
            val restoredSession = authSessionManager.restoreAuthenticatedUserSession()
            if (restoredSession == null) return
            session = restoredSession
            _state.value = ChatAppUiState(
                authMode = ChatAuthMode.LOGIN,
                email = restoredSession.user.email,
                isAuthenticated = true,
                currentUserDisplayName = restoredSession.user.displayName,
                currentUserEmail = restoredSession.user.email,
            )
            ensureSocketConnected()
            refreshRooms()
        } catch (error: Throwable) {
            appLogger.append(
                level = "WARN",
                category = "chat",
                message = "Session restore failed",
                details = error.message ?: error::class.simpleName,
            )
        }
    }

    private suspend fun handleAuthenticationFailure(error: Throwable): Boolean {
        if (error !is AuthenticationRequiredException) return false

        session = null
        runCatching { chatSocketClient.disconnect() }
        authSessionManager.clearSession()
        _state.value = ChatAppUiState(
            authMode = ChatAuthMode.LOGIN,
            email = state.value.currentUserEmail.ifBlank { state.value.email },
            errorMessage = "Session expired. Sign in again.",
        )
        return true
    }

    private fun handleSocketEvent(event: ChatSocketEvent) {
        when (event) {
            is ChatSocketEvent.StatusChanged -> {
                _state.update { current ->
                    current.copy(connectionState = event.status.toUi())
                }
            }

            is ChatSocketEvent.MessageReceived -> handleIncomingMessage(event.message)

            is ChatSocketEvent.MessageDeleted -> handleDeletedMessage(event.deletion)

            is ChatSocketEvent.Failure -> {
                _state.update { current ->
                    current.copy(errorMessage = event.reason)
                }
            }
        }
    }

    private fun handleIncomingMessage(message: ChatMessage) {
        val authenticatedSession = session ?: return

        _state.update { current ->
            val updatedRooms = current.rooms.updatePreview(
                roomId = message.roomId,
                preview = message.content,
                sortKey = message.sortKey(),
            )
            if (current.selectedRoomId != message.roomId) {
                current.copy(rooms = updatedRooms)
            } else {
                current.copy(
                    rooms = updatedRooms,
                    messages = current.messages.mergeIncomingMessage(
                        incomingMessage = message,
                        currentUserId = authenticatedSession.user.id,
                    ),
                )
            }
        }
    }

    private fun handleDeletedMessage(deletion: ChatMessageDeleted) {
        applyMessageDeletion(
            roomId = deletion.roomId,
            messageId = deletion.messageId,
        )
    }

    private fun updateMessageDelivery(
        clientMessageId: String,
        deliveryLabel: String?,
    ) {
        _state.update { current ->
            current.copy(
                messages = current.messages.map { message ->
                    if (message.clientMessageId == clientMessageId) {
                        message.copy(deliveryLabel = deliveryLabel)
                    } else {
                        message
                    }
                },
            )
        }
    }

    private fun setMessageDeleting(
        messageId: String,
        isDeleting: Boolean,
    ) {
        _state.update { current ->
            current.copy(
                messages = current.messages.map { message ->
                    if (message.id == messageId) {
                        message.copy(
                            deliveryLabel = if (isDeleting) "Deleting" else null,
                        )
                    } else {
                        message
                    }
                },
                errorMessage = null,
            )
        }
    }

    private fun applyMessageDeletion(
        roomId: String,
        messageId: String,
    ) {
        var shouldRefreshRooms = false
        _state.update { current ->
            val result = applyDeletedMessageToUi(
                rooms = current.rooms,
                selectedRoomId = current.selectedRoomId,
                messages = current.messages,
                roomId = roomId,
                messageId = messageId,
            )
            shouldRefreshRooms = result.shouldRefreshRooms

            if (result.rooms == current.rooms && result.messages == current.messages) {
                return@update current
            }
            current.copy(
                messages = result.messages,
                rooms = result.rooms,
            )
        }

        if (shouldRefreshRooms) {
            refreshRooms()
        }
    }
}

private fun ChatSocketStatus.toUi(): ChatConnectionUiState = when (this) {
    ChatSocketStatus.DISCONNECTED -> ChatConnectionUiState.DISCONNECTED
    ChatSocketStatus.CONNECTING -> ChatConnectionUiState.CONNECTING
    ChatSocketStatus.CONNECTED -> ChatConnectionUiState.CONNECTED
}

private fun List<ChatRoom>.toUi(selectedRoomId: String?): List<ChatRoomItemUi> =
    map { room -> room.toUi(isSelected = room.id == selectedRoomId) }
        .sortedByDescending { room -> room.sortKey }

private fun ChatRoom.toUi(
    isSelected: Boolean,
): ChatRoomItemUi = ChatRoomItemUi(
    id = id,
    name = name,
    preview = lastMessagePreview?.takeIf { it.isNotBlank() } ?: "No messages yet",
    activityLabel = formatRoomActivity(lastActivityAt ?: updatedAt ?: createdAt),
    memberCountLabel = if (memberCount == 1L) "1 member" else "$memberCount members",
    isSelected = isSelected,
    sortKey = sortKey(),
)

private fun ChatMessage.toUi(currentUserId: String): ChatMessageItemUi {
    val isMine = senderUserId == currentUserId
    return ChatMessageItemUi(
        id = id,
        clientMessageId = clientMessageId,
        senderLabel = if (isMine) "You" else senderUserId.take(8),
        body = content,
        timeLabel = formatMessageTime(createdAt),
        isMine = isMine,
        deliveryLabel = null,
        sortKey = sortKey(),
    )
}

private fun ChatRoom.sortKey(): String = lastActivityAt ?: updatedAt ?: createdAt ?: ""

private fun ChatMessage.sortKey(): String = createdAt ?: updatedAt ?: ""

internal fun List<ChatMessageItemUi>.mergeIncomingMessage(
    incomingMessage: ChatMessage,
    currentUserId: String,
): List<ChatMessageItemUi> {
    val uiMessage = incomingMessage.toUi(currentUserId)
    val existingIndex = indexOfFirst { item ->
        item.id == incomingMessage.id || (
            incomingMessage.clientMessageId.isNotBlank() &&
                item.clientMessageId == incomingMessage.clientMessageId
            )
    }
    val pendingIndex = findPendingEchoMatchIndex(uiMessage)
    val updatedMessages = toMutableList()
    when {
        existingIndex >= 0 -> {
            updatedMessages[existingIndex] = uiMessage
            if (pendingIndex >= 0 && pendingIndex != existingIndex) {
                updatedMessages.removeAt(pendingIndex)
            }
        }

        pendingIndex >= 0 -> {
            updatedMessages[pendingIndex] = uiMessage
        }

        else -> {
            updatedMessages += uiMessage
        }
    }
    return updatedMessages.sortedBy { it.sortKey }
}

private fun List<ChatMessageItemUi>.findPendingEchoMatchIndex(
    incomingMessage: ChatMessageItemUi,
): Int {
    if (!incomingMessage.isMine) return -1

    return indexOfLast { message ->
        message.isPendingEchoCandidateFor(incomingMessage)
    }
}

private fun ChatMessageItemUi.isPendingEchoCandidateFor(
    incomingMessage: ChatMessageItemUi,
): Boolean {
    if (deliveryLabel == null || !isMine || !incomingMessage.isMine) return false
    if (body != incomingMessage.body) return false
    return true
}

private fun ChatMessageItemUi.canBeDeleted(): Boolean = isMine && deliveryLabel == null

internal data class ChatMessageDeletionUiResult(
    val rooms: List<ChatRoomItemUi>,
    val messages: List<ChatMessageItemUi>,
    val shouldRefreshRooms: Boolean,
)

internal fun applyDeletedMessageToUi(
    rooms: List<ChatRoomItemUi>,
    selectedRoomId: String?,
    messages: List<ChatMessageItemUi>,
    roomId: String,
    messageId: String,
): ChatMessageDeletionUiResult {
    if (selectedRoomId != roomId) {
        return ChatMessageDeletionUiResult(
            rooms = rooms,
            messages = messages,
            shouldRefreshRooms = true,
        )
    }

    val updatedMessages = messages.filterNot { message ->
        message.id == messageId
    }
    if (updatedMessages.size == messages.size) {
        return ChatMessageDeletionUiResult(
            rooms = rooms,
            messages = messages,
            shouldRefreshRooms = false,
        )
    }

    return ChatMessageDeletionUiResult(
        rooms = rooms.updatePreviewAfterDeletion(
            roomId = roomId,
            latestMessage = updatedMessages.lastOrNull(),
        ),
        messages = updatedMessages,
        shouldRefreshRooms = updatedMessages.isEmpty(),
    )
}

private fun List<ChatRoomItemUi>.markSelected(roomId: String): List<ChatRoomItemUi> =
    map { room -> room.copy(isSelected = room.id == roomId) }

private fun List<ChatRoomItemUi>.updatePreview(
    roomId: String,
    preview: String,
    sortKey: String,
): List<ChatRoomItemUi> = map { room ->
    if (room.id == roomId) {
        room.copy(
            preview = preview,
            activityLabel = formatRoomActivity(sortKey),
            sortKey = sortKey,
        )
    } else {
        room
    }
}.sortedByDescending { room -> room.sortKey }

private fun List<ChatRoomItemUi>.updatePreviewAfterDeletion(
    roomId: String,
    latestMessage: ChatMessageItemUi?,
): List<ChatRoomItemUi> = map { room ->
    if (room.id == roomId) {
        latestMessage?.let { message ->
            room.copy(
                preview = message.body,
                activityLabel = formatRoomActivity(message.sortKey),
                sortKey = message.sortKey,
            )
        } ?: room.copy(
            preview = "No messages yet",
        )
    } else {
        room
    }
}.sortedByDescending { room -> room.sortKey }

private fun formatRoomActivity(timestamp: String?): String {
    val localDateTime = timestamp.parseLocalDateTimeOrNull() ?: return "No activity yet"
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val timeLabel = "${localDateTime.hour.pad2()}:${localDateTime.minute.pad2()}"
    return if (now.date == localDateTime.date) {
        "Active $timeLabel"
    } else {
        "${localDateTime.date.monthNumber}/${localDateTime.date.dayOfMonth} $timeLabel"
    }
}

private fun formatMessageTime(timestamp: String?): String {
    val localDateTime = timestamp.parseLocalDateTimeOrNull() ?: return "Now"
    return "${localDateTime.hour.pad2()}:${localDateTime.minute.pad2()}"
}

private fun String?.parseInstantOrNull(): Instant? =
    this?.let { raw ->
        runCatching {
            Instant.parse(raw)
        }.getOrNull()
    }

private fun String?.parseLocalDateTimeOrNull() =
    parseInstantOrNull()?.toLocalDateTime(TimeZone.currentSystemDefault())

private fun Int.pad2(): String = toString().padStart(2, '0')
