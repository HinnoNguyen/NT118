package com.example.mobileapp

import com.example.mobileapp.data.dto.NoteDto
import com.example.mobileapp.data.dto.StoryDto
import com.example.mobileapp.data.dto.TaskDto
import com.example.mobileapp.data.dto.TimerSessionDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreSchemaMapperTest {

    @Test
    fun noteDto_toDomain_mapsAllFields() {
        val dto = NoteDto(
            id = "note-1",
            userId = "user-1",
            title = "Test note",
            content = "Note content",
            type = "note",
            pinned = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val note = dto.toDomain()

        assertEquals("note-1", note.id)
        assertEquals("user-1", note.userId)
        assertEquals("Test note", note.title)
        assertEquals("Note content", note.content)
        assertEquals("note", note.type)
        assertEquals(true, note.pinned)
        assertEquals(1000L, note.createdAt)
        assertEquals(2000L, note.updatedAt)
    }

    @Test
    fun note_toDto_mapsAllFields() {
        val dto = NoteDto(
            id = "note-1",
            userId = "user-1",
            title = "Test note",
            content = "Note content",
            type = "note",
            pinned = false,
            createdAt = 1000L,
            updatedAt = 2000L
        ).toDomain().toDto()

        assertEquals("note-1", dto.id)
        assertEquals("user-1", dto.userId)
        assertEquals("Test note", dto.title)
        assertEquals("Note content", dto.content)
        assertEquals("note", dto.type)
        assertEquals(false, dto.pinned)
        assertEquals(1000L, dto.createdAt)
        assertEquals(2000L, dto.updatedAt)
    }

    @Test
    fun taskDto_toDomain_mapsAllFields() {
        val dto = TaskDto(
            id = "task-1",
            userId = "user-1",
            title = "Submit assignment",
            description = "Finish Day 3 schema",
            dueAt = 3000L,
            completed = false,
            priority = "high",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val task = dto.toDomain()

        assertEquals("task-1", task.id)
        assertEquals("user-1", task.userId)
        assertEquals("Submit assignment", task.title)
        assertEquals("Finish Day 3 schema", task.description)
        assertEquals(3000L, task.dueAt)
        assertEquals(false, task.completed)
        assertEquals("high", task.priority)
        assertEquals(1000L, task.createdAt)
        assertEquals(2000L, task.updatedAt)
    }

    @Test
    fun storyDto_toDomain_mapsAllFields() {
        val dto = StoryDto(
            id = "story-1",
            userId = "user-1",
            title = "Reflection",
            content = "Today I completed the schema.",
            relatedNoteIds = listOf("note-1", "note-2"),
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val story = dto.toDomain()

        assertEquals("story-1", story.id)
        assertEquals("user-1", story.userId)
        assertEquals("Reflection", story.title)
        assertEquals("Today I completed the schema.", story.content)
        assertEquals(listOf("note-1", "note-2"), story.relatedNoteIds)
        assertEquals(1000L, story.createdAt)
        assertEquals(2000L, story.updatedAt)
    }

    @Test
    fun timerSessionDto_toDomain_mapsAllFields() {
        val dto = TimerSessionDto(
            id = "session-1",
            userId = "user-1",
            startedAt = 1000L,
            endedAt = 2500L,
            durationMinutes = 25,
            completed = true,
            createdAt = 900L
        )

        val timerSession = dto.toDomain()

        assertEquals("session-1", timerSession.id)
        assertEquals("user-1", timerSession.userId)
        assertEquals(1000L, timerSession.startedAt)
        assertEquals(2500L, timerSession.endedAt)
        assertEquals(25, timerSession.durationMinutes)
        assertEquals(true, timerSession.completed)
        assertEquals(900L, timerSession.createdAt)
    }
}
