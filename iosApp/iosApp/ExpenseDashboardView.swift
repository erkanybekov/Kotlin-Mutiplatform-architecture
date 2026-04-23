import SwiftUI
import shared

struct ChatAppView: View {
    @StateObject private var viewModel: ChatAppViewModel

    init(appGraph: SharedAppGraph) {
        _viewModel = StateObject(wrappedValue: ChatAppViewModel(appGraph: appGraph))
    }

    var body: some View {
        let state = viewModel.viewState

        Group {
            if state.isAuthenticated {
                authenticatedView(state)
            } else {
                authView(state)
            }
        }
        .task {
            viewModel.load()
        }
    }

    private func authView(_ state: ChatRootViewState) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("experimentKS Chat")
                        .font(.system(size: 38, weight: .heavy, design: .rounded))
                        .foregroundStyle(ExpensePalette.textPrimary)

                    Text("Sign in or create an account, join a room, and talk to your websocket backend in real time.")
                        .font(.body)
                        .foregroundStyle(ExpensePalette.textSecondary)
                }

                VStack(alignment: .leading, spacing: 16) {
                    Picker("Mode", selection: authModeBinding(state)) {
                        ForEach(ChatAuthModeView.allCases) { mode in
                            Text(mode.rawValue).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)

                    if state.authMode == .createAccount {
                        VStack(alignment: .leading, spacing: 6) {
                            TextField("Display name", text: binding(state.displayName, setter: viewModel.updateDisplayName))
                                .textFieldStyle(.roundedBorder)
                            if let error = state.displayNameError {
                                ExpenseValidationMessage(text: error)
                            }
                        }
                    }

                    VStack(alignment: .leading, spacing: 6) {
                        TextField("Email", text: binding(state.email, setter: viewModel.updateEmail))
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .textFieldStyle(.roundedBorder)
                        if let error = state.emailError {
                            ExpenseValidationMessage(text: error)
                        }
                    }

                    VStack(alignment: .leading, spacing: 6) {
                        SecureField("Password", text: binding(state.password, setter: viewModel.updatePassword))
                            .textFieldStyle(.roundedBorder)
                        if let error = state.passwordError {
                            ExpenseValidationMessage(text: error)
                        }
                    }

                    if let error = state.errorMessage {
                        ChatErrorBanner(message: error, action: viewModel.dismissError)
                    }

                    Button {
                        viewModel.authenticate()
                    } label: {
                        if state.isAuthenticating {
                            ProgressView()
                                .frame(maxWidth: .infinity)
                        } else {
                            Text(state.authMode == .signIn ? "Sign in" : "Create account")
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(state.isAuthenticating)

                    Text(state.authMode == .signIn ? "Need an account? Create one above." : "Already have an account? Switch back to sign in.")
                        .font(.caption)
                        .foregroundStyle(ExpensePalette.textMuted)
                        .frame(maxWidth: .infinity, alignment: .center)
                }
                .padding(20)
                .background(
                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                        .fill(ExpensePalette.surface)
                )
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 28)
        }
        .background(
            LinearGradient(
                colors: [ExpensePalette.background, ExpensePalette.surfaceInset],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
        )
    }

    private func authenticatedView(_ state: ChatRootViewState) -> some View {
        NavigationStack {
            VStack(spacing: 14) {
                userHeader(state)
                roomCreation(state)

                if !state.rooms.isEmpty {
                    roomSelector(state)
                }

                if let error = state.errorMessage {
                    ChatErrorBanner(message: error, action: viewModel.dismissError)
                }

                messageSection(state)
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(ExpensePalette.background)
            .navigationTitle("Chat")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    ConnectionPill(state: state.connectionState)
                    Button("Logout") {
                        viewModel.logout()
                    }
                }
            }
            .safeAreaInset(edge: .bottom) {
                composer(state)
            }
        }
    }

    private func userHeader(_ state: ChatRootViewState) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(state.currentUserDisplayName)
                .font(.title2.weight(.bold))
                .foregroundStyle(ExpensePalette.textPrimary)
            Text(state.currentUserEmail)
                .font(.subheadline)
                .foregroundStyle(ExpensePalette.textSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .background(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .fill(ExpensePalette.surface)
        )
    }

    private func roomCreation(_ state: ChatRootViewState) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Rooms")
                .font(.headline)

            HStack(alignment: .top, spacing: 10) {
                VStack(alignment: .leading, spacing: 6) {
                    TextField("New room name", text: binding(state.newRoomName, setter: viewModel.updateNewRoomName))
                        .textFieldStyle(.roundedBorder)
                    if let error = state.newRoomNameError {
                        ExpenseValidationMessage(text: error)
                    }
                }

                Button {
                    viewModel.createRoom()
                } label: {
                    if state.isCreatingRoom {
                        ProgressView()
                    } else {
                        Text("Create")
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(state.isCreatingRoom)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .background(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .fill(ExpensePalette.surface)
        )
    }

    private func roomSelector(_ state: ChatRootViewState) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(state.rooms) { room in
                    Button {
                        viewModel.selectRoom(room.id)
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(room.name)
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(ExpensePalette.textPrimary)
                            Text(room.preview)
                                .font(.caption)
                                .foregroundStyle(ExpensePalette.textSecondary)
                                .lineLimit(1)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 12)
                        .frame(width: 180, alignment: .leading)
                        .background(
                            RoundedRectangle(cornerRadius: 18, style: .continuous)
                                .fill(room.isSelected ? ExpensePalette.accentIndigo.opacity(0.18) : ExpensePalette.surfaceInset)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.vertical, 2)
        }
    }

    private func messageSection(_ state: ChatRootViewState) -> some View {
        Group {
            if state.isLoadingMessages {
                VStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .frame(maxWidth: .infinity)
            } else if state.selectedRoomId == nil {
                ChatEmptyState(
                    title: "No room selected",
                    message: "Create a room or choose one above to start the conversation."
                )
            } else if state.messages.isEmpty {
                ChatEmptyState(
                    title: "No messages yet",
                    message: "Say hello and your websocket backend should stream the conversation here."
                )
            } else {
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(state.messages) { message in
                                ChatMessageBubble(
                                    message: message,
                                    onDelete: viewModel.deleteMessage
                                )
                                    .id(message.id)
                            }
                        }
                        .padding(16)
                    }
                    .onAppear {
                        if let lastId = state.messages.last?.id {
                            proxy.scrollTo(lastId, anchor: .bottom)
                        }
                    }
                    .onChange(of: state.messages.map(\.id)) { _ in
                        if let lastId = state.messages.last?.id {
                            withAnimation {
                                proxy.scrollTo(lastId, anchor: .bottom)
                            }
                        }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .fill(ExpensePalette.surface)
        )
    }

    private func composer(_ state: ChatRootViewState) -> some View {
        HStack(spacing: 10) {
            TextField("Message", text: binding(state.composerText, setter: viewModel.updateComposerText), axis: .vertical)
                .lineLimit(1...4)
                .textFieldStyle(.roundedBorder)
                .disabled(state.selectedRoomId == nil)

            Button("Send") {
                viewModel.sendMessage()
            }
            .buttonStyle(.borderedProminent)
            .disabled(state.selectedRoomId == nil || state.composerText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(ExpensePalette.surfaceStrong)
    }

    private func binding(
        _ value: String,
        setter: @escaping (String) -> Void
    ) -> Binding<String> {
        Binding(
            get: { value },
            set: setter
        )
    }

    private func authModeBinding(_ state: ChatRootViewState) -> Binding<ChatAuthModeView> {
        Binding(
            get: { state.authMode },
            set: { viewModel.switchAuthMode($0) }
        )
    }
}

private struct ChatMessageBubble: View {
    let message: ChatMessageModel
    let onDelete: (String) -> Void

    var body: some View {
        HStack {
            if message.isMine { Spacer(minLength: 48) }

            VStack(alignment: message.isMine ? .trailing : .leading, spacing: 4) {
                if !message.isMine {
                    Text(message.senderLabel)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(ExpensePalette.textMuted)
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text(message.body)
                        .font(.body)
                        .foregroundStyle(ExpensePalette.textPrimary)

                    Text([message.timeLabel, message.deliveryLabel].compactMap { $0 }.joined(separator: " • "))
                        .font(.caption2)
                        .foregroundStyle(ExpensePalette.textMuted)
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 20, style: .continuous)
                        .fill(message.isMine ? ExpensePalette.accentIndigo.opacity(0.18) : ExpensePalette.surfaceInset)
                )

                if message.isMine && message.deliveryLabel == nil {
                    Button(role: .destructive) {
                        onDelete(message.id)
                    } label: {
                        Text("Delete")
                            .font(.caption.weight(.semibold))
                    }
                    .buttonStyle(.plain)
                }
            }

            if !message.isMine { Spacer(minLength: 48) }
        }
        .frame(maxWidth: .infinity)
    }
}

private struct ChatEmptyState: View {
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: 8) {
            Text(title)
                .font(.headline)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(ExpensePalette.textSecondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}

private struct ChatErrorBanner: View {
    let message: String
    let action: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Text(message)
                .font(.subheadline)
                .foregroundStyle(ExpensePalette.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Button("Dismiss", action: action)
                .font(.subheadline.weight(.semibold))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(ExpensePalette.error.opacity(0.14))
        )
    }
}

private struct ConnectionPill: View {
    let state: ChatConnectionViewState

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(color)
                .frame(width: 8, height: 8)
            Text(state.rawValue)
                .font(.caption.weight(.semibold))
                .foregroundStyle(ExpensePalette.textPrimary)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 7)
        .background(
            Capsule(style: .continuous)
                .fill(color.opacity(0.16))
        )
    }

    private var color: Color {
        switch state {
        case .connected:
            return ExpensePalette.accentSuccess
        case .connecting:
            return ExpensePalette.accentGold
        case .disconnected:
            return ExpensePalette.error
        }
    }
}
