package com.erkan.experimentkmp.android

import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.erkan.experimentkmp.logging.LogEntry
import com.erkan.experimentkmp.presentation.logs.LogsStateHolder
import com.erkan.experimentkmp.presentation.logs.LogsUiState
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
    private val logsStateHolder: LogsStateHolder by inject()

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
                        logsStateHolder = logsStateHolder,
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
    logsStateHolder: LogsStateHolder,
) {
    val postsState by postsStateHolder.state.collectAsState()
    val notesState by notesStateHolder.state.collectAsState()
    val logsState by logsStateHolder.state.collectAsState()
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
        logsState = logsState,
        onRefreshPosts = postsStateHolder::refresh,
        onRefreshNotes = notesStateHolder::refresh,
        onAddNote = notesStateHolder::addNote,
        onToggleNote = notesStateHolder::toggleNote,
        onClearLogs = logsStateHolder::clearLogs,
    )
}

@Composable
private fun StarterScreen(
    selectedTab: StarterTab,
    onTabSelected: (StarterTab) -> Unit,
    postsState: PostsUiState,
    notesState: NotesUiState,
    logsState: LogsUiState,
    onRefreshPosts: () -> Unit,
    onRefreshNotes: () -> Unit,
    onAddNote: (String, String) -> Unit,
    onToggleNote: (Long) -> Unit,
    onClearLogs: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("KMP Commerce Lab", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Products come from DummyJSON, notes stay local, and logs stream from the shared Ktor client.",
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
            StarterTab.Logs -> LogsPane(logsState, onClearLogs)
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
                    PostItemUi(
                        1,
                        "Leather Weekend Bag",
                        "Compact carry for short city trips.",
                        "$129.99",
                        "Accessories",
                        "https://cdn.dummyjson.com/product-images/mens-shirts/essence-mascara-lash-princess/thumbnail.webp",
                    ),
                ),
            ),
            notesState = NotesUiState(
                notes = listOf(
                    NoteItemUi(1, "Offline note", "Stored locally on device.", false),
                ),
            ),
            logsState = LogsUiState(
                entries = listOf(
                    LogEntry(
                        id = 1,
                        timestampEpochMillis = 1_710_000_000_000,
                        level = "INFO",
                        category = "http",
                        message = "RESPONSE: 200 OK",
                        details = "engine=okhttp",
                    ),
                ),
            ),
            onRefreshPosts = {},
            onRefreshNotes = {},
            onAddNote = { _, _ -> },
            onToggleNote = {},
            onClearLogs = {},
        )
    }
}

private enum class StarterTab(val label: String) {
    Posts("Products"),
    Notes("Notes"),
    Logs("Logs"),
}

@Composable
private fun PostsPane(
    state: PostsUiState,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onRefresh) {
            Text(if (state.isLoading) "Refreshing..." else "Refresh products")
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.posts, key = { it.id }) { post ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = post.title,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(18.dp)),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    post.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Text(
                                    post.priceLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(post.category) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            )
                            Text(post.body, style = MaterialTheme.typography.bodyMedium)
                        }
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

@Composable
private fun LogsPane(
    state: LogsUiState,
    onClearLogs: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onClearLogs) {
                Text("Clear logs")
            }
            Text(
                "${state.entries.size} entries",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        if (state.entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Text("No logs yet. Refresh products to generate HTTP events.")
            }
        } else {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101418)),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    items(state.entries, key = { it.id }) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(logTone(entry.level))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        entry.level,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(
                                    formatLogTimestamp(entry.timestampEpochMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                            Text(
                                "[${entry.category}] ${entry.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E8F0),
                                fontFamily = FontFamily.Monospace,
                            )
                            entry.details?.takeIf { it.isNotBlank() }?.let { details ->
                                Text(
                                    details,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                            HorizontalDivider(color = Color(0xFF1E293B))
                        }
                    }
                }
            }
        }
    }
}

private fun formatLogTimestamp(epochMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

@Composable
private fun logTone(level: String): Color = when (level) {
    "ERROR" -> Color(0xFFF97316)
    "INFO" -> Color(0xFF22C55E)
    "DEBUG" -> Color(0xFF38BDF8)
    else -> Color(0xFFE2E8F0)
}
