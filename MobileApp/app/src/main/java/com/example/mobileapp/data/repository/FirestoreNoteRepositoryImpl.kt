package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.NoteDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import com.example.mobileapp.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreNoteRepositoryImpl : NoteRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun notesCollection(userId: String) =
        db.collection("users").document(userId).collection("notes")

    override suspend fun getNotes(userId: String): Resource<List<Note>> = try {
        val snapshot = notesCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Resource.Success(snapshot.documents.mapNotNull { it.toObject(NoteDto::class.java)?.toDomain() })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load notes")
    }

    override suspend fun addNote(userId: String, title: String, content: String, type: String): Resource<Note> = try {
        val docRef = notesCollection(userId).document()
        val now = System.currentTimeMillis()
        val dto = NoteDto(id = docRef.id, userId = userId, title = title, content = content, type = type, createdAt = now, updatedAt = now)
        docRef.set(dto).await()
        Resource.Success(dto.toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save note")
    }

    override suspend fun deleteNote(userId: String, noteId: String): Resource<Unit> = try {
        notesCollection(userId).document(noteId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete note")
    }
}
