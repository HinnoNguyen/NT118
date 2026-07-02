package com.example.mobileapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.SnakeGameViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.example.mobileapp.util.AnimationUtils.popIn
import com.example.mobileapp.util.AnimationUtils.setBounceClick
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SnakeGameActivity : AppCompatActivity(), SnakeGameView.GameStateListener {

    private val viewModel: SnakeGameViewModel by viewModels { ViewModelFactory() }
    private lateinit var snakeGameView: SnakeGameView
    private lateinit var tvScore: TextView
    private var hasShownStartDialog = false
    
    private val handler = Handler(Looper.getMainLooper())
    private val gameTickMs = 250L // Slower snake speed (250ms per tick)

    private val gameRunnable = object : Runnable {
        override fun run() {
            snakeGameView.update()
            handler.postDelayed(this, gameTickMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_snake_game)
        setupEdgeToEdge()

        snakeGameView = findViewById(R.id.snakeGameView)
        tvScore = findViewById(R.id.tvScore)
        snakeGameView.gameStateListener = this

        findViewById<View>(R.id.gameBoardContainer).popIn(duration = 500)

        setupControls()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        if (user != null && !hasShownStartDialog) {
                            hasShownStartDialog = true
                            showStartDialog()
                        }
                    }
                }
                launch {
                    viewModel.expAwarded.collect { exp ->
                        exp?.let {
                            Toast.makeText(this@SnakeGameActivity, "Bonus +$it EXP awarded!", Toast.LENGTH_SHORT).show()
                            viewModel.clearExpFlag()
                        }
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@SnakeGameActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private fun setupControls() {
        findViewById<TextView>(R.id.btnBack).apply {
            setBounceClick()
            setOnClickListener {
                finish()
            }
        }

        val upButton = findViewById<MaterialButton>(R.id.btnUp)
        val downButton = findViewById<MaterialButton>(R.id.btnDown)
        val leftButton = findViewById<MaterialButton>(R.id.btnLeft)
        val rightButton = findViewById<MaterialButton>(R.id.btnRight)

        listOf(upButton, downButton, leftButton, rightButton).forEach { it.setBounceClick() }

        upButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                snakeGameView.setDirection(SnakeGameView.Direction.UP)
                v.performClick()
            }
            true
        }
        downButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                snakeGameView.setDirection(SnakeGameView.Direction.DOWN)
                v.performClick()
            }
            true
        }
        leftButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                snakeGameView.setDirection(SnakeGameView.Direction.LEFT)
                v.performClick()
            }
            true
        }
        rightButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                snakeGameView.setDirection(SnakeGameView.Direction.RIGHT)
                v.performClick()
            }
            true
        }
    }

    private fun showStartDialog() {
        val user = viewModel.userProfile.value
        val now = System.currentTimeMillis()
        val isNewDay = user?.let { !isSameDay(it.lastMiniGameRewardAt, now) } ?: true
        val currentCount = if (isNewDay) 0 else (user?.miniGameRewardCount ?: 0)
        val remaining = maxOf(0, 3 - currentCount)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("SNAKE RUN")
        builder.setMessage("Navigate the snake using the D-Pad or swipe gestures.\nEat yellow orbs to grow and score points.\n\nNote: Rewards are limited to 3 times per day.\nRemaining rewards today: $remaining/3\n\nReady?")
        builder.setCancelable(false)
        builder.setPositiveButton("START") { dialog, _ ->
            dialog.dismiss()
            startGame()
        }
        builder.setNegativeButton("EXIT") { _, _ ->
            finish()
        }
        
        val dialog = builder.create()
        dialog.show()
    }

    private fun startGame() {
        snakeGameView.startGame()
        handler.removeCallbacks(gameRunnable)
        handler.postDelayed(gameRunnable, gameTickMs)
    }

    private fun stopGame() {
        handler.removeCallbacks(gameRunnable)
        snakeGameView.pauseGame()
    }

    override fun onScoreUpdated(score: Int) {
        tvScore.text = "SCORE: $score"
    }

    override fun onGameOver(finalScore: Int) {
        stopGame()
        
        // Award bonus EXP (5 EXP per food eaten)
        val expGained = finalScore / 10 * 5
        if (expGained > 0) {
            viewModel.awardExp(expGained)
        }
        
        val user = viewModel.userProfile.value
        val now = System.currentTimeMillis()
        val isNewDay = user?.let { !isSameDay(it.lastMiniGameRewardAt, now) } ?: true
        val currentCount = if (isNewDay) 0 else (user?.miniGameRewardCount ?: 0)
        // If expGained > 0, we just requested to increment the count, so visually we should subtract one more if it's not already at limit
        val nextCount = if (expGained > 0) minOf(3, currentCount + 1) else currentCount
        val remaining = maxOf(0, 3 - nextCount)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("GAME OVER")
        builder.setMessage("Final Score: $finalScore\nEXP Gained: +$expGained XP\nRemaining rewards: $remaining/3")
        builder.setCancelable(false)
        builder.setPositiveButton("PLAY AGAIN") { dialog, _ ->
            dialog.dismiss()
            snakeGameView.resetGame()
            startGame()
        }
        builder.setNegativeButton("EXIT") { dialog, _ ->
            dialog.dismiss()
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        stopGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGame()
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
