package np.ict.mad.studybuddy.feature.timer

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime

    private var countDownTimer: CountDownTimer? = null
    private val timerRepository: TimerRepository = TimerRepository(application)

    fun setTimer(seconds: Long) {
        _remainingTime.value = seconds
    }

    fun setTimerWithSeconds(minutes: Long, seconds: Long) {
        val totalSeconds = (minutes * 60) + seconds
        setTimer(totalSeconds)
        saveDuration(totalSeconds)
    }

    fun startTimer() {
        _isRunning.value = true
        _isPaused.value = false

        countDownTimer = object : CountDownTimer(_remainingTime.value * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                stopTimer()
            }
        }.start()
    }

    fun pauseTimer() {
        _isPaused.value = true
        countDownTimer?.cancel()
    }

    fun resumeTimer() {
        _isPaused.value = false
        startTimer() // Resume from the paused time
    }

    fun stopTimer() {
        _isRunning.value = false
        _isPaused.value = false
        countDownTimer?.cancel()
        _remainingTime.value = 0
    }

    fun saveDuration(duration: Long) {
        timerRepository.saveDuration(duration)
    }

    fun getSavedDuration(): Long {
        return timerRepository.getSavedDuration()
    }
}