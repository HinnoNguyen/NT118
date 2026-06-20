package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.NoteRepositoryImpl
import com.example.mobileapp.domain.model.Note
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {
    private val noteRepository = NoteRepositoryImpl()
    private var selectedType = "note"
    private var editingNote: Note? = null

    private lateinit var btnNewNote: MaterialButton
    private lateinit var btnSaveNote: MaterialButton
    private lateinit var newNoteSection: LinearLayout
    private lateinit var btnTypeNote: TextView
    private lateinit var btnTypeReminder: TextView
    private lateinit var btnTypeFlashcard: TextView
    private lateinit var reminderTimeLayout: LinearLayout
    private lateinit var etNoteTitle: EditText
    private lateinit var etNoteContent: EditText
    private lateinit var tvNotesStatus: TextView
    private lateinit var notesListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        setupUI()
        setupNavigation()
    }

    private fun setupUI() {
        btnNewNote = findViewById(R.id.btnNewNote)
        btnSaveNote = findViewById(R.id.btnSaveNote)
        newNoteSection = findViewById(R.id.newNoteSection)
        btnTypeNote = findViewById(R.id.btnTypeNote)
        btnTypeReminder = findViewById(R.id.btnTypeReminder)
        btnTypeFlashcard = findViewById(R.id.btnTypeFlashcard)
        reminderTimeLayout = findViewById(R.id.reminderTimeLayout)
        etNoteTitle = findViewById(R.id.etNoteTitle)
        etNoteContent = findViewById(R.id.etNoteContent)
        tvNotesStatus = findViewById(R.id.tvNotesStatus)
        notesListContainer = findViewById(R.id.notesListContainer)

        btnNewNote.setOnClickListener {
            toggleEditor(newNoteSection.visibility == View.GONE)
        }

        val typeButtons = listOf(btnTypeNote, btnTypeReminder, btnTypeFlashcard)

        fun selectType(selectedView: TextView) {
            typeButtons.forEach { btn ->
                if (btn == selectedView) {
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.note_purple))
                    btn.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    btn.setBackgroundResource(R.drawable.bg_button_unselected)
                    btn.setTextColor(0xFFAAAAAA.toInt())
                }
            }

            selectedType = when (selectedView.id) {
                R.id.btnTypeReminder -> "reminder"
                R.id.btnTypeFlashcard -> "flashcard"
                else -> "note"
            }
            reminderTimeLayout.visibility = if (selectedType == "reminder") View.VISIBLE else View.GONE
        }

        btnTypeNote.setOnClickListener { selectType(btnTypeNote) }
        btnTypeReminder.setOnClickListener { selectType(btnTypeReminder) }
        btnTypeFlashcard.setOnClickListener { selectType(btnTypeFlashcard) }
        btnSaveNote.setOnClickListener { saveNote() }
        selectType(btnTypeNote)
        loadNotes()
    }

    private fun toggleEditor(show: Boolean) {
        newNoteSection.visibility = if (show) View.VISIBLE else View.GONE
        btnNewNote.text = if (show) "✕" else "+ NEW"
        if (!show) {
            clearEditor()
        }
    }

    private fun clearEditor() {
        editingNote = null
        etNoteTitle.text?.clear()
        etNoteContent.text?.clear()
        btnSaveNote.text = "✦ SAVE SCROLL"
        selectedType = "note"
        btnTypeNote.performClick()
    }

    private fun saveNote() {
        val title = etNoteTitle.text.toString()
        val content = etNoteContent.text.toString()

        lifecycleScope.launch {
            val result = if (editingNote == null) {
                noteRepository.createNote(
                    title = title,
                    content = content,
                    type = selectedType
                )
            } else {
                noteRepository.updateNote(
                    editingNote!!.copy(
                        title = title.trim(),
                        content = content.trim(),
                        type = selectedType
                    )
                )
            }

            result.onSuccess {
                Toast.makeText(
                    this@NotesActivity,
                    if (editingNote == null) "Scroll saved" else "Scroll updated",
                    Toast.LENGTH_SHORT
                ).show()
                toggleEditor(false)
                loadNotes()
            }.onFailure {
                Toast.makeText(
                    this@NotesActivity,
                    it.message ?: "Failed to save note",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadNotes() {
        tvNotesStatus.visibility = View.VISIBLE
        tvNotesStatus.text = "Loading scrolls..."
        notesListContainer.removeAllViews()

        lifecycleScope.launch {
            noteRepository.getNotes()
                .onSuccess { notes ->
                    renderNotes(notes)
                }
                .onFailure {
                    tvNotesStatus.visibility = View.VISIBLE
                    tvNotesStatus.text = it.message ?: "Failed to load notes"
                }
        }
    }

    private fun renderNotes(notes: List<Note>) {
        notesListContainer.removeAllViews()
        if (notes.isEmpty()) {
            tvNotesStatus.visibility = View.VISIBLE
            tvNotesStatus.text = "No notes yet. Create your first scroll."
            return
        }

        tvNotesStatus.visibility = View.GONE
        val inflater = LayoutInflater.from(this)
        notes.forEach { note ->
            val itemView = inflater.inflate(R.layout.item_note, notesListContainer, false)
            val tvNoteIcon = itemView.findViewById<TextView>(R.id.tvNoteIcon)
            val tvNoteTitle = itemView.findViewById<TextView>(R.id.tvNoteTitle)
            val tvNoteMeta = itemView.findViewById<TextView>(R.id.tvNoteMeta)
            val tvNoteContent = itemView.findViewById<TextView>(R.id.tvNoteContent)
            val tvPinnedBadge = itemView.findViewById<TextView>(R.id.tvPinnedBadge)
            val btnEditNote = itemView.findViewById<TextView>(R.id.btnEditNote)
            val btnDeleteNote = itemView.findViewById<TextView>(R.id.btnDeleteNote)

            tvNoteIcon.text = noteIcon(note.type)
            tvNoteTitle.text = note.title
            tvNoteMeta.text = note.type.uppercase()
            tvNoteContent.text = note.content.ifBlank { "(No content)" }
            tvPinnedBadge.visibility = if (note.pinned) View.VISIBLE else View.GONE

            btnEditNote.setOnClickListener {
                startEditing(note)
            }
            btnDeleteNote.setOnClickListener {
                deleteNote(note)
            }

            notesListContainer.addView(itemView)
        }
    }

    private fun startEditing(note: Note) {
        editingNote = note
        etNoteTitle.setText(note.title)
        etNoteContent.setText(note.content)
        btnSaveNote.text = "✦ UPDATE SCROLL"
        toggleEditor(true)

        when (note.type) {
            "reminder" -> btnTypeReminder.performClick()
            "flashcard" -> btnTypeFlashcard.performClick()
            else -> btnTypeNote.performClick()
        }
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            noteRepository.deleteNote(note.id)
                .onSuccess {
                    if (editingNote?.id == note.id) {
                        toggleEditor(false)
                    }
                    Toast.makeText(this@NotesActivity, "Scroll deleted", Toast.LENGTH_SHORT).show()
                    loadNotes()
                }
                .onFailure {
                    Toast.makeText(
                        this@NotesActivity,
                        it.message ?: "Failed to delete note",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun noteIcon(type: String): String {
        return when (type) {
            "reminder" -> "🔔"
            "flashcard" -> "🃏"
            else -> "📝"
        }
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
        // Current is Notes
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
