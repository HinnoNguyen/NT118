package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.NoteDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoteRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NoteRepository {

    private val notesCollection = firestore.collection("notes")

    override fun getNotes(userId: String): Flow<List<Note>> = callbackFlow {
        val registration = notesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(NoteDto::class.java)
                        .map { it.toDomain() }
                        .sortedByDescending { it.updatedAt }
                    trySend(notes)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addNote(note: Note): Result<Unit> {
        return try {
            val docRef = notesCollection.document()
            val noteWithId = note.copy(id = docRef.id)
            docRef.set(noteWithId.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            notesCollection.document(note.id).set(note.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            notesCollection.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
