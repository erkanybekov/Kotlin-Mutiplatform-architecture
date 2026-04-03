package com.erkan.experimentkmp

import com.erkan.experimentkmp.domain.model.Note
import com.erkan.experimentkmp.domain.model.Post
import com.erkan.experimentkmp.domain.repository.NotesRepository
import com.erkan.experimentkmp.domain.repository.PostsRepository
import com.erkan.experimentkmp.domain.usecase.AddNoteUseCase
import com.erkan.experimentkmp.domain.usecase.GetNotesUseCase
import com.erkan.experimentkmp.domain.usecase.GetPostsUseCase
import com.erkan.experimentkmp.domain.usecase.ToggleNoteCompletionUseCase
import com.erkan.experimentkmp.logging.InMemoryAppLogger
import com.erkan.experimentkmp.network.configureSharedHttpClient
import com.erkan.experimentkmp.presentation.logs.LogsStateHolder
import com.erkan.experimentkmp.presentation.notes.NotesStateHolder
import com.erkan.experimentkmp.presentation.posts.PostsStateHolder
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeatureStateHolderTest {
    @Test
    fun postsStateHolderLoadsRemotePosts() = runTest {
        val stateHolder = PostsStateHolder(
            getPostsUseCase = GetPostsUseCase(FakePostsRepository()),
            scope = backgroundScope,
        )

        stateHolder.load()
        delay(10)

        assertEquals(2, stateHolder.currentState.posts.size)
        assertEquals("Remote post", stateHolder.currentState.posts.first().title)
        assertEquals("$89.99", stateHolder.currentState.posts.first().priceLabel)
        assertTrue(!stateHolder.currentState.isLoading)
    }

    @Test
    fun notesStateHolderAddsAndTogglesNotes() = runTest {
        val repository = FakeNotesRepository()
        val stateHolder = NotesStateHolder(
            getNotesUseCase = GetNotesUseCase(repository),
            addNoteUseCase = AddNoteUseCase(repository),
            toggleNoteCompletionUseCase = ToggleNoteCompletionUseCase(repository),
            scope = backgroundScope,
        )

        stateHolder.load()
        delay(10)
        stateHolder.addNote("Plan", "Ship KMP demo")
        delay(10)

        val note = stateHolder.currentState.notes.first()
        assertEquals("Plan", note.title)
        assertTrue(!note.isDone)

        stateHolder.toggleNote(note.id)
        delay(10)

        assertTrue(stateHolder.currentState.notes.first().isDone)
    }

    @Test
    fun logsStateHolderReflectsAppLoggerAndClear() = runTest {
        val appLogger = InMemoryAppLogger(maxEntries = 3)
        val stateHolder = LogsStateHolder(appLogger, backgroundScope)

        appLogger.append(level = "INFO", category = "network", message = "first")
        delay(10)
        assertEquals(1, stateHolder.currentState.entries.size)

        appLogger.append(level = "DEBUG", category = "network", message = "second")
        appLogger.append(level = "ERROR", category = "network", message = "third")
        appLogger.append(level = "INFO", category = "network", message = "fourth")
        delay(10)

        assertEquals(3, stateHolder.currentState.entries.size)
        assertEquals("fourth", stateHolder.currentState.entries.first().message)
        assertEquals("second", stateHolder.currentState.entries.last().message)

        stateHolder.clearLogs()
        delay(10)
        assertTrue(stateHolder.currentState.entries.isEmpty())
    }

    @Test
    fun networkInstrumentationLogsStartAndSuccess() = runTest {
        val appLogger = InMemoryAppLogger()
        val client = HttpClient(
            MockEngine { request ->
                respond(
                    content = """[{"id":1,"title":"Hello","body":"World"}]""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            },
        ) {
            configureSharedHttpClient(engineName = "mock", appLogger = appLogger)
        }

        client.get("https://example.com/posts")

        val entries = appLogger.entries.value
        assertTrue(entries.any { it.message.contains("REQUEST: https://example.com/posts") })
        assertTrue(entries.any { it.message.contains("RESPONSE: 200 OK") })
    }

    @Test
    fun networkInstrumentationLogsFailure() = runTest {
        val appLogger = InMemoryAppLogger()
        val client = HttpClient(
            MockEngine {
                error("network down")
            },
        ) {
            configureSharedHttpClient(engineName = "mock", appLogger = appLogger)
        }

        runCatching {
            client.get("https://example.com/posts")
        }

        val entries = appLogger.entries.value
        assertTrue(entries.any { it.level == "ERROR" && it.message.contains("HTTP failure: GET https://example.com/posts") })
        assertTrue(entries.any { it.message.contains("REQUEST: https://example.com/posts") })
        assertTrue(entries.any { it.details != null })
    }
}

private class FakePostsRepository : PostsRepository {
    override suspend fun getPosts(): List<Post> = listOf(
        Post(id = 1, title = "Remote post", body = "Fetched from API", price = 89.99, category = "bags", imageUrl = "https://example.com/1.png"),
        Post(id = 2, title = "Cached idea", body = "Ready for UI", price = 24.5, category = "tech", imageUrl = "https://example.com/2.png"),
    )
}

private class FakeNotesRepository : NotesRepository {
    private val notes = mutableListOf<Note>()

    override suspend fun getNotes(): List<Note> = notes.toList()

    override suspend fun addNote(title: String, body: String): List<Note> {
        notes.add(
            0,
            Note(
                id = (notes.size + 1).toLong(),
                title = title,
                body = body,
                isDone = false,
            ),
        )
        return notes.toList()
    }

    override suspend fun toggleNote(id: Long): List<Note> {
        val index = notes.indexOfFirst { it.id == id }
        if (index >= 0) {
            val note = notes[index]
            notes[index] = note.copy(isDone = !note.isDone)
        }
        return notes.toList()
    }
}
