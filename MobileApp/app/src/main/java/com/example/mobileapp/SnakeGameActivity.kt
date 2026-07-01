package com.example.mobileapp

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.example.mobileapp.util.AnimationUtils.popIn
import com.example.mobileapp.util.AnimationUtils.setBounceClick
import com.google.android.material.button.MaterialButton

class SnakeGameActivity : AppCompatActivity(), SnakeGameView.GameStateListener {

    private lateinit var snakeGameView: SnakeGameView
    private lateinit var tvScore: TextView
    
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
        showStartDialog()
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("SNAKE RUN")
        builder.setMessage("Navigate the snake using the D-Pad or swipe gestures.\nEat yellow orbs to grow and score points.\n\nReady?")
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
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("GAME OVER")
        builder.setMessage("Final Score: $finalScore\nEXP Gained: +$expGained XP")
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
}
