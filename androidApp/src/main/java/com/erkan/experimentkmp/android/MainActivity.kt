package com.erkan.experimentkmp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.erkan.experimentkmp.presentation.notes.NoteItemUi
import com.erkan.experimentkmp.presentation.notes.NotesStateHolder
import com.erkan.experimentkmp.presentation.notes.NotesUiState
import com.erkan.experimentkmp.presentation.posts.PostItemUi
import com.erkan.experimentkmp.presentation.posts.PostsStateHolder
import com.erkan.experimentkmp.presentation.posts.PostsUiState
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val postsStateHolder: PostsStateHolder by inject()
    private val notesStateHolder: NotesStateHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StarterRoute(
                        postsStateHolder = postsStateHolder,
                        notesStateHolder = notesStateHolder,
                    )
                }
            }
        }
    }
}

@Composable
private fun StarterRoute(
    postsStateHolder: PostsStateHolder,
    notesStateHolder: NotesStateHolder,
) {
    val postsState by postsStateHolder.state.collectAsState()
    val notesState by notesStateHolder.state.collectAsState()
    var selectedTab by remember { mutableStateOf(StarterTab.Posts) }

    LaunchedEffect(postsStateHolder, notesStateHolder) {
        postsStateHolder.load()
        notesStateHolder.load()
    }

    StarterScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        postsState = postsState,
        notesState = notesState,
        onRefreshPosts = postsStateHolder::refresh,
        onRefreshNotes = notesStateHolder::refresh,
        onAddNote = notesStateHolder::addNote,
        onToggleNote = notesStateHolder::toggleNote,
    )
}

@Composable
private fun StarterScreen(
    selectedTab: StarterTab,
    onTabSelected: (StarterTab) -> Unit,
    postsState: PostsUiState,
    notesState: NotesUiState,
    onRefreshPosts: () -> Unit,
    onRefreshNotes: () -> Unit,
    onAddNote: (String, String) -> Unit,
    onToggleNote: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("KMP Demo Workspace", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Posts come from JSONPlaceholder, notes are stored locally on device.",
            style = MaterialTheme.typography.bodyMedium,
        )

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            StarterTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.label) },
                )
            }
        }

        when (selectedTab) {
            StarterTab.Posts -> PostsPane(postsState, onRefreshPosts)
            StarterTab.Notes -> NotesPane(notesState, onRefreshNotes, onAddNote, onToggleNote)
        }
    }
}

@Preview
@Composable
private fun StarterScreenPreview() {
    MyApplicationTheme {
        StarterScreen(
            selectedTab = StarterTab.Posts,
            onTabSelected = {},
            postsState = PostsUiState(
                posts = listOf(
                    PostItemUi(1, "Preview post", "Loaded from the public API."),
                ),
            ),
            notesState = NotesUiState(
                notes = listOf(
                    NoteItemUi(1, "Offline note", "Stored locally on device.", false),
                ),
            ),
            onRefreshPosts = {},
            onRefreshNotes = {},
            onAddNote = { _, _ -> },
            onToggleNote = {},
        )
    }
}

private enum class StarterTab(val label: String) {
    Posts("Posts"),
    Notes("Notes"),
}

@Composable
private fun PostsPane(
    state: PostsUiState,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onRefresh) {
            Text(if (state.isLoading) "Refreshing..." else "Refresh posts")
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.posts, key = { it.id }) { post ->
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(post.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesPane(
    state: NotesUiState,
    onRefresh: () -> Unit,
    onAddNote: (String, String) -> Unit,
    onToggleNote: (Long) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note title") },
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            label = { Text("Note body") },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    onAddNote(title, body)
                    title = ""
                    body = ""
                },
            ) {
                Text(if (state.isLoading) "Saving..." else "Save note")
            }
            Button(onClick = onRefresh) {
                Text("Reload")
            }
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.notes.forEach { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleNote(note.id) },
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(note.body, style = MaterialTheme.typography.bodyMedium)
                        HorizontalDivider()
                        Text(
                            if (note.isDone) "Status: done" else "Status: active",
                            color = if (note.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            if (state.notes.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text("No notes yet. Add your first one above.")
                }
            }
        }
    }
}
