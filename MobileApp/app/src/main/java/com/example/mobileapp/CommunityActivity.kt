package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.PublicStoryRepositoryImpl
import com.example.mobileapp.domain.model.PublicStory
import com.example.mobileapp.domain.model.Comment
import com.example.mobileapp.adapter.CommentAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityActivity : AppCompatActivity() {
    private val publicStoryRepository = PublicStoryRepositoryImpl()

    private lateinit var btnBackToStories: TextView
    private lateinit var tvCommunityStatus: TextView
    private lateinit var publicStoriesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        setupUi()
        setupNavigation()
        loadPublicStories()
    }

    private fun setupUi() {
        btnBackToStories = findViewById(R.id.btnBackToStories)
        tvCommunityStatus = findViewById(R.id.tvCommunityStatus)
        publicStoriesContainer = findViewById(R.id.publicStoriesContainer)

        btnBackToStories.setOnClickListener {
            finish()
        }
    }

    private fun loadPublicStories() {
        tvCommunityStatus.visibility = View.VISIBLE
        tvCommunityStatus.text = "Loading public stories..."
        publicStoriesContainer.removeAllViews()

        lifecycleScope.launch {
            publicStoryRepository.getPublicStories(limit = 100)
                .onSuccess { renderPublicStories(it) }
                .onFailure {
                    tvCommunityStatus.text = it.message ?: "Failed to load community feed"
                    tvCommunityStatus.visibility = View.VISIBLE
                }
        }
    }

    private fun renderPublicStories(stories: List<PublicStory>) {
        publicStoriesContainer.removeAllViews()
        if (stories.isEmpty()) {
            tvCommunityStatus.text = "No public stories yet."
            tvCommunityStatus.visibility = View.VISIBLE
            return
        }

        tvCommunityStatus.visibility = View.GONE
        val inflater = LayoutInflater.from(this)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        stories.forEach { story ->
            val itemView = inflater.inflate(R.layout.item_public_story, publicStoriesContainer, false)
            itemView.findViewById<TextView>(R.id.tvPublicStoryAuthor).text = story.authorName
            itemView.findViewById<TextView>(R.id.tvPublicStoryTitle).text = story.title
            itemView.findViewById<TextView>(R.id.tvPublicStoryPreview).text =
                story.contentPreview.ifBlank { "(No preview available)" }
            itemView.findViewById<TextView>(R.id.tvPublicStoryMeta).text =
                "${formatter.format(Date(story.sharedAt))} • ${story.visibility.uppercase()}"
            
            val tvLike = itemView.findViewById<TextView>(R.id.tvLikeAction)
            val tvComment = itemView.findViewById<TextView>(R.id.tvCommentAction)
            
            tvLike.text = "❤ ${story.likeCount}"
            tvComment.text = "💬 ${story.commentCount}"

            tvLike.setOnClickListener {
                lifecycleScope.launch {
                    publicStoryRepository.likeStory(story.id)
                    loadPublicStories()
                }
            }
            
            tvComment.setOnClickListener {
                showCommentDialog(story.id)
            }

            itemView.setOnClickListener {
                showFullStoryDialog(story)
            }

            publicStoriesContainer.addView(itemView)
        }
    }

    private fun showFullStoryDialog(story: PublicStory) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_story_detail)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = story.title.uppercase()
        dialog.findViewById<TextView>(R.id.tvDialogContent).text = story.content
        
        // Hide "Share to Community" button in Community screen
        val btnShare = dialog.findViewById<MaterialButton>(R.id.btnShareToCommunity)
        btnShare.visibility = View.GONE
        
        val btnClose = dialog.findViewById<MaterialButton>(R.id.btnDialogClose)
        btnClose.text = "CLOSE"
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showCommentDialog(storyId: String) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_comment)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val rvComments = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvComments)
        val etComment = dialog.findViewById<android.widget.EditText>(R.id.etCommentContent)
        val btnSubmit = dialog.findViewById<MaterialButton>(R.id.btnSubmitComment)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancelComment)

        val dialogAdapter = CommentAdapter()
        rvComments.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@CommunityActivity)
            adapter = dialogAdapter
        }

        // Load comments
        lifecycleScope.launch {
            publicStoryRepository.getComments(storyId)
                .onSuccess { comments ->
                    dialogAdapter.submitList(comments)
                }
                .onFailure {
                    Toast.makeText(this@CommunityActivity, "Error loading comments: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnSubmit.setOnClickListener {
            val commentText = etComment.text.toString()
            if (commentText.isNotBlank()) {
                btnSubmit.isEnabled = false
                lifecycleScope.launch {
                    val result = publicStoryRepository.addComment(storyId, commentText)
                    if (result.isSuccess) {
                        etComment.text.clear()
                        // Reload comments after posting
                        publicStoryRepository.getComments(storyId)
                            .onSuccess { comments ->
                                dialogAdapter.submitList(comments) {
                                    if (comments.isNotEmpty()) {
                                        rvComments.smoothScrollToPosition(comments.size - 1)
                                    }
                                }
                            }
                        loadPublicStories() // Update count on main screen
                        Toast.makeText(this@CommunityActivity, "Comment added!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                        Toast.makeText(this@CommunityActivity, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                    btnSubmit.isEnabled = true
                }
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener {
            startActivity(Intent(this, QuestActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener {
            startActivity(Intent(this, StoryActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
