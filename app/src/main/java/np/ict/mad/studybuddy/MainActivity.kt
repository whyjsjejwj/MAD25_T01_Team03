package np.ict.mad.studybuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.FirebaseApp
import np.ict.mad.studybuddy.feature.auth.LoginPreferences
import np.ict.mad.studybuddy.ui.theme.StudyBuddyTheme
import np.ict.mad.studybuddy.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {


            val context = LocalContext.current
            val prefs = remember { LoginPreferences(context) }
            val savedTheme by prefs.theme.collectAsState(initial = "system")

            val useDarkTheme = when (savedTheme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            StudyBuddyTheme(darkTheme = useDarkTheme) {
                AppNav()
            }
        }
    }
}
