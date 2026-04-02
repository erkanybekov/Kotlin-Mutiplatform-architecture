package com.erkan.experimentkmp

import com.erkan.experimentkmp.domain.model.Note
import com.erkan.experimentkmp.domain.model.Post
import com.erkan.experimentkmp.domain.repository.NotesRepository
import com.erkan.experimentkmp.domain.repository.PostsRepository
import com.erkan.experimentkmp.domain.usecase.AddNoteUseCase
import com.erkan.experimentkmp.domain.usecase.GetNotesUseCase
import com.erkan.experimentkmp.domain.usecase.GetPostsUseCase
import com.erkan.experimentkmp.domain.usecase.ToggleNoteCompletionUseCase
import com.erkan.experimentkmp.presentation.notes.NotesStateHolder
import com.erkan.experimentkmp.presentation.posts.PostsStateHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}

private class FakePostsRepository : PostsRepository {
    override suspend fun getPosts(): List<Post> = listOf(
        Post(id = 1, title = "Remote post", body = "Fetched from API"),
        Post(id = 2, title = "Cached idea", body = "Ready for UI"),
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
