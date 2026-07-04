package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun addNote(note: Note): Result<Unit>

    fun getNotes(userId: String): Flow<List<Note>>

    suspend fun getNote(noteId: String): Result<Note>

    suspend fun updateNote(note: Note): Result<Note>

    suspend fun deleteNote(noteId: String): Result<Unit>
}
