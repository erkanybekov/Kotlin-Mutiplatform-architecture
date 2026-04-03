import SwiftUI
import shared

struct LogRowModel: Identifiable {
    let id: Int64
    let level: String
    let category: String
    let message: String
    let details: String?
    let timestamp: Date
}

@MainActor
final class PostsObservableState: ObservableObject {
    @Published private(set) var items: [PostRowModel] = []
    @Published private(set) var isLoading = false
    @Published private(set) var errorMessage: String?

    private let stateHolder: PostsStateHolder
    private var observationHandle: ObservationHandle?

    init(appGraph: SharedAppGraph) {
        stateHolder = appGraph.postsStateHolder()
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

    func refresh() {
        stateHolder.refresh()
    }

    deinit {
        observationHandle?.dispose()
    }

    private func sync(with state: PostsUiState) {
        items = state.posts.map { post in
            PostRowModel(
                id: post.id,
                title: post.title,
                body: post.body,
                priceLabel: post.priceLabel,
                category: post.category,
                imageUrl: post.imageUrl
            )
        }
        isLoading = state.isLoading
        errorMessage = state.errorMessage
    }
}

@MainActor
final class NotesObservableState: ObservableObject {
    @Published private(set) var items: [NoteRowModel] = []
    @Published private(set) var isLoading = false
    @Published private(set) var errorMessage: String?

    private let stateHolder: NotesStateHolder
    private var observationHandle: ObservationHandle?

    init(appGraph: SharedAppGraph) {
        stateHolder = appGraph.notesStateHolder()
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

    func refresh() {
        stateHolder.refresh()
    }

    func addNote(title: String, body: String) {
        stateHolder.addNote(title: title, body: body)
    }

    func toggleNote(id: Int64) {
        stateHolder.toggleNote(id: id)
    }

    deinit {
        observationHandle?.dispose()
    }

    private func sync(with state: NotesUiState) {
        items = state.notes.map { note in
            NoteRowModel(
                id: note.id,
                title: note.title,
                body: note.body,
                isDone: note.isDone
            )
        }
        isLoading = state.isLoading
        errorMessage = state.errorMessage
    }
}

@MainActor
final class LogsObservableState: ObservableObject {
    @Published private(set) var items: [LogRowModel] = []

    private let stateHolder: LogsStateHolder
    private var observationHandle: ObservationHandle?

    init(appGraph: SharedAppGraph) {
        stateHolder = appGraph.logsStateHolder()
        sync(with: stateHolder.currentState)
        observationHandle = stateHolder.watch { [weak self] state in
            DispatchQueue.main.async {
                self?.sync(with: state)
            }
        }
    }

    func clear() {
        stateHolder.clearLogs()
    }

    deinit {
        observationHandle?.dispose()
    }

    private func sync(with state: LogsUiState) {
        items = state.entries.map { entry in
            LogRowModel(
                id: entry.id,
                level: entry.level,
                category: entry.category,
                message: entry.message,
                details: entry.details,
                timestamp: Date(timeIntervalSince1970: TimeInterval(entry.timestampEpochMillis) / 1000)
            )
        }
    }
}

struct PostRowModel: Identifiable {
    let id: Int64
    let title: String
    let body: String
    let priceLabel: String
    let category: String
    let imageUrl: String
}

struct NoteRowModel: Identifiable {
    let id: Int64
    let title: String
    let body: String
    let isDone: Bool
}

enum StarterTab: String, CaseIterable, Identifiable {
    case posts = "Products"
    case notes = "Notes"
    case logs = "Logs"

    var id: String { rawValue }
}

struct ContentView: View {
	@StateObject private var postsState: PostsObservableState
    @StateObject private var notesState: NotesObservableState
    @StateObject private var logsState: LogsObservableState
    @State private var selectedTab: StarterTab = .posts
    @State private var noteTitle = ""
    @State private var noteBody = ""

    init(appGraph: SharedAppGraph) {
        _postsState = StateObject(wrappedValue: PostsObservableState(appGraph: appGraph))
        _notesState = StateObject(wrappedValue: NotesObservableState(appGraph: appGraph))
        _logsState = StateObject(wrappedValue: LogsObservableState(appGraph: appGraph))
    }

	var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text("KMP Commerce Lab")
                    .font(.title)
                    .fontWeight(.bold)

                Text("Products come from DummyJSON, notes stay local, and logs stream from the shared Ktor client.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                Picker("Section", selection: $selectedTab) {
                    ForEach(StarterTab.allCases) { tab in
                        Text(tab.rawValue).tag(tab)
                    }
                }
                .pickerStyle(.segmented)

