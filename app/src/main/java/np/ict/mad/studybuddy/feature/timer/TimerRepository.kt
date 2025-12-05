package np.ict.mad.studybuddy.feature.timer

import android.content.Context
import android.content.SharedPreferences

class TimerRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    fun saveDuration(duration: Long) {
        prefs.edit().putLong("last_duration", duration).apply()
    }

    fun getSavedDuration(): Long {
        return prefs.getLong("last_duration", 0)
    }
}