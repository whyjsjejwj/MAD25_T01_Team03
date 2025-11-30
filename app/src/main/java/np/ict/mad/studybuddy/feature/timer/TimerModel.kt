package np.ict.mad.studybuddy.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    fun setTimer(seconds: Long) {
        _remainingTime.value = seconds
    }

    fun startTimer() {
        if (_remainingTime.value <= 0L) return
        _isRunning.value = true
        _isPaused.value = false

        viewModelScope.launch {
            while (_remainingTime.value > 0 && _isRunning.value) {
                if (!_isPaused.value) {
                    delay(1000)
                    _remainingTime.value -= 1
                } else {
                    delay(200)
                }
            }

            _isRunning.value = false
        }
    }

    fun pauseTimer() {
        _isPaused.value = true
    }

    fun resumeTimer() {
        _isPaused.value = false
    }

    fun stopTimer() {
        _isRunning.value = false
        _isPaused.value = false
    }

    fun reset() {
        stopTimer()
        _remainingTime.value = 0
    }
}
