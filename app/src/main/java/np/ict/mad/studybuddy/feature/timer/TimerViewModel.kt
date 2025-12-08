package np.ict.mad.studybuddy.feature.timer

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ViewModel for managing the timer logic and state
// AndroidViewModel is used because we need Application context for TimerRepository
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    // Tracks whether the timer is currently running
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    // Tracks whether the timer is paused
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    // Holds the remaining time in seconds
    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime

    // Android CountDownTimer for scheduling timer ticks
    private var countDownTimer: CountDownTimer? = null

    // Repository for persisting and retrieving last timer duration
    private val timerRepository: TimerRepository = TimerRepository(application)

    // Directly set the remaining time (in seconds)
    fun setTimer(seconds: Long) {
        _remainingTime.value = seconds
    }

    // Set the timer using minutes and seconds
    // Also saves this duration so it can be reused later
    fun setTimerWithSeconds(minutes: Long, seconds: Long) {
        val totalSeconds = (minutes * 60) + seconds
        setTimer(totalSeconds)
        saveDuration(totalSeconds) // Persist last timer duration
    }

    // Start the countdown timer
    fun startTimer() {
        _isRunning.value = true // Mark timer as running
        _isPaused.value = false // Reset paused state

        // Initialize CountDownTimer with remaining time converted to milliseconds
        countDownTimer = object : CountDownTimer(_remainingTime.value * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update remaining time every second
                _remainingTime.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                // Stop the timer when finished
                stopTimer()
            }
        }.start()
    }

    // Pause the timer
    fun pauseTimer() {
        _isPaused.value = true
        countDownTimer?.cancel() // Cancel current CountDownTimer to stop ticking
    }

    // Resume a paused timer
    fun resumeTimer() {
        _isPaused.value = false
        startTimer() // Start a new CountDownTimer using remaining time
    }

    // Stop and reset the timer completely
    fun stopTimer() {
        _isRunning.value = false
        _isPaused.value = false
        countDownTimer?.cancel() // Cancel any ongoing timer
        _remainingTime.value = 0 // Reset remaining time to zero
    }

    // Save the timer duration to persistent storage
    fun saveDuration(duration: Long) {
        timerRepository.saveDuration(duration)
    }

    // Retrieve the last saved timer duration
    fun getSavedDuration(): Long {
        return timerRepository.getSavedDuration()
    }
}
