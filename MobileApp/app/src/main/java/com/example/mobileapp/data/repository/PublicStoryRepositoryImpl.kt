package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.PublicStoryDto
import com.example.mobileapp.data.dto.CommentDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.PublicStory
import com.example.mobileapp.domain.model.Comment
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.PublicStoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PublicStoryRepositoryImpl : PublicStoryRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val publicStoriesCollection = firestore.collection("public_stories")

    override suspend fun publishStory(
        story: Story,
        authorName: String,
        authorAvatarUrl: String,
        coverImageUrl: String
    ): Result<PublicStory> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (story.userId != currentUserId) {
            return Result.failure(Exception("Cannot publish another user's story"))
        }
        if (authorName.isBlank()) {
            return Result.failure(Exception("Author name cannot be empty"))
        }

        return try {
            val sharedAt = System.currentTimeMillis()
            val publicStory = PublicStory(
                id = story.id,
                storyId = story.id,
                authorId = story.userId,
                authorName = authorName.trim(),
                authorAvatarUrl = authorAvatarUrl.trim(),
                title = story.title,
                content = story.content,
                contentPreview = story.content.take(180),
                coverImageUrl = coverImageUrl.trim(),
                likeCount = 0,
                commentCount = 0,
                createdAt = story.createdAt,
                sharedAt = sharedAt,
                visibility = "public"
            )

            publicStoriesCollection.document(publicStory.id).set(publicStory.toDto()).await()
            Result.success(publicStory)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to publish story", e))
        }
    }

    override suspend fun getPublicStories(limit: Long): Result<List<PublicStory>> {
        return try {
            val snapshot = publicStoriesCollection
                .orderBy("sharedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            Result.success(snapshot.documents.mapNotNull { it.toObject(PublicStoryDto::class.java)?.toDomain() })
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch public stories", e))
        }
    }

    override suspend fun unpublishStory(storyId: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (storyId.isBlank()) {
            return Result.failure(Exception("Story id cannot be empty"))
        }

        return try {
            val snapshot = publicStoriesCollection.document(storyId).get().await()
            val publicStory = snapshot.toObject(PublicStoryDto::class.java)
                ?: return Result.success(Unit)
            if (publicStory.authorId != currentUserId) {
                return Result.failure(Exception("Cannot unpublish another user's story"))
            }

            publicStoriesCollection.document(storyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to unpublish story", e))
        }
    }

    override suspend fun likeStory(storyId: String): Result<Unit> {
        return try {
            publicStoriesCollection.document(storyId)
                .update("likeCount", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikeStory(storyId: String): Result<Unit> {
        return try {
            publicStoriesCollection.document(storyId)
                .update("likeCount", FieldValue.increment(-1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getComments(storyId: String): Result<List<Comment>> {
        return try {
            val snapshot = publicStoriesCollection.document(storyId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(CommentDto::class.java)?.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addComment(storyId: String, commentText: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        val userName = auth.currentUser?.displayName ?: "Explorer"
        
        return try {
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                storyId = storyId,
                userId = currentUserId,
                userName = userName,
                content = commentText,
                createdAt = System.currentTimeMillis()
            )

            firestore.runTransaction { transaction ->
                val storyRef = publicStoriesCollection.document(storyId)
                val commentRef = storyRef.collection("comments").document(comment.id)
                
                transaction.set(commentRef, comment.toDto())
                transaction.update(storyRef, "commentCount", FieldValue.increment(1))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
