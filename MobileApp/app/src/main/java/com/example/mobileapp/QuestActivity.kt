package com.example.mobileapp

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.presentation.QuestAdapter
import com.example.mobileapp.presentation.QuestViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class QuestActivity : BaseActivity() {
    
    private val viewModel: QuestViewModel by viewModels { ViewModelFactory() }
    private lateinit var rvQuests: RecyclerView
    private lateinit var questAdapter: QuestAdapter
    private lateinit var etQuestTitle: EditText
    private lateinit var btnAddQuest: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest)
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        rvQuests = findViewById(R.id.rvQuests)
        etQuestTitle = findViewById(R.id.etQuestTitle)
        btnAddQuest = findViewById(R.id.btnAddQuest)
        val tvQuestRank = findViewById<TextView>(R.id.tvQuestRank)

        questAdapter = QuestAdapter { taskId, completed ->
            viewModel.toggleTask(taskId, completed)
        }

        rvQuests.apply {
            layoutManager = LinearLayoutManager(this@QuestActivity)
            adapter = questAdapter
        }

        var selectedPriority = "normal"

        tvQuestRank?.setOnClickListener {
            val ranks = arrayOf("Rank S (High - +50xp)", "Rank A (High - +50xp)", "Rank B (Normal - +20xp)", "Rank C (Low - +10xp)")
            android.app.AlertDialog.Builder(this)
                .setTitle("Select Quest Rank")
                .setItems(ranks) { _, which ->
                    when (which) {
                        0 -> {
                            tvQuestRank.text = "S ⌵"
                            tvQuestRank.setTextColor(android.graphics.Color.parseColor("#FF4444"))
                            selectedPriority = "high"
                        }
                        1 -> {
                            tvQuestRank.text = "A ⌵"
                            tvQuestRank.setTextColor(android.graphics.Color.parseColor("#FFD700"))
                            selectedPriority = "high"
                        }
                        2 -> {
                            tvQuestRank.text = "B ⌵"
                            tvQuestRank.setTextColor(android.graphics.Color.parseColor("#00FF00"))
                            selectedPriority = "normal"
                        }
                        3 -> {
                            tvQuestRank.text = "C ⌵"
                            tvQuestRank.setTextColor(android.graphics.Color.parseColor("#448AFF"))
                            selectedPriority = "low"
                        }
                    }
                }
                .show()
        }

        btnAddQuest.setOnClickListener {
            val title = etQuestTitle.text.toString()
            if (title.isNotBlank()) {
                viewModel.addTask(title, selectedPriority)
                etQuestTitle.text.clear()
                tvQuestRank?.text = "B ⌵"
                tvQuestRank?.setTextColor(android.graphics.Color.parseColor("#00FF00"))
                selectedPriority = "normal"
            } else {
                showAppNotification("Attention", "Please enter a quest name")
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { showLoading(it) }
                }
                launch {
                    viewModel.tasks.collect { tasks ->
                        questAdapter.submitList(tasks)
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            showAppNotification("System Error", it)
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

}
