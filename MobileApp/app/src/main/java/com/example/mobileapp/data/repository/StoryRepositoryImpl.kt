package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.StoryDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StoryRepositoryImpl : StoryRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storiesCollection = firestore.collection("stories")

    override suspend fun saveStory(story: Story): Result<Unit> {
        return try {
            val finalId = if (story.id.isBlank()) storiesCollection.document().id else story.id
            val finalStory = if (story.id.isBlank()) story.copy(id = finalId) else story
            
            storiesCollection.document(finalId).set(finalStory.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to save story", e))
        }
    }

    override fun getStories(userId: String): Flow<List<Story>> {
        return storiesCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.toObject(StoryDto::class.java)?.toDomain()
                }.sortedByDescending { it.updatedAt }
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
        if (story.id.isBlank()) {
            return Result.failure(Exception("Story id cannot be empty"))
        }
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
