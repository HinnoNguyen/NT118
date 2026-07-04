package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun saveStory(story: Story): Result<Unit>

    fun getStories(userId: String): Flow<List<Story>>

    suspend fun getStory(storyId: String): Result<Story>

    suspend fun updateStory(story: Story): Result<Story>

    suspend fun deleteStory(storyId: String): Result<Unit>
}
