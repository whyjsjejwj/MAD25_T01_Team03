package np.ict.mad.studybuddy.feature.auth

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("login_prefs")

class LoginPreferences(private val context: Context) {

    private val KEY_UID = stringPreferencesKey("uid")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("displayName")
    private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")

    val uid: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_UID]
    }

    val email: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMAIL]
    }

    val displayName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DISPLAY_NAME]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] ?: false
    }

    suspend fun saveLogin(uid: String, email: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UID] = uid
            prefs[KEY_EMAIL] = email
            prefs[KEY_DISPLAY_NAME] = displayName
            prefs[KEY_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
