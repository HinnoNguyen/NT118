package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getStories(userId: String): Flow<List<Story>>
    suspend fun saveStory(story: Story): Result<Unit>
    suspend fun deleteStory(storyId: String): Result<Unit>
}
