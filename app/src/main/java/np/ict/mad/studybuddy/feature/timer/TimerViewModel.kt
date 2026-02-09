package np.ict.mad.studybuddy.feature.timer

import android.app.Application
import android.net.Uri
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    // Public observable state
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _remainingTime = MutableStateFlow(0L)  // seconds
    val remainingTime: StateFlow<Long> = _remainingTime

    private val _lastDuration = MutableStateFlow(0L)   // seconds
    val lastDuration: StateFlow<Long> = _lastDuration

    private val _presets = MutableStateFlow<List<TimerPreset>>(emptyList())
    val presets: StateFlow<List<TimerPreset>> = _presets

    // Video section
    enum class VideoState { PLAY, PAUSE, STOP }
    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri
    private val _videoState = MutableStateFlow(VideoState.STOP)
    val videoState: StateFlow<VideoState> = _videoState

    // Internals
    private var countDownTimer: CountDownTimer? = null
    private var baseDurationSeconds: Long = 0L
    private val repo = TimerRepository(application)

    init {
        // Load persisted values
        _lastDuration.value = repo.getSavedDuration()
        baseDurationSeconds = _lastDuration.value
        _presets.value = repo.getPresets()

        repo.getVideoUri()?.let { saved ->
            _videoUri.value = Uri.parse(saved)
        }
    }

    // --------------------------
    // Timer public API (seconds)
    // --------------------------
    fun setTimerFromSeconds(seconds: Long) {
        val safe = seconds.coerceAtLeast(1L)
        baseDurationSeconds = safe
        _remainingTime.value = safe
        repo.saveDuration(safe)
        _lastDuration.value = safe
    }

    fun startTimer() {
        if (_remainingTime.value <= 0) {
            val fallback = if (baseDurationSeconds > 0) baseDurationSeconds else _lastDuration.value
            if (fallback <= 0) return
            _remainingTime.value = fallback
            baseDurationSeconds = fallback
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
                autoReset()
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isPaused.value = true
        _videoState.value = VideoState.PAUSE
    }

    fun resumeTimer() {
        if (_remainingTime.value <= 0) {
            _remainingTime.value = baseDurationSeconds
        }
        _isPaused.value = false
        _videoState.value = VideoState.PLAY
        startTimer()
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _isPaused.value = false
        _remainingTime.value = baseDurationSeconds
        _videoState.value = VideoState.STOP
    }

    private fun autoReset() {
        _isRunning.value = false
        _isPaused.value = false
        _videoState.value = VideoState.STOP
        val resetVal = if (baseDurationSeconds > 0) baseDurationSeconds else _lastDuration.value
        _remainingTime.value = resetVal
    }

    // --------------------------
    // Presets
    // --------------------------
    fun savePreset(name: String, seconds: Long) {
        if (name.isBlank() || seconds <= 0) return
        repo.upsertPreset(name.trim(), seconds)
        _presets.value = repo.getPresets()
    }

    fun deletePreset(name: String) {
        repo.deletePreset(name)
        _presets.value = repo.getPresets()
    }

    fun loadPreset(preset: TimerPreset, autoStart: Boolean) {
        setTimerFromSeconds(preset.seconds)
        if (autoStart) startTimer()
    }

    // --------------------------
    // Video selection
    // --------------------------
    fun setVideoUri(uri: Uri?) {
        _videoUri.value = uri
        repo.saveVideoUri(uri?.toString())
    }

    fun clearVideoUri() {
        setVideoUri(null)
    }
}
