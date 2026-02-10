package np.ict.mad.studybuddy.feature.timer

import android.content.Context
import android.content.SharedPreferences

class TimerRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    fun saveDuration(durationSeconds: Long) {
        prefs.edit().putLong("last_duration", durationSeconds).apply()
    }

    fun getSavedDuration(): Long = prefs.getLong("last_duration", 0L)

    fun saveVideoUri(uriString: String?) {
        prefs.edit().putString("study_video_uri", uriString).apply()
    }

    fun getVideoUri(): String? = prefs.getString("study_video_uri", null)
}