                if selectedTab == .posts {
                    PostsSection(state: postsState)
                } else if selectedTab == .notes {
                    NotesSection(
                        state: notesState,
                        noteTitle: $noteTitle,
                        noteBody: $noteBody
                    )
                } else {
                    LogsSection(state: logsState)
                }
            }
            .padding(20)
            .navigationTitle("Starter")
        }
        .onAppear {
            postsState.load()
            notesState.load()
        }
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView(appGraph: SharedAppGraph(storageDirectoryPath: NSTemporaryDirectory()))
	}
}

private struct PostsSection: View {
    @ObservedObject var state: PostsObservableState

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button(state.isLoading ? "Refreshing..." : "Refresh products") {
                state.refresh()
            }
            .buttonStyle(.borderedProminent)

            if let error = state.errorMessage {
                Text(error)
                    .foregroundStyle(.red)
            }

            List(state.items) { post in
                HStack(alignment: .top, spacing: 14) {
                    AsyncImage(url: URL(string: post.imageUrl)) { image in
                        image
                            .resizable()
                            .scaledToFill()
                    } placeholder: {
                        RoundedRectangle(cornerRadius: 18)
                            .fill(Color.gray.opacity(0.15))
                    }
                    .frame(width: 86, height: 86)
                    .clipShape(RoundedRectangle(cornerRadius: 18))

                    VStack(alignment: .leading, spacing: 8) {
                        HStack(alignment: .top) {
                            Text(post.title)
                                .font(.headline)
                            Spacer(minLength: 12)
                            Text(post.priceLabel)
                                .font(.headline)
                                .foregroundStyle(.blue)
                        }
                        Text(post.category)
                            .font(.caption)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.secondary.opacity(0.12))
                            .clipShape(Capsule())
                        Text(post.body)
                            .font(.body)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(.vertical, 6)
            }
            .listStyle(.plain)
        }
    }
}

private struct NotesSection: View {
    @ObservedObject var state: NotesObservableState
    @Binding var noteTitle: String
    @Binding var noteBody: String

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("Note title", text: $noteTitle)
                .textFieldStyle(.roundedBorder)

            TextField("Note body", text: $noteBody, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(4, reservesSpace: true)

            HStack(spacing: 12) {
                Button(state.isLoading ? "Saving..." : "Save note") {
                    state.addNote(title: noteTitle, body: noteBody)
                    noteTitle = ""
                    noteBody = ""
                }
                .buttonStyle(.borderedProminent)

                Button("Reload") {
                    state.refresh()
                }
                .buttonStyle(.bordered)
            }

            if let error = state.errorMessage {
                Text(error)
                    .foregroundStyle(.red)
            }

            List(state.items) { note in
                Button {
                    state.toggleNote(id: note.id)
                } label: {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(note.title)
                            .font(.headline)
                        Text(note.body)
                            .font(.body)
                        Text(note.isDone ? "Status: done" : "Status: active")
                            .font(.caption)
                            .foregroundStyle(note.isDone ? .green : .secondary)
                    }
                    .padding(.vertical, 6)
                }
                .buttonStyle(.plain)
            }
            .listStyle(.plain)
        }
    }
}

private struct LogsSection: View {
    @ObservedObject var state: LogsObservableState

    private static let formatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss.SSS"
        return formatter
    }()

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Button("Clear logs") {
                    state.clear()
                }
                .buttonStyle(.borderedProminent)

                Text("\(state.items.count) entries")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            if state.items.isEmpty {
                Text("No logs yet. Refresh products to generate HTTP events.")
                    .foregroundStyle(.secondary)
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 24)
                        .fill(Color(red: 0.06, green: 0.08, blue: 0.10))

                    List(state.items) { item in
                        VStack(alignment: .leading, spacing: 8) {
                            HStack(spacing: 8) {
                                Text(item.level)
                                    .font(.caption2.weight(.bold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(color(for: item.level))
                                    .foregroundStyle(.black)
                                    .clipShape(Capsule())
                                Text(Self.formatter.string(from: item.timestamp))
                                    .font(.system(.caption, design: .monospaced))
                                    .foregroundStyle(Color(red: 0.58, green: 0.64, blue: 0.72))
                            }
                            Text("[\(item.category)] \(item.message)")
                                .font(.system(.footnote, design: .monospaced))
                                .foregroundStyle(Color(red: 0.89, green: 0.91, blue: 0.95))
                            if let details = item.details, !details.isEmpty {
                                Text(details)
                                    .font(.system(.caption, design: .monospaced))
                                    .foregroundStyle(Color(red: 0.39, green: 0.45, blue: 0.54))
                            }
                        }
                        .padding(.vertical, 8)
                        .listRowBackground(Color.clear)
                    }
                    .scrollContentBackground(.hidden)
                    .listStyle(.plain)
                    .padding(6)
                }
            }
        }
    }

    private func color(for level: String) -> Color {
        switch level {
        case "ERROR":
            return .red
        case "INFO":
            return .blue
        default:
            return .secondary
        }
    }
}
