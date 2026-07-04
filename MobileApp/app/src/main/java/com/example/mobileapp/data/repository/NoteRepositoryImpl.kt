package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.NoteDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NoteRepositoryImpl : NoteRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val notesCollection = firestore.collection("notes")

    override suspend fun addNote(note: Note): Result<Unit> {
        return try {
            val finalId = if (note.id.isBlank()) notesCollection.document().id else note.id
            val finalNote = if (note.id.isBlank()) note.copy(id = finalId) else note
            
            notesCollection.document(finalId).set(finalNote.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to add note", e))
        }
    }

    override fun getNotes(userId: String): Flow<List<Note>> {
        return notesCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.toObject(NoteDto::class.java)?.toDomain()
                }.sortedByDescending { it.updatedAt }
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
