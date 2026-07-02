package com.example.mobileapp.data

import android.os.CountDownTimer
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object TimerManager {

    private var userRepository: UserRepository? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @JvmStatic
    fun initialize(repository: UserRepository) {
        this.userRepository = repository
    }

    private var countDownTimer: CountDownTimer? = null

    private val _timeLeftMs = MutableStateFlow(25 * 60 * 1000L)
    val timeLeftMs: StateFlow<Long> = _timeLeftMs.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerFinished = MutableStateFlow(false)
    val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

    private var currentTimerLengthMs = 25 * 60 * 1000L

    @JvmStatic
    fun setTimer(minutes: Int) {
        stopTimer()
        currentTimerLengthMs = minutes * 60 * 1000L
        _timeLeftMs.value = currentTimerLengthMs
        _timerFinished.value = false
    }

    @JvmStatic
    fun startTimer() {
        if (_isTimerRunning.value) return

        countDownTimer = object : CountDownTimer(_timeLeftMs.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeftMs.value = millisUntilFinished
            }

            override fun onFinish() {
                _timeLeftMs.value = 0
                _isTimerRunning.value = false
                _timerFinished.value = true
                
                // Record focus time
                val repository = userRepository
                val minutes = getTimerLengthMinutes()
                if (repository != null) {
                    val uid = repository.getCurrentUserId()
                    if (uid != null) {
                        scope.launch {
                            repository.addFocusMinutes(uid, minutes)
                        }
                    }
                }
            }
        }.start()

        _isTimerRunning.value = true
        _timerFinished.value = false
    }

    @JvmStatic
    fun pauseTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false
    }

    @JvmStatic
    fun resetTimer() {
        stopTimer()
        _timeLeftMs.value = currentTimerLengthMs
        _timerFinished.value = false
    }

    @JvmStatic
    fun stopTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false
    }
    
    @JvmStatic
    fun clearFinishedFlag() {
        _timerFinished.value = false
    }

    @JvmStatic
    fun getTimerLengthMinutes(): Int {
        return (currentTimerLengthMs / 60000).toInt()
    }
}
