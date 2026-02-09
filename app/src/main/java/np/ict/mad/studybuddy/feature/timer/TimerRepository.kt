package np.ict.mad.studybuddy.feature.timer

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class TimerRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    // ---- Last duration (seconds) ----
    fun saveDuration(durationSeconds: Long) {
        prefs.edit()
            .putLong(KEY_LAST_DURATION, durationSeconds)
            .apply()
    }

    fun getSavedDuration(): Long = prefs.getLong(KEY_LAST_DURATION, 0L)

    // ---- Presets stored as JSON ----
    fun getPresets(): List<TimerPreset> {
        val json = prefs.getString(KEY_PRESETS, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<TimerPreset>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val name = obj.optString("name")
            val secs = obj.optLong("seconds", 0L)
            if (name.isNotBlank() && secs > 0) {
                list += TimerPreset(name, secs)
            }
        }
        return list.sortedBy { it.name.lowercase() }
    }

    /** Upsert by name (case-insensitive). */
    fun upsertPreset(name: String, seconds: Long) {
        val current = getPresets().toMutableList()
        val idx = current.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (idx >= 0) current[idx] = TimerPreset(name, seconds)
        else current += TimerPreset(name, seconds)
        savePresets(current)
    }

    fun deletePreset(name: String) {
        val filtered = getPresets().filterNot { it.name.equals(name, ignoreCase = true) }
        savePresets(filtered)
    }

    private fun savePresets(list: List<TimerPreset>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject()
                    .put("name", p.name)
                    .put("seconds", p.seconds)
            )
        }
        prefs.edit()
            .putString(KEY_PRESETS, arr.toString())
            .apply()
    }

    // ---- Persist selected video URI ----
    fun saveVideoUri(uriString: String?) {
        prefs.edit().apply {
            if (uriString.isNullOrBlank()) remove(KEY_VIDEO_URI)
            else putString(KEY_VIDEO_URI, uriString)
        }.apply()
    }

    fun getVideoUri(): String? = prefs.getString(KEY_VIDEO_URI, null)

    companion object {
        private const val KEY_LAST_DURATION = "last_duration"
        private const val KEY_PRESETS = "presets_json"
        private const val KEY_VIDEO_URI = "study_video_uri"
    }
}