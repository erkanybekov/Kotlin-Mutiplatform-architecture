package com.erkan.experimentkmp.presentation.chat

enum class ChatAuthMode {
    LOGIN,
    SIGNUP,
}

enum class ChatConnectionUiState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
}

data class ChatRoomItemUi(
    val id: String,
    val name: String,
    val preview: String,
    val activityLabel: String,
    val memberCountLabel: String,
    val isSelected: Boolean,
    val sortKey: String,
)

data class ChatMessageItemUi(
    val id: String,
    val clientMessageId: String,
    val senderLabel: String,
    val body: String,
    val timeLabel: String,
    val isMine: Boolean,
    val deliveryLabel: String?,
    val sortKey: String,
)

data class ChatAppUiState(
    val authMode: ChatAuthMode = ChatAuthMode.LOGIN,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isAuthenticating: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUserDisplayName: String = "",
    val currentUserEmail: String = "",
    val rooms: List<ChatRoomItemUi> = emptyList(),
    val selectedRoomId: String? = null,
    val messages: List<ChatMessageItemUi> = emptyList(),
    val newRoomName: String = "",
    val newRoomNameError: String? = null,
    val inviteMemberEmail: String = "",
    val inviteMemberEmailError: String? = null,
    val composerText: String = "",
    val isLoadingRooms: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val isCreatingRoom: Boolean = false,
    val isInvitingMember: Boolean = false,
    val connectionState: ChatConnectionUiState = ChatConnectionUiState.DISCONNECTED,
    val errorMessage: String? = null,
) {
    val authActionLabel: String
        get() = if (authMode == ChatAuthMode.LOGIN) "Sign in" else "Create account"

    val authSwitchLabel: String
        get() = if (authMode == ChatAuthMode.LOGIN) {
            "Need an account? Create one"
        } else {
            "Already have an account? Sign in"
        }

    val selectedRoomName: String?
        get() = rooms.firstOrNull { it.id == selectedRoomId }?.name
}
