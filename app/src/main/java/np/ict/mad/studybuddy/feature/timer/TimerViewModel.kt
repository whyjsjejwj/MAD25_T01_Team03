package np.ict.mad.studybuddy.feature.timer

import android.app.Application
import android.net.Uri
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _remainingTime = MutableStateFlow(0L) // total seconds
    val remainingTime: StateFlow<Long> = _remainingTime

    // Video state
    enum class VideoState { PLAY, PAUSE, STOP }
    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri
    private val _videoState = MutableStateFlow(VideoState.STOP)
    val videoState: StateFlow<VideoState> = _videoState

    private var countDownTimer: CountDownTimer? = null
    private var baseDurationSeconds: Long = 0L
    private val repo = TimerRepository(application)

    init {
        // Load only the last duration and video URI
        baseDurationSeconds = repo.getSavedDuration()
        _remainingTime.value = baseDurationSeconds
        repo.getVideoUri()?.let { _videoUri.value = Uri.parse(it) }
    }

    fun startTimer(mins: Int, secs: Int) {
        val totalSeconds = (mins * 60L) + secs
        if (totalSeconds <= 0 && _remainingTime.value <= 0) return

        if (!_isPaused.value) {
            baseDurationSeconds = totalSeconds
            _remainingTime.value = totalSeconds
            repo.saveDuration(totalSeconds)
        }

        _isRunning.value = true
        _isPaused.value = false
        _videoState.value = VideoState.PLAY

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(_remainingTime.value * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = (millisUntilFinished / 1000).coerceAtLeast(0)
            }
            override fun onFinish() {
                stopTimer()
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isPaused.value = true
        _videoState.value = VideoState.PAUSE
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _isPaused.value = false
        _remainingTime.value = baseDurationSeconds
        _videoState.value = VideoState.STOP
    }

    fun setVideoUri(uri: Uri?) {
        _videoUri.value = uri
        repo.saveVideoUri(uri?.toString())
    }
}
