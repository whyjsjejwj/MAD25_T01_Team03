package np.ict.mad.studybuddy.feature.motivation

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MotivationStorage(context: Context) {

    private val prefs = context.getSharedPreferences("motivation_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<MotivationItem>>() {}.type

    fun getFavourites(): List<MotivationItem> {
        val json = prefs.getString("fav_list", null) ?: return emptyList()
        return gson.fromJson(json, type)
    }

    fun saveFavourite(item: MotivationItem) {
        val current = getFavourites().toMutableList()
        current.add(0, item) // newest on top
        prefs.edit().putString("fav_list", gson.toJson(current)).apply()
    }

    fun removeFavourite(item: MotivationItem) {
        val current = getFavourites().toMutableList()
        current.remove(item)
        prefs.edit().putString("fav_list", gson.toJson(current)).apply()
    }
}
