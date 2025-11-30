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

    // These are the keys I use to save data into DataStore.
    // Example: KEY_UID will store the user's UID, KEY_EMAIL stores the email, etc.
    private val KEY_UID = stringPreferencesKey("uid")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("displayName")
    private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")

    // Key for storing the selected theme (light/dark/system)
    private val KEY_THEME = stringPreferencesKey("theme_mode")  // "light" | "dark" | "system"

    // These are Flow variables that read the saved values from DataStore.
    //The Flow will automatically send the latest value
    // to the UI when it's ready, and its how my auto-login works.
    val uid: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_UID]
    }

    val email: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMAIL]
    }

    val displayName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DISPLAY_NAME]
    }

    // If logged_in was never saved, default to false.
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] ?: false
    }

    // Read saved theme mode, default “system”
    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "system"   // default: follow system
    }

    // Save the login details after the user successfully signs in.
    // This lets the app auto-login next time without typing.
    suspend fun saveLogin(uid: String, email: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UID] = uid
            prefs[KEY_EMAIL] = email
            prefs[KEY_DISPLAY_NAME] = displayName
            prefs[KEY_LOGGED_IN] = true
        }
    }

    // When the user logs out, clear everything from DataStore.
    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Save the selected theme (light/dark/system)
    suspend fun setTheme(value: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = value   // "light", "dark", or "system"
        }
    }
}
