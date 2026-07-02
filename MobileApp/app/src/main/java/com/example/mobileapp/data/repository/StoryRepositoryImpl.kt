package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.StoryDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StoryRepositoryImpl : StoryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storiesCollection = firestore.collection("stories")

    override fun getStories(userId: String): Flow<List<Story>> = callbackFlow {
        val subscription = storiesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val stories = snapshot.toObjects(StoryDto::class.java)
                        .map { it.toDomain() }
                        .sortedByDescending { it.createdAt }
                    trySend(stories)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveStory(story: Story): Result<Unit> {
        return try {
            storiesCollection.document(story.id).set(story.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStory(storyId: String): Result<Unit> {
        return try {
            storiesCollection.document(storyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
