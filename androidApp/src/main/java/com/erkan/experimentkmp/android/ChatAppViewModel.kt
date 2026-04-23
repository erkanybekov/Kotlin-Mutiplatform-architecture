package com.erkan.experimentkmp.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erkan.experimentkmp.presentation.chat.ChatAppStateHolder
import com.erkan.experimentkmp.presentation.chat.ChatAppUiState
import com.erkan.experimentkmp.presentation.chat.ChatAuthMode
import kotlinx.coroutines.flow.StateFlow

class ChatAppViewModel(
    private val stateHolder: ChatAppStateHolder,
) : ViewModel() {
    val state: StateFlow<ChatAppUiState> = stateHolder.state

    fun load() = stateHolder.load()

    fun switchAuthMode(mode: ChatAuthMode) = stateHolder.switchAuthMode(mode)

    fun updateDisplayName(value: String) = stateHolder.updateDisplayName(value)

    fun updateEmail(value: String) = stateHolder.updateEmail(value)

    fun updatePassword(value: String) = stateHolder.updatePassword(value)

    fun authenticate() = stateHolder.authenticate()

    fun dismissError() = stateHolder.dismissError()

    fun logout() = stateHolder.logout()

    fun updateNewRoomName(value: String) = stateHolder.updateNewRoomName(value)

    fun createRoom() = stateHolder.createRoom()

    fun selectRoom(roomId: String) = stateHolder.selectRoom(roomId)

    fun updateComposerText(value: String) = stateHolder.updateComposerText(value)

    fun sendMessage() = stateHolder.sendMessage()

    fun deleteMessage(messageId: String) = stateHolder.deleteMessage(messageId)
}

class ChatAppViewModelFactory(
    private val stateHolder: ChatAppStateHolder,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatAppViewModel(stateHolder) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
