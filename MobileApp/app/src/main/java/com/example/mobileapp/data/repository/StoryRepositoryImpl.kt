package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.StoryDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StoryRepositoryImpl : StoryRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storiesCollection = firestore.collection("stories")

    override suspend fun createStory(
        title: String,
        content: String,
        relatedNoteIds: List<String>
    ): Result<Story> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (title.isBlank()) {
            return Result.failure(Exception("Story title cannot be empty"))
        }
        if (content.isBlank()) {
            return Result.failure(Exception("Story content cannot be empty"))
        }

        return try {
            val now = System.currentTimeMillis()
            val story = Story(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title.trim(),
                content = content.trim(),
                relatedNoteIds = relatedNoteIds,
                createdAt = now,
                updatedAt = now
            )

            storiesCollection.document(story.id).set(story.toDto()).await()
            Result.success(story)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to create story", e))
        }
    }

    override suspend fun getStories(): Result<List<Story>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))

        return try {
            val snapshot = storiesCollection
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(StoryDto::class.java)?.toDomain() })
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch stories", e))
        }
    }

    override suspend fun getStory(storyId: String): Result<Story> {
        if (storyId.isBlank()) {
            return Result.failure(Exception("Story id cannot be empty"))
        }

        return try {
            val snapshot = storiesCollection.document(storyId).get().await()
            val story = snapshot.toObject(StoryDto::class.java)?.toDomain()
                ?: return Result.failure(Exception("Story not found"))
            Result.success(story)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch story", e))
        }
    }

    override suspend fun updateStory(story: Story): Result<Story> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (story.userId != userId) {
            return Result.failure(Exception("Cannot update another user's story"))
        }

        return try {
            val updatedStory = story.copy(updatedAt = System.currentTimeMillis())
            storiesCollection.document(updatedStory.id).set(updatedStory.toDto()).await()
            Result.success(updatedStory)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update story", e))
        }
    }

    override suspend fun deleteStory(storyId: String): Result<Unit> {
        if (storyId.isBlank()) {
            return Result.failure(Exception("Story id cannot be empty"))
        }

        return try {
            storiesCollection.document(storyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to delete story", e))
        }
    }
}
