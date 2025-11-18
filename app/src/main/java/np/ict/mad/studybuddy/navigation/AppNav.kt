package np.ict.mad.studybuddy.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.feature.auth.LoginPreferences
import np.ict.mad.studybuddy.feature.auth.LoginScreen
import np.ict.mad.studybuddy.feature.home.HomeScreen
import np.ict.mad.studybuddy.feature.notes.NotesScreen
import np.ict.mad.studybuddy.feature.motivation.MotivationScreen
import np.ict.mad.studybuddy.feature.motivation.FavouriteScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // DataStore (remember me)
    val context = LocalContext.current
    val loginPrefs = remember { LoginPreferences(context) }

    val isLoggedIn by loginPrefs.isLoggedIn.collectAsState(initial = false)
    val savedUsername by loginPrefs.username.collectAsState(initial = "")

    // Auto login on app start
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !savedUsername.isNullOrEmpty()) {
            nav.navigate("home/$savedUsername") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = nav, startDestination = "login") {

        // LOGIN SCREEN
        composable("login") {
            LoginScreen(
                loginPrefs = loginPrefs,
                onLoginSuccess = { username ->
                    nav.navigate("home/$username") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // HOME SCREEN
        composable(
            route = "home/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            val scope = rememberCoroutineScope()

            HomeScreen(
                username = username,
                onOpenNotes = { nav.navigate("notes") },
                onOpenMotivation = { nav.navigate("motivation") },
                onLogout = {
                    scope.launch { loginPrefs.logout() }

                    nav.navigate("login") {
                        popUpTo("home/$username") { inclusive = true }
                    }
                }
            )
        }

        // NOTES SCREEN
        composable("notes") {
            NotesScreen()
        }

        // MOTIVATION SCREEN
        composable("motivation") {
            MotivationScreen(
                onOpenFavourites = { nav.navigate("favourites") }
            )
        }

        // FAVOURITES SCREEN
        composable("favourites") {
            FavouriteScreen()
        }
    }
}
