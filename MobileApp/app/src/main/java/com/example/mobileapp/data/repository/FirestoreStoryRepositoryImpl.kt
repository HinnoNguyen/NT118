package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.StoryDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import com.example.mobileapp.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreStoryRepositoryImpl : StoryRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun storiesCollection(userId: String) =
        db.collection("users").document(userId).collection("stories")

    override suspend fun getStories(userId: String): Resource<List<Story>> = try {
        val snapshot = storiesCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Resource.Success(snapshot.documents.mapNotNull { it.toObject(StoryDto::class.java)?.toDomain() })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load stories")
    }

    override suspend fun addStory(userId: String, title: String, content: String, genre: String): Resource<Story> = try {
        val docRef = storiesCollection(userId).document()
        val now = System.currentTimeMillis()
        val dto = StoryDto(id = docRef.id, userId = userId, title = title, content = content, genre = genre, createdAt = now, updatedAt = now)
        docRef.set(dto).await()
        Resource.Success(dto.toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save story")
    }

    override suspend fun deleteStory(userId: String, storyId: String): Resource<Unit> = try {
        storiesCollection(userId).document(storyId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete story")
    }
}
