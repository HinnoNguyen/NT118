package com.example.mobileapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.activity.viewModels
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

        questAdapter = QuestAdapter { taskId, completed ->
            viewModel.toggleTask(taskId, completed)
        }

        rvQuests.apply {
            layoutManager = LinearLayoutManager(this@QuestActivity)
            adapter = questAdapter
        }

        btnAddQuest.setOnClickListener {
            val title = etQuestTitle.text.toString()
            if (title.isNotBlank()) {
                viewModel.addTask(title)
                etQuestTitle.text.clear()
            } else {
                Toast.makeText(this, "Please enter a quest name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasks.collect { tasks ->
                        questAdapter.submitList(tasks)
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@QuestActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

}
