package com.example.mobileapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.presentation.notes.NotesViewModel
import com.example.mobileapp.utils.NavHelper
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {

    private val viewModel: NotesViewModel by viewModels { NotesViewModel.factory() }
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        setupUI()
        NavHelper.setup(this, NavHelper.Screen.NOTES)
        observeViewModel()
        auth.currentUser?.uid?.let { viewModel.loadNotes(it) }
    }

    private fun setupUI() {
        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val btnSaveNote = findViewById<MaterialButton>(R.id.btnSaveNote)
        val etTitle = findViewById<EditText>(R.id.etNoteTitle)
        val etContent = findViewById<EditText>(R.id.etNoteContent)

        btnNewNote.setOnClickListener { viewModel.toggleNewNoteSection() }

        btnSaveNote.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            viewModel.saveNote(uid, etTitle.text.toString(), etContent.text.toString())
            etTitle.text?.clear()
            etContent.text?.clear()
        }
    }

    private fun observeViewModel() {
        val btnNewNote = findViewById<MaterialButton>(R.id.btnNewNote)
        val newNoteSection = findViewById<LinearLayout>(R.id.newNoteSection)
        val notesListContainer = findViewById<LinearLayout>(R.id.notesListContainer)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isNewNoteVisible.collect { visible ->
                        newNoteSection.visibility = if (visible) View.VISIBLE else View.GONE
                        btnNewNote.text = if (visible) "✕" else "+ NEW"
                    }
                }
                launch {
                    viewModel.notes.collect { notes ->
                        renderNotes(notesListContainer, notes)
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        msg?.let { Toast.makeText(this@NotesActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }

    private fun renderNotes(container: LinearLayout, notes: List<Note>) {
        container.removeAllViews()
        val uid = auth.currentUser?.uid ?: return
        notes.forEach { note ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * resources.displayMetrics.density).toInt() }
                setBackgroundResource(R.drawable.bg_main_card)
                setPadding(
                    (12 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt()
                )
            }

            val icon = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (40 * resources.displayMetrics.density).toInt(),
                    (40 * resources.displayMetrics.density).toInt()
                )
                setBackgroundColor(0xFF1A1A24.toInt())
                gravity = android.view.Gravity.CENTER
                text = when (note.type) { "reminder" -> "🔔"; "flashcard" -> "🃏"; else -> "📝" }
                textSize = 18f
            }

            val textBlock = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (12 * resources.displayMetrics.density).toInt()
                }
            }

            val titleView = TextView(this).apply {
                text = note.title.ifBlank { "(no title)" }
                setTextColor(0xFF57E389.toInt())
                textSize = 14f
                typeface = android.graphics.Typeface.MONOSPACE
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val contentView = TextView(this).apply {
                text = note.content
                setTextColor(0xFFAAAAAA.toInt())
                textSize = 11f
                typeface = android.graphics.Typeface.MONOSPACE
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (2 * resources.displayMetrics.density).toInt() }
            }

            val deleteBtn = TextView(this).apply {
                text = "✕"
                setTextColor(0xFFFF4444.toInt())
                textSize = 14f
                setPadding((8 * resources.displayMetrics.density).toInt(), 0, 0, 0)
                setOnClickListener { viewModel.deleteNote(uid, note.id) }
            }

            textBlock.addView(titleView)
            textBlock.addView(contentView)
            row.addView(icon)
            row.addView(textBlock)
            row.addView(deleteBtn)
            container.addView(row)
        }
    }
}
