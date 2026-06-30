package com.example.mobileapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.util.Random
import kotlin.math.abs

class SnakeGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    interface GameStateListener {
        fun onScoreUpdated(score: Int)
        fun onGameOver(finalScore: Int)
    }

    var gameStateListener: GameStateListener? = null

    // Grid size
    private val gridWidth = 20
    private val gridHeight = 20

    // Snake and Food state
    private val snake = ArrayList<Point>()
    private var food = Point(0, 0)
    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT

    // Game loop parameters
    private var isPlaying = false
    private var isGameOver = false
    private var score = 0

    // Graphics Paints
    private val snakePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val snakeStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val foodPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val random = Random()

    init {
        // Resolve colors
        val greenColor = ContextCompat.getColor(context, R.color.accent_green)
        val yellowColor = ContextCompat.getColor(context, R.color.accent_yellow)
        val darkBgColor = ContextCompat.getColor(context, R.color.bg_dark)

        snakePaint.color = greenColor
        snakeStrokePaint.color = darkBgColor
        foodPaint.color = yellowColor

        resetGame()
    }

    fun resetGame() {
        snake.clear()
        // Start in the middle
        val midX = gridWidth / 2
        val midY = gridHeight / 2
        snake.add(Point(midX, midY))
        snake.add(Point(midX - 1, midY))
        snake.add(Point(midX - 2, midY))

        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        score = 0
        isGameOver = false
        gameStateListener?.onScoreUpdated(score)
        spawnFood()
        invalidate()
    }

    fun startGame() {
        if (isGameOver) {
            resetGame()
        }
        isPlaying = true
    }

    fun pauseGame() {
        isPlaying = false
    }

    fun setDirection(newDirection: Direction) {
        if (!isPlaying || isGameOver) return

        // Prevent 180-degree turns
        when (newDirection) {
            Direction.UP -> if (direction != Direction.DOWN) nextDirection = newDirection
            Direction.DOWN -> if (direction != Direction.UP) nextDirection = newDirection
            Direction.LEFT -> if (direction != Direction.RIGHT) nextDirection = newDirection
            Direction.RIGHT -> if (direction != Direction.LEFT) nextDirection = newDirection
        }
    }

    private fun spawnFood() {
        var foodX: Int
        var foodY: Int
        do {
            foodX = random.nextInt(gridWidth)
            foodY = random.nextInt(gridHeight)
        } while (isPointOnSnake(foodX, foodY))
        food = Point(foodX, foodY)
    }

    private fun isPointOnSnake(x: Int, y: Int): Boolean {
        for (part in snake) {
            if (part.x == x && part.y == y) return true
        }
        return false
    }

    // Step/Update game state called by activity game loop
    fun update() {
        if (!isPlaying || isGameOver) return

        direction = nextDirection
        val head = snake[0]
        var newHeadX = head.x
        var newHeadY = head.y

        when (direction) {
            Direction.UP -> newHeadY--
            Direction.DOWN -> newHeadY++
            Direction.LEFT -> newHeadX--
            Direction.RIGHT -> newHeadX++
        }

        // Check bounds collision
        if (newHeadX < 0 || newHeadX >= gridWidth || newHeadY < 0 || newHeadY >= gridHeight) {
            endGame()
            return
        }

        // Check self collision
        if (isPointOnSnake(newHeadX, newHeadY)) {
            endGame()
            return
        }

        // Add new head
        val newHead = Point(newHeadX, newHeadY)
        snake.add(0, newHead)

        // Check food collision
        if (newHeadX == food.x && newHeadY == food.y) {
            score += 10
            gameStateListener?.onScoreUpdated(score)
            spawnFood()
        } else {
            // Remove tail if no food eaten
            snake.removeAt(snake.size - 1)
        }

        invalidate()
    }

    private fun endGame() {
        isPlaying = false
        isGameOver = true
        gameStateListener?.onGameOver(score)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellWidth = width.toFloat() / gridWidth
        val cellHeight = height.toFloat() / gridHeight

        // Draw Food
        val foodRect = RectF(
            food.x * cellWidth + 2,
            food.y * cellHeight + 2,
            (food.x + 1) * cellWidth - 2,
            (food.y + 1) * cellHeight - 2
        )
        canvas.drawOval(foodRect, foodPaint)

        // Draw Snake
        for (i in snake.indices) {
            val part = snake[i]
            val rect = RectF(
                part.x * cellWidth + 1,
                part.y * cellHeight + 1,
                (part.x + 1) * cellWidth - 1,
                (part.y + 1) * cellHeight - 1
            )
            
            canvas.drawRect(rect, snakePaint)
            canvas.drawRect(rect, snakeStrokePaint)
        }
    }

    // Touch Swipe Gestures
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > 100 && abs(velocityX) > 100) {
                    if (diffX > 0) {
                        setDirection(Direction.RIGHT)
                    } else {
                        setDirection(Direction.LEFT)
                    }
                    return true
                }
            } else {
                if (abs(diffY) > 100 && abs(velocityY) > 100) {
                    if (diffY > 0) {
                        setDirection(Direction.DOWN)
                    } else {
                        setDirection(Direction.UP)
                    }
                    return true
                }
            }
            return false
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    data class Point(val x: Int, val y: Int)
}
