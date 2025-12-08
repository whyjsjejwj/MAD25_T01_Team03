package np.ict.mad.studybuddy.feature.timer

import android.content.Context
import android.content.SharedPreferences

// Repository class responsible for persisting timer-related data
// This abstraction allows the rest of the app to save and retrieve timer info
class TimerRepository(context: Context) {

    // Initialize SharedPreferences using a private file named "timer_prefs"
    // Context.MODE_PRIVATE ensures that only this app can access the preferences
    private val prefs: SharedPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    // Save the last timer duration in milliseconds (or seconds, depending on usage)
    // Using SharedPreferences for lightweight local storage
    fun saveDuration(duration: Long) {
        prefs.edit() // Begin editing SharedPreferences
            .putLong("last_duration", duration) // Store the duration with key "last_duration"
            .apply() // Apply changes asynchronously
    }

    // Retrieve the last saved timer duration
    // Returns 0 if no value has been stored yet
    fun getSavedDuration(): Long {
        return prefs.getLong("last_duration", 0)
    }
}
