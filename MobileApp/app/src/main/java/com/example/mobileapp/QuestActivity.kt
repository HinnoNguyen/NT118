package com.example.mobileapp

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.TaskRepositoryImpl
import com.example.mobileapp.domain.model.Task
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class QuestActivity : AppCompatActivity() {
    private val taskRepository = TaskRepositoryImpl()
    private val priorities = listOf("high", "normal", "low")
    private var selectedPriority = "normal"

    private lateinit var etQuestTitle: EditText
    private lateinit var tvQuestPriority: TextView
    private lateinit var btnAddQuest: MaterialButton
    private lateinit var tvQuestsStatus: TextView
    private lateinit var questsListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest)
        setupQuestUi()
        setupNavigation()
    }

    private fun setupQuestUi() {
        etQuestTitle = findViewById(R.id.etQuestTitle)
        tvQuestPriority = findViewById(R.id.tvQuestPriority)
        btnAddQuest = findViewById(R.id.btnAddQuest)
        tvQuestsStatus = findViewById(R.id.tvQuestsStatus)
        questsListContainer = findViewById(R.id.questsListContainer)

        updatePriorityLabel()
        tvQuestPriority.setOnClickListener {
            selectedPriority = priorities[(priorities.indexOf(selectedPriority) + 1) % priorities.size]
            updatePriorityLabel()
        }
        btnAddQuest.setOnClickListener { saveTask() }

        loadTasks()
    }

    private fun saveTask() {
        lifecycleScope.launch {
            taskRepository.createTask(
                title = etQuestTitle.text.toString(),
                priority = selectedPriority
            ).onSuccess {
                Toast.makeText(this@QuestActivity, "Quest added", Toast.LENGTH_SHORT).show()
                clearForm()
                loadTasks()
            }.onFailure {
                Toast.makeText(this@QuestActivity, it.message ?: "Failed to add quest", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearForm() {
        etQuestTitle.text?.clear()
        selectedPriority = "normal"
        updatePriorityLabel()
    }

    private fun loadTasks() {
        tvQuestsStatus.visibility = View.VISIBLE
        tvQuestsStatus.text = "Loading quests..."
        questsListContainer.removeAllViews()

        lifecycleScope.launch {
            taskRepository.getTasks()
                .onSuccess { renderTasks(it) }
                .onFailure {
                    tvQuestsStatus.text = it.message ?: "Failed to load quests"
                    tvQuestsStatus.visibility = View.VISIBLE
                }
        }
    }

    private fun renderTasks(tasks: List<Task>) {
        questsListContainer.removeAllViews()
        if (tasks.isEmpty()) {
            tvQuestsStatus.text = "No quests yet. Add your first quest."
            tvQuestsStatus.visibility = View.VISIBLE
            return
        }

        tvQuestsStatus.visibility = View.GONE
        val inflater = LayoutInflater.from(this)
        tasks.forEach { task ->
            val itemView = inflater.inflate(R.layout.item_task, questsListContainer, false)
            val cbTaskCompleted = itemView.findViewById<CheckBox>(R.id.cbTaskCompleted)
            val tvTaskTitle = itemView.findViewById<TextView>(R.id.tvTaskTitle)
            val tvTaskMeta = itemView.findViewById<TextView>(R.id.tvTaskMeta)
            val tvTaskXp = itemView.findViewById<TextView>(R.id.tvTaskXp)
            val btnDeleteTask = itemView.findViewById<TextView>(R.id.btnDeleteTask)

            cbTaskCompleted.setOnCheckedChangeListener(null)
            cbTaskCompleted.isChecked = task.completed
            tvTaskTitle.text = task.title
            tvTaskMeta.text = buildTaskMeta(task)
            tvTaskXp.text = xpLabel(task.priority)
            tvTaskXp.alpha = if (task.completed) 0.5f else 1f

            if (task.completed) {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTaskTitle.alpha = 0.6f
            } else {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTaskTitle.alpha = 1f
            }

            cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                toggleTaskCompleted(task, isChecked)
            }
            btnDeleteTask.setOnClickListener {
                deleteTask(task)
            }

            questsListContainer.addView(itemView)
        }
    }

    private fun toggleTaskCompleted(task: Task, completed: Boolean) {
        lifecycleScope.launch {
            taskRepository.updateTask(task.copy(completed = completed))
                .onSuccess {
                    Toast.makeText(
                        this@QuestActivity,
                        if (completed) "Quest completed" else "Quest reopened",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadTasks()
                }
                .onFailure {
                    Toast.makeText(this@QuestActivity, it.message ?: "Failed to update quest", Toast.LENGTH_SHORT).show()
                    loadTasks()
                }
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            taskRepository.deleteTask(task.id)
                .onSuccess {
                    Toast.makeText(this@QuestActivity, "Quest deleted", Toast.LENGTH_SHORT).show()
                    loadTasks()
                }
                .onFailure {
                    Toast.makeText(this@QuestActivity, it.message ?: "Failed to delete quest", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updatePriorityLabel() {
        val label = when (selectedPriority) {
            "high" -> "A ⌵"
            "low" -> "C ⌵"
            else -> "B ⌵"
        }
        tvQuestPriority.text = label
        val color = when (selectedPriority) {
            "high" -> getColor(R.color.rank_s)
            "low" -> getColor(R.color.rank_c)
            else -> getColor(R.color.rank_b)
        }
        tvQuestPriority.setTextColor(color)
    }

    private fun buildTaskMeta(task: Task): String {
        val rank = when (task.priority) {
            "high" -> "RANK A"
            "low" -> "RANK C"
            else -> "RANK B"
        }
        val state = if (task.completed) "COMPLETED" else "ACTIVE"
        return "$rank • $state"
    }

    private fun xpLabel(priority: String): String {
        return when (priority) {
            "high" -> "+50xp"
            "low" -> "+15xp"
            else -> "+30xp"
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener { startActivity(Intent(this, TimerActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener { startActivity(Intent(this, NotesActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener { startActivity(Intent(this, StoryActivity::class.java)); finish() }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish() }
    }
}
