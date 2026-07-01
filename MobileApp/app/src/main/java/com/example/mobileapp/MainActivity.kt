package com.example.mobileapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.MainViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.example.mobileapp.util.AnimationUtils.setBounceClick
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory() }
    
    private lateinit var tvUserName: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvExpValue: TextView
    private lateinit var expProgressBar: ProgressBar
    private lateinit var ivUserAvatar: ImageView
    
    private lateinit var tvQuestsDone: TextView
    private lateinit var tvNotesWritten: TextView
    private lateinit var tvFocusTime: TextView
    private lateinit var tvStreak: TextView

    private lateinit var tvDailyQuestTasks: TextView
    private lateinit var tvDailyQuestFocus: TextView
    private lateinit var tvDailyQuestNote: TextView
    private lateinit var tvMainMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }


    private fun setupUI() {
        tvUserName = findViewById(R.id.tvUserName)
        tvLevel = findViewById(R.id.tvLevel)
        tvExpValue = findViewById(R.id.tvExpValue)
        expProgressBar = findViewById(R.id.expProgressBar)
        ivUserAvatar = findViewById(R.id.ivUserAvatar)
        
        tvQuestsDone = findViewById(R.id.tvQuestsDone)
        tvNotesWritten = findViewById(R.id.tvNotesWritten)
        tvFocusTime = findViewById(R.id.tvFocusTime)
        tvStreak = findViewById(R.id.tvStreak)

        tvDailyQuestTasks = findViewById(R.id.tvDailyQuestTasks)
        tvDailyQuestFocus = findViewById(R.id.tvDailyQuestFocus)
        tvDailyQuestNote = findViewById(R.id.tvDailyQuestNote)
        tvMainMessage = findViewById(R.id.tvMainMessage)
        
        val userInfoCard = findViewById<LinearLayout>(R.id.userInfoCard)
        userInfoCard.setBounceClick()
        userInfoCard.setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        val btnPlayGame = findViewById<LinearLayout>(R.id.btnPlayGame)
        btnPlayGame.setBounceClick()
        btnPlayGame.setOnClickListener {
            navigateTo(SnakeGameActivity::class.java)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        user?.let {
                            tvUserName.text = it.name
                            tvLevel.text = "LV.${it.level}"
                            tvExpValue.text = "${it.exp}/${it.level * 100}"
                            
                            expProgressBar.setProgress(it.exp % 100, true)
                            
                            tvQuestsDone.text = it.completedTaskCount.toString()
                            val hours = it.totalFocusMinutes / 60
                            val minutes = it.totalFocusMinutes % 60
                            tvFocusTime.text = "${hours}h ${minutes}m"

                            // Load Avatar
                            if (it.avatarUrl.isNotBlank()) {
                                val resId = resources.getIdentifier(it.avatarUrl, "drawable", packageName)
                                if (resId != 0) {
                                    ivUserAvatar.setImageResource(resId)
                                }
                            }
                            
                            // Daily Quest Focus Progress
                            val focusProgress = minOf(it.todayFocusMinutes, 30)
                            tvDailyQuestFocus.text = "${if (focusProgress >= 30) "■" else "□"} Focus for 30 min ($focusProgress/30)"
                        }
                    }
                }
                launch {
                    viewModel.notesCount.collect { count ->
                        tvNotesWritten.text = count.toString()
                        val noteDone = count > 0 
                        tvDailyQuestNote.text = "${if (noteDone) "■" else "□"} Write a note (${minOf(count, 1)}/1)"
                    }
                }
                launch {
                    viewModel.todayTasksCompleted.collect { count ->
                        val taskProgress = minOf(count, 3)
                        tvDailyQuestTasks.text = "${if (taskProgress >= 3) "■" else "□"} Complete 3 tasks ($taskProgress/3)"
                    }
                }
                launch {
                    viewModel.remainingQuests.collect { count ->
                        val newMessage = if (count > 0) {
                            "You have $count quests to complete today!"
                        } else {
                            "All daily quests completed! Great job hero!"
                        }
                        if (tvMainMessage.text != newMessage) {
                            tvMainMessage.text = newMessage
                            tvMainMessage.alpha = 0f
                            tvMainMessage.animate().alpha(1f).setDuration(500).start()
                        }
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
}
