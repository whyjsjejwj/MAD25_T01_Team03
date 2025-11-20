package np.ict.mad.studybuddy.core.storage

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import np.ict.mad.studybuddy.feature.auth.User

class UserJsonStorage(private val context: Context) {

    fun loadUsers(): List<User> {
        val inputStream = context.assets.open("users.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        return Json.decodeFromString(json)
    }
}
