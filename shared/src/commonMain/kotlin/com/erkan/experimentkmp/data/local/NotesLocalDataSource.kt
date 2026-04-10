package com.erkan.experimentkmp.data.local

import com.erkan.experimentkmp.data.local.model.StoredNoteDto
import com.erkan.experimentkmp.domain.model.Note
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

class NotesLocalDataSource(
    storageDirectoryPath: String,
    private val json: Json,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) {
    private val notesDirectory = storageDirectoryPath.toPath()
    private val notesFile = (storageDirectoryPath + "/notes.json").toPath()
    private val serializer = ListSerializer(StoredNoteDto.serializer())

    fun readNotes(): List<Note> {
        if (!fileSystem.exists(notesFile)) return emptyList()

        val content = fileSystem.read(notesFile) {
            readUtf8()
        }
        if (content.isBlank()) return emptyList()

        return json.decodeFromString(serializer, content).map { it.toDomain() }
    }

    fun writeNotes(notes: List<Note>) {
        fileSystem.createDirectories(notesDirectory)
        val payload = json.encodeToString(
            serializer,
            notes.map(StoredNoteDto::fromDomain),
        )
        fileSystem.write(notesFile) {
            writeUtf8(payload)
        }
    }
}
