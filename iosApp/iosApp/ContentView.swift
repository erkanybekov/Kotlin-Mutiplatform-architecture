import SwiftUI
import shared

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
                body: post.body
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

struct PostRowModel: Identifiable {
    let id: Int64
    let title: String
    let body: String
}

struct NoteRowModel: Identifiable {
    let id: Int64
    let title: String
    let body: String
    let isDone: Bool
}

enum StarterTab: String, CaseIterable, Identifiable {
    case posts = "Posts"
    case notes = "Notes"

    var id: String { rawValue }
}

struct ContentView: View {
	@StateObject private var postsState: PostsObservableState
    @StateObject private var notesState: NotesObservableState
    @State private var selectedTab: StarterTab = .posts
    @State private var noteTitle = ""
    @State private var noteBody = ""

    init(appGraph: SharedAppGraph) {
        _postsState = StateObject(wrappedValue: PostsObservableState(appGraph: appGraph))
        _notesState = StateObject(wrappedValue: NotesObservableState(appGraph: appGraph))
    }

	var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text("KMP Demo Workspace")
                    .font(.title)
                    .fontWeight(.bold)

                Text("Posts come from JSONPlaceholder, notes are saved locally on this device.")
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
                } else {
                    NotesSection(
                        state: notesState,
                        noteTitle: $noteTitle,
                        noteBody: $noteBody
                    )
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
            Button(state.isLoading ? "Refreshing..." : "Refresh posts") {
                state.refresh()
            }
            .buttonStyle(.borderedProminent)

            if let error = state.errorMessage {
                Text(error)
                    .foregroundStyle(.red)
            }

            List(state.items) { post in
                VStack(alignment: .leading, spacing: 8) {
                    Text(post.title)
                        .font(.headline)
                    Text(post.body)
                        .font(.body)
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
