package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.NoteDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepositoryImpl : NoteRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val notesCollection = firestore.collection("notes")

    override suspend fun createNote(
        title: String,
        content: String,
        type: String,
        pinned: Boolean
    ): Result<Note> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (title.isBlank()) {
            return Result.failure(Exception("Title cannot be empty"))
        }

        return try {
            val now = System.currentTimeMillis()
            val note = Note(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title.trim(),
                content = content.trim(),
                type = type.ifBlank { "note" },
                pinned = pinned,
                createdAt = now,
                updatedAt = now
            )

            notesCollection.document(note.id).set(note.toDto()).await()
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to create note", e))
        }
    }

    override suspend fun getNotes(): Result<List<Note>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))

        return try {
            val snapshot = notesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { document ->
                document.toObject(NoteDto::class.java)?.toDomain()
            }.sortedByDescending { it.updatedAt }
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch notes", e))
        }
    }

    override suspend fun getNote(noteId: String): Result<Note> {
        if (noteId.isBlank()) {
            return Result.failure(Exception("Note id cannot be empty"))
        }

        return try {
            val snapshot = notesCollection.document(noteId).get().await()
            val note = snapshot.toObject(NoteDto::class.java)?.toDomain()
                ?: return Result.failure(Exception("Note not found"))
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch note", e))
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (note.id.isBlank()) {
            return Result.failure(Exception("Note id cannot be empty"))
        }
        if (note.userId != userId) {
            return Result.failure(Exception("Cannot update another user's note"))
        }

        return try {
            val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
            notesCollection.document(updatedNote.id).set(updatedNote.toDto()).await()
            Result.success(updatedNote)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update note", e))
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        if (noteId.isBlank()) {
            return Result.failure(Exception("Note id cannot be empty"))
        }

        return try {
            notesCollection.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to delete note", e))
        }
    }
}
