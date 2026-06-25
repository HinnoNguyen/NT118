package com.example.mobileapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.presentation.story.StoryViewModel
import com.example.mobileapp.utils.NavHelper
import com.example.mobileapp.utils.StoryAIHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {

    private val viewModel: StoryViewModel by viewModels { StoryViewModel.factory() }
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        setupUI()
        NavHelper.setup(this, NavHelper.Screen.STORY)
        observeViewModel()
        auth.currentUser?.uid?.let { viewModel.loadStories(it) }
    }

    private fun setupUI() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val btnStoryFy = findViewById<MaterialButton>(R.id.btnStoryFy)
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)

        btnCreateStory.setOnClickListener { viewModel.toggleForgeSection() }

        genreEpic.setOnClickListener    { viewModel.selectGenre(StoryViewModel.Genre.EPIC) }
        genreMystery.setOnClickListener { viewModel.selectGenre(StoryViewModel.Genre.MYSTERY) }
        genreComedy.setOnClickListener  { viewModel.selectGenre(StoryViewModel.Genre.COMEDY) }
        genreHorror.setOnClickListener  { viewModel.selectGenre(StoryViewModel.Genre.HORROR) }

        btnStoryFy?.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val title = findViewById<EditText>(R.id.etStoryTitle)?.text?.toString() ?: ""
            val content = findViewById<EditText>(R.id.etStoryContent)?.text?.toString() ?: ""
            viewModel.saveStory(uid, title, content)
            findViewById<EditText>(R.id.etStoryTitle)?.text?.clear()
            findViewById<EditText>(R.id.etStoryContent)?.text?.clear()
        }

        val etContent = findViewById<EditText>(R.id.etStoryContent)
        val btnAiSuggest = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAiSuggest)
        val btnAiPrompts = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAiPrompts)
        val btnAiNotes   = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAiNotes)

        btnAiSuggest?.setOnClickListener {
            val text = etContent?.text?.toString() ?: ""
            if (text.isBlank()) { Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            btnAiSuggest.isEnabled = false
            lifecycleScope.launch {
                val suggestion = StoryAIHelper.suggestContinuation(text)
                btnAiSuggest.isEnabled = true
                AlertDialog.Builder(this@StoryActivity)
                    .setTitle("AI Suggestion")
                    .setMessage(suggestion)
                    .setPositiveButton("Add to Story") { _, _ -> etContent?.append("\n\n$suggestion") }
                    .setNegativeButton("Dismiss", null)
                    .show()
            }
        }

        btnAiPrompts?.setOnClickListener {
            val text = etContent?.text?.toString() ?: ""
            btnAiPrompts.isEnabled = false
            lifecycleScope.launch {
                val prompts = StoryAIHelper.getWritingPrompts(text)
                btnAiPrompts.isEnabled = true
                AlertDialog.Builder(this@StoryActivity)
                    .setTitle("Reflection Prompts")
                    .setItems(prompts.toTypedArray()) { _, _ -> }
                    .show()
            }
        }

        btnAiNotes?.setOnClickListener {
            val text = etContent?.text?.toString() ?: ""
            val currentNotes = viewModel.getLoadedNotes()
            val related = StoryAIHelper.findRelatedNotes(text, currentNotes)
            if (related.isEmpty()) {
                Toast.makeText(this, "No related notes found", Toast.LENGTH_SHORT).show()
            } else {
                val titles = related.map { "• ${it.title}" }.toTypedArray()
                AlertDialog.Builder(this@StoryActivity)
                    .setTitle("Related Notes")
                    .setItems(titles) { _, _ -> }
                    .show()
            }
        }
    }

    private fun observeViewModel() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val newStorySection = findViewById<LinearLayout>(R.id.newStorySection)
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)
        val genres = listOf(genreEpic, genreMystery, genreComedy, genreHorror)
        val genreEnum = listOf(StoryViewModel.Genre.EPIC, StoryViewModel.Genre.MYSTERY,
            StoryViewModel.Genre.COMEDY, StoryViewModel.Genre.HORROR)
        val storyListContainer = findViewById<LinearLayout>(R.id.storyListContainer)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isForgeVisible.collect { visible ->
                        newStorySection.visibility = if (visible) View.VISIBLE else View.GONE
                        btnCreateStory.text = if (visible) "✕" else "+ CREATE"
                    }
                }
                launch {
                    viewModel.selectedGenre.collect { selected ->
                        genres.forEachIndexed { i, layout ->
                            val text = layout.getChildAt(1) as? TextView ?: return@forEachIndexed
                            if (genreEnum[i] == selected) {
                                layout.setBackgroundColor(0xFFFFD700.toInt())
                                text.setTextColor(ContextCompat.getColor(this@StoryActivity, R.color.black))
                            } else {
                                layout.setBackgroundResource(R.drawable.bg_button_unselected)
                                text.setTextColor(0xFFAAAAAA.toInt())
                            }
                        }
                    }
                }
                launch {
                    viewModel.stories.collect { stories ->
                        renderStories(storyListContainer, stories)
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        msg?.let { Toast.makeText(this@StoryActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }

    private fun renderStories(container: LinearLayout?, stories: List<Story>) {
        container?.removeAllViews() ?: return
        val uid = auth.currentUser?.uid ?: return
        val dp = resources.displayMetrics.density

        stories.forEach { story ->
            val genreEmoji = when (story.genre) {
                "epic" -> "⚔️"; "mystery" -> "🔮"; "comedy" -> "🤡"; "horror" -> "👻"; else -> "📖"
            }
            val genreColor = when (story.genre) {
                "epic" -> 0xFFFFD700.toInt(); "mystery" -> 0xFF9B59B6.toInt()
                "comedy" -> 0xFFFF4444.toInt(); "horror" -> 0xFF888888.toInt()
                else -> 0xFF57E389.toInt()
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (16 * dp).toInt() }
                setBackgroundResource(R.drawable.bg_main_card)
                setPadding((16 * dp).toInt(), (16 * dp).toInt(), (16 * dp).toInt(), (16 * dp).toInt())
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val icon = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams((48 * dp).toInt(), (48 * dp).toInt())
                setBackgroundColor(0xFF1A1A24.toInt())
                gravity = android.view.Gravity.CENTER
                text = genreEmoji; textSize = 24f
            }

            val textBlock = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (16 * dp).toInt()
                }
            }

            val genreLabel = TextView(this).apply {
                text = story.genre.uppercase()
                setTextColor(genreColor); textSize = 10f
                typeface = android.graphics.Typeface.MONOSPACE
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val titleView = TextView(this).apply {
                text = story.title
                setTextColor(0xFF57E389.toInt()); textSize = 14f
                typeface = android.graphics.Typeface.MONOSPACE
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (4 * dp).toInt() }
            }
            val contentView = TextView(this).apply {
                text = story.content
                setTextColor(0xFFAAAAAA.toInt()); textSize = 11f
                typeface = android.graphics.Typeface.MONOSPACE
                maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (4 * dp).toInt() }
            }

            val deleteBtn = TextView(this).apply {
                text = "✕"; setTextColor(0xFFFF4444.toInt()); textSize = 12f
                setPadding((8 * dp).toInt(), 0, 0, 0)
                setOnClickListener { viewModel.deleteStory(uid, story.id) }
            }

            textBlock.addView(genreLabel)
            textBlock.addView(titleView)
            textBlock.addView(contentView)
            row.addView(icon); row.addView(textBlock); row.addView(deleteBtn)
            container.addView(row)
        }
    }
}
