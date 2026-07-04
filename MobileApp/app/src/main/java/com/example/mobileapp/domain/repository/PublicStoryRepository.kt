package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.PublicStory
import com.example.mobileapp.domain.model.Story

interface PublicStoryRepository {
    suspend fun publishStory(
        story: Story,
        authorName: String,
        authorAvatarUrl: String = "",
        coverImageUrl: String = ""
    ): Result<PublicStory>

    suspend fun getPublicStories(limit: Long = 20): Result<List<PublicStory>>

    suspend fun unpublishStory(storyId: String): Result<Unit>

    suspend fun likeStory(storyId: String): Result<Unit>

    suspend fun unlikeStory(storyId: String): Result<Unit>

    suspend fun getComments(storyId: String): Result<List<com.example.mobileapp.domain.model.Comment>>

    suspend fun addComment(storyId: String, commentText: String): Result<Unit>
}
