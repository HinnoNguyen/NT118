package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Note

interface NoteRepository {
    suspend fun createNote(
        title: String,
        content: String,
        type: String = "note",
        pinned: Boolean = false,
        reminderTime: Long? = null
    ): Result<Note>

    suspend fun getNotes(): Result<List<Note>>

    suspend fun getNote(noteId: String): Result<Note>

    suspend fun updateNote(note: Note): Result<Note>

    suspend fun deleteNote(noteId: String): Result<Unit>
}
