import SwiftUI
import shared

struct SummaryCardModel: Identifiable, Equatable {
    let id: String
    let title: String
    let amountLabel: String
    let caption: String
    let accentHex: String
}

struct CategoryCardModel: Identifiable, Equatable {
    let id: String
    let name: String
    let amountLabel: String
    let shareLabel: String
    let progress: Double
    let accentHex: String
}

struct ChartPointModel: Identifiable, Equatable {
    let id: String
    let label: String
    let amount: Double
}

struct TransactionRowModel: Identifiable, Equatable {
    let id: Int64
    let title: String
    let subtitle: String
    let dateLabel: String
    let category: String
    let amountLabel: String
    let accentHex: String
    let isIncome: Bool
}

enum ChatAuthModeView: String, CaseIterable, Identifiable {
    case signIn = "Sign in"
    case createAccount = "Create account"

    var id: String { rawValue }
}

enum ChatConnectionViewState: String {
    case connected = "Live"
    case connecting = "Connecting"
    case disconnected = "Offline"
}

struct ChatRoomCardModel: Identifiable, Equatable {
    let id: String
    let name: String
    let preview: String
    let activityLabel: String
    let memberCountLabel: String
    let isSelected: Bool
}

struct ChatMessageModel: Identifiable, Equatable {
    let id: String
    let clientMessageId: String
    let senderLabel: String
    let body: String
    let timeLabel: String
    let isMine: Bool
    let deliveryLabel: String?
}

struct ChatRootViewState: Equatable {
    let authMode: ChatAuthModeView
    let displayName: String
    let email: String
    let password: String
    let displayNameError: String?
    let emailError: String?
    let passwordError: String?
    let isAuthenticating: Bool
    let isAuthenticated: Bool
    let currentUserDisplayName: String
    let currentUserEmail: String
    let rooms: [ChatRoomCardModel]
    let selectedRoomId: String?
    let selectedRoomName: String?
    let messages: [ChatMessageModel]
    let newRoomName: String
    let newRoomNameError: String?
    let composerText: String
    let isLoadingRooms: Bool
    let isLoadingMessages: Bool
    let isCreatingRoom: Bool
    let connectionState: ChatConnectionViewState
    let errorMessage: String?

    static let empty = ChatRootViewState(
        authMode: .signIn,
        displayName: "",
        email: "",
        password: "",
        displayNameError: nil,
        emailError: nil,
        passwordError: nil,
        isAuthenticating: false,
        isAuthenticated: false,
        currentUserDisplayName: "",
        currentUserEmail: "",
        rooms: [],
        selectedRoomId: nil,
        selectedRoomName: nil,
        messages: [],
        newRoomName: "",
        newRoomNameError: nil,
        composerText: "",
        isLoadingRooms: false,
        isLoadingMessages: false,
        isCreatingRoom: false,
        connectionState: .disconnected,
        errorMessage: nil
    )
}

@MainActor
final class ChatAppViewModel: ObservableObject {
    @Published private(set) var viewState = ChatRootViewState.empty

    private let stateHolder: ChatAppStateHolder
    private var observationHandle: ObservationHandle?

    init(appGraph: SharedAppGraph) {
        stateHolder = appGraph.chatAppStateHolder()
        sync(with: stateHolder.currentState)
        observationHandle = stateHolder.watch { [weak self] state in
            DispatchQueue.main.async {
                self?.sync(with: state)
            }
        }
    }

    func load() {
        stateHolder.load()
    }

    func switchAuthMode(_ mode: ChatAuthModeView) {
        switch mode {
        case .signIn:
            stateHolder.switchAuthMode(mode: ChatAuthMode.login)
        case .createAccount:
            stateHolder.switchAuthMode(mode: ChatAuthMode.signup)
        }
    }

    func updateDisplayName(_ value: String) {
        stateHolder.updateDisplayName(value: value)
    }

    func updateEmail(_ value: String) {
        stateHolder.updateEmail(value: value)
    }

    func updatePassword(_ value: String) {
        stateHolder.updatePassword(value: value)
    }

    func authenticate() {
        stateHolder.authenticate()
    }

    func dismissError() {
        stateHolder.dismissError()
    }

    func logout() {
        stateHolder.logout()
    }

    func updateNewRoomName(_ value: String) {
        stateHolder.updateNewRoomName(value: value)
    }

    func createRoom() {
        stateHolder.createRoom()
    }

    func selectRoom(_ roomId: String) {
        stateHolder.selectRoom(roomId: roomId)
    }

    func updateComposerText(_ value: String) {
        stateHolder.updateComposerText(value: value)
    }

    func sendMessage() {
        stateHolder.sendMessage()
    }

    deinit {
        observationHandle?.dispose()
    }

    private func sync(with state: ChatAppUiState) {
        viewState = ChatRootViewState(
            authMode: ChatAuthModeView(kotlinMode: state.authMode),
            displayName: state.displayName,
            email: state.email,
            password: state.password,
            displayNameError: state.displayNameError,
            emailError: state.emailError,
            passwordError: state.passwordError,
            isAuthenticating: state.isAuthenticating,
            isAuthenticated: state.isAuthenticated,
            currentUserDisplayName: state.currentUserDisplayName,
            currentUserEmail: state.currentUserEmail,
            rooms: state.rooms.map { room in
                ChatRoomCardModel(
                    id: room.id,
                    name: room.name,
                    preview: room.preview,
                    activityLabel: room.activityLabel,
                    memberCountLabel: room.memberCountLabel,
                    isSelected: room.isSelected
                )
            },
            selectedRoomId: state.selectedRoomId,
            selectedRoomName: state.selectedRoomName,
            messages: state.messages.map { message in
                ChatMessageModel(
                    id: message.id,
                    clientMessageId: message.clientMessageId,
                    senderLabel: message.senderLabel,
                    body: message.body,
                    timeLabel: message.timeLabel,
                    isMine: message.isMine,
                    deliveryLabel: message.deliveryLabel
                )
            },
            newRoomName: state.newRoomName,
            newRoomNameError: state.newRoomNameError,
            composerText: state.composerText,
            isLoadingRooms: state.isLoadingRooms,
            isLoadingMessages: state.isLoadingMessages,
            isCreatingRoom: state.isCreatingRoom,
            connectionState: ChatConnectionViewState(kotlinState: state.connectionState),
            errorMessage: state.errorMessage
        )
    }
}

private extension ChatAuthModeView {
    init(kotlinMode: ChatAuthMode) {
        switch kotlinMode {
        case ChatAuthMode.signup:
            self = .createAccount
        default:
            self = .signIn
        }
    }
}

private extension ChatConnectionViewState {
    init(kotlinState: ChatConnectionUiState) {
        switch kotlinState {
        case ChatConnectionUiState.connected:
            self = .connected
        case ChatConnectionUiState.connecting:
            self = .connecting
        default:
            self = .disconnected
        }
    }
}
