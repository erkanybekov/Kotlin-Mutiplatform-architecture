package com.erkan.experimentkmp.android.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.erkan.experimentkmp.android.ChatAppViewModel
import com.erkan.experimentkmp.android.uikit.ExpenseBackgroundBrush
import com.erkan.experimentkmp.android.uikit.ExpensePalette
import com.erkan.experimentkmp.presentation.chat.ChatAppUiState
import com.erkan.experimentkmp.presentation.chat.ChatAuthMode
import com.erkan.experimentkmp.presentation.chat.ChatConnectionUiState
import com.erkan.experimentkmp.presentation.chat.ChatMessageItemUi
import com.erkan.experimentkmp.presentation.chat.ChatRoomItemUi

@Composable
fun ChatAppScreen(
    state: ChatAppUiState,
    viewModel: ChatAppViewModel,
) {
    if (state.isAuthenticated) {
        ChatConversationScreen(
            state = state,
            onDismissError = viewModel::dismissError,
            onLogout = viewModel::logout,
            onNewRoomNameChange = viewModel::updateNewRoomName,
            onCreateRoom = viewModel::createRoom,
            onInviteMemberEmailChange = viewModel::updateInviteMemberEmail,
            onInviteMember = viewModel::inviteMember,
            onSelectRoom = viewModel::selectRoom,
            onComposerTextChange = viewModel::updateComposerText,
            onSendMessage = viewModel::sendMessage,
            onDeleteMessage = viewModel::deleteMessage,
        )
    } else {
        ChatAuthScreen(
            state = state,
            onModeSelected = viewModel::switchAuthMode,
            onDisplayNameChange = viewModel::updateDisplayName,
            onEmailChange = viewModel::updateEmail,
            onPasswordChange = viewModel::updatePassword,
            onAuthenticate = viewModel::authenticate,
            onDismissError = viewModel::dismissError,
        )
    }
}

@Composable
private fun ChatAuthScreen(
    state: ChatAppUiState,
    onModeSelected: (ChatAuthMode) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAuthenticate: () -> Unit,
    onDismissError: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExpenseBackgroundBrush)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "experimentKS Chat",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = ExpensePalette.TextPrimary,
                    )
                    Text(
                        text = "Sign in or create an account, join a room, and talk to your backend over websocket in real time.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ExpensePalette.TextSecondary,
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = ExpensePalette.Surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.authMode == ChatAuthMode.LOGIN,
                                onClick = { onModeSelected(ChatAuthMode.LOGIN) },
                                label = { Text("Sign in") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExpensePalette.AccentIndigo.copy(alpha = 0.18f),
                                    selectedLabelColor = ExpensePalette.AccentIndigo,
                                ),
                            )
                            FilterChip(
                                selected = state.authMode == ChatAuthMode.SIGNUP,
                                onClick = { onModeSelected(ChatAuthMode.SIGNUP) },
                                label = { Text("Create account") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExpensePalette.AccentWarm.copy(alpha = 0.18f),
                                    selectedLabelColor = ExpensePalette.AccentWarm,
                                ),
                            )
                        }

                        if (state.authMode == ChatAuthMode.SIGNUP) {
                            OutlinedTextField(
                                value = state.displayName,
                                onValueChange = onDisplayNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Display name") },
                                isError = state.displayNameError != null,
                                supportingText = state.displayNameError?.let { error ->
                                    { Text(error, color = MaterialTheme.colorScheme.error) }
                                },
                            )
                        }

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email") },
                            isError = state.emailError != null,
                            supportingText = state.emailError?.let { error ->
                                { Text(error, color = MaterialTheme.colorScheme.error) }
                            },
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            isError = state.passwordError != null,
                            supportingText = state.passwordError?.let { error ->
                                { Text(error, color = MaterialTheme.colorScheme.error) }
                            },
                        )

                        state.errorMessage?.let { message ->
                            ChatErrorCard(
                                message = message,
                                onDismiss = onDismissError,
                            )
                        }

                        Button(
                            onClick = onAuthenticate,
                            enabled = !state.isAuthenticating,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (state.isAuthenticating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(state.authActionLabel)
                            }
                        }

                        Text(
                            text = state.authSwitchLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpensePalette.TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatConversationScreen(
    state: ChatAppUiState,
    onDismissError: () -> Unit,
    onLogout: () -> Unit,
    onNewRoomNameChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onInviteMemberEmailChange: (String) -> Unit,
    onInviteMember: () -> Unit,
    onSelectRoom: (String) -> Unit,
    onComposerTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDeleteMessage: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.selectedRoomId) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = ExpensePalette.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Chat")
                        Text(
                            text = state.selectedRoomName ?: "Choose a room",
                            style = MaterialTheme.typography.labelLarge,
                            color = ExpensePalette.TextSecondary,
                        )
                    }
                },
                actions = {
                    ConnectionBadge(state.connectionState)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                },
            )
        },
        bottomBar = {
            ChatComposerBar(
                state = state,
                onComposerTextChange = onComposerTextChange,
                onSendMessage = onSendMessage,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            UserHeaderCard(state)

            RoomCreationCard(
                state = state,
                onNewRoomNameChange = onNewRoomNameChange,
                onCreateRoom = onCreateRoom,
                onInviteMemberEmailChange = onInviteMemberEmailChange,
                onInviteMember = onInviteMember,
            )

            if (state.rooms.isNotEmpty()) {
                RoomsRow(
                    rooms = state.rooms,
                    onSelectRoom = onSelectRoom,
                )
            }

            state.errorMessage?.let { message ->
                ChatErrorCard(
                    message = message,
                    onDismiss = onDismissError,
                )
            }

            MessagesPane(
                state = state,
                listState = listState,
                onDeleteMessage = onDeleteMessage,
            )
        }
    }
}

