package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Story

interface StoryRepository {
    suspend fun createStory(
        title: String,
        genre: String,
        content: String,
        relatedNoteIds: List<String> = emptyList()
    ): Result<Story>

    suspend fun getStories(): Result<List<Story>>

    suspend fun getStory(storyId: String): Result<Story>

    suspend fun updateStory(story: Story): Result<Story>

    suspend fun deleteStory(storyId: String): Result<Unit>
}
