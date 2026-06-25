package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.utils.Resource

interface StoryRepository {
    suspend fun getStories(userId: String): Resource<List<Story>>
    suspend fun addStory(userId: String, title: String, content: String, genre: String): Resource<Story>
    suspend fun deleteStory(userId: String, storyId: String): Resource<Unit>
}
