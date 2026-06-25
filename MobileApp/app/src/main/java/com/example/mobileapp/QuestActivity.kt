package com.example.mobileapp

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.presentation.quest.QuestViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class QuestActivity : AppCompatActivity() {

    private val viewModel: QuestViewModel by viewModels { QuestViewModel.factory() }
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest)
        setupNavigation()
        setupUI()
        observeViewModel()
        auth.currentUser?.uid?.let { viewModel.loadTasks(it) }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener { startActivity(Intent(this, TimerActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }

    private fun setupUI() {
        val btnAddQuest = findViewById<MaterialButton>(R.id.btnAddQuest)
        val etTitle = findViewById<EditText>(R.id.etQuestTitle)

        btnAddQuest?.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            viewModel.addTask(uid, etTitle?.text?.toString() ?: "")
            etTitle?.text?.clear()
        }
    }

    private fun observeViewModel() {
        val questListContainer = findViewById<LinearLayout>(R.id.questListContainer)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasks.collect { tasks ->
                        renderTasks(questListContainer, tasks)
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        msg?.let { Toast.makeText(this@QuestActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }

    private fun renderTasks(container: LinearLayout?, tasks: List<Task>) {
        container?.removeAllViews() ?: return
        val uid = auth.currentUser?.uid ?: return
        val dp = resources.displayMetrics.density

        tasks.forEach { task ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (8 * dp).toInt() }
                setBackgroundResource(R.drawable.bg_main_card)
                setPadding((12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt())
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val checkbox = CheckBox(this).apply {
                isChecked = task.completed
                buttonTintList = android.content.res.ColorStateList.valueOf(0xFF57E389.toInt())
                setOnCheckedChangeListener { _, _ -> viewModel.toggleComplete(uid, task) }
            }

            val titleView = TextView(this).apply {
                text = task.title
                setTextColor(if (task.completed) 0xFF666666.toInt() else 0xFF57E389.toInt())
                textSize = 13f
                typeface = android.graphics.Typeface.MONOSPACE
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (8 * dp).toInt()
                }
                if (task.completed) paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            val deleteBtn = TextView(this).apply {
                text = "✕"
                setTextColor(0xFFFF4444.toInt())
                textSize = 14f
                setPadding((8 * dp).toInt(), 0, 0, 0)
                setOnClickListener { viewModel.deleteTask(uid, task.id) }
            }

            row.addView(checkbox)
            row.addView(titleView)
            row.addView(deleteBtn)
            container.addView(row)
        }
    }
}