@Composable
private fun UserHeaderCard(
    state: ChatAppUiState,
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = ExpensePalette.Surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = state.currentUserDisplayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ExpensePalette.TextPrimary,
            )
            Text(
                text = state.currentUserEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = ExpensePalette.TextSecondary,
            )
        }
    }
}

@Composable
private fun RoomCreationCard(
    state: ChatAppUiState,
    onNewRoomNameChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onInviteMemberEmailChange: (String) -> Unit,
    onInviteMember: () -> Unit,
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = ExpensePalette.Surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Rooms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = state.newRoomName,
                    onValueChange = onNewRoomNameChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("New room name") },
                    isError = state.newRoomNameError != null,
                    supportingText = state.newRoomNameError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                )
                Button(
                    onClick = onCreateRoom,
                    enabled = !state.isCreatingRoom,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    if (state.isCreatingRoom) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Create")
                    }
                }
            }

            Text(
                text = "Invite member",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = state.inviteMemberEmail,
                    onValueChange = onInviteMemberEmailChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Member email") },
                    enabled = state.selectedRoomId != null,
                    isError = state.inviteMemberEmailError != null,
                    supportingText = state.inviteMemberEmailError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                )
                Button(
                    onClick = onInviteMember,
                    enabled = state.selectedRoomId != null && !state.isInvitingMember,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    if (state.isInvitingMember) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Invite")
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomsRow(
    rooms: List<ChatRoomItemUi>,
    onSelectRoom: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(rooms, key = { it.id }) { room ->
            FilterChip(
                selected = room.isSelected,
                onClick = { onSelectRoom(room.id) },
                label = {
                    Column {
                        Text(
                            text = room.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = room.preview,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExpensePalette.AccentIndigo.copy(alpha = 0.18f),
                    selectedLabelColor = ExpensePalette.TextPrimary,
                ),
            )
        }
    }
}

@Composable
private fun ColumnScope.MessagesPane(
    state: ChatAppUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onDeleteMessage: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        colors = CardDefaults.elevatedCardColors(
            containerColor = ExpensePalette.Surface,
        ),
    ) {
        when {
            state.isLoadingMessages -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.selectedRoomId == null -> {
                ChatEmptyState(
                    title = "No room selected",
                    message = "Create a room or choose one above to start the conversation.",
                )
            }

            state.messages.isEmpty() -> {
                ChatEmptyState(
                    title = "No messages yet",
                    message = "Say hello and your websocket backend should stream the conversation here.",
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.messages,
                        key = { message -> message.id },
                    ) { message ->
                        MessageBubble(
                            message = message,
                            onDeleteMessage = onDeleteMessage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    state: ChatAppUiState,
    onComposerTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
) {
    Surface(
        tonalElevation = 6.dp,
        color = ExpensePalette.SurfaceStrong,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .windowInsetsPadding(WindowInsets.ime),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = state.composerText,
                onValueChange = onComposerTextChange,
                modifier = Modifier.weight(1f),
                label = { Text("Message") },
                enabled = state.selectedRoomId != null,
                minLines = 1,
                maxLines = 4,
            )
            Button(
                onClick = onSendMessage,
                enabled = state.selectedRoomId != null && state.composerText.isNotBlank(),
                modifier = Modifier.height(56.dp),
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageItemUi,
    onDeleteMessage: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.82f),
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
        ) {
            if (!message.isMine) {
                Text(
                    text = message.senderLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = ExpensePalette.TextMuted,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Surface(
                color = if (message.isMine) {
                    ExpensePalette.AccentIndigo.copy(alpha = 0.22f)
                } else {
                    ExpensePalette.SurfaceInset
                },
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = message.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ExpensePalette.TextPrimary,
                    )
                    Text(
                        text = listOfNotNull(message.timeLabel, message.deliveryLabel).joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpensePalette.TextMuted,
                    )
                }
            }

            if (message.isMine && message.deliveryLabel == null) {
                TextButton(
                    onClick = { onDeleteMessage(message.id) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        color = ExpensePalette.Error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyState(
    title: String,
    message: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ExpensePalette.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ChatErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = ExpensePalette.Error.copy(alpha = 0.14f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = ExpensePalette.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun ConnectionBadge(
    connectionState: ChatConnectionUiState,
) {
    val accent = when (connectionState) {
        ChatConnectionUiState.CONNECTED -> ExpensePalette.AccentSuccess
        ChatConnectionUiState.CONNECTING -> ExpensePalette.AccentGold
        ChatConnectionUiState.DISCONNECTED -> ExpensePalette.Error
    }
    val label = when (connectionState) {
        ChatConnectionUiState.CONNECTED -> "Live"
        ChatConnectionUiState.CONNECTING -> "Connecting"
        ChatConnectionUiState.DISCONNECTED -> "Offline"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}
