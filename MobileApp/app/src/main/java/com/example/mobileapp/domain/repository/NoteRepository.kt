package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(userId: String): Flow<List<Note>>
    suspend fun addNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>
}
