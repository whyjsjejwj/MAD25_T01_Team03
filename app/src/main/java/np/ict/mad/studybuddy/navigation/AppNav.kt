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
import np.ict.mad.studybuddy.feature.auth.RegisterScreen
import np.ict.mad.studybuddy.feature.home.HomeScreen
import np.ict.mad.studybuddy.feature.notes.NotesScreen
import np.ict.mad.studybuddy.feature.notes.EditNoteScreen
import np.ict.mad.studybuddy.feature.motivation.MotivationScreen
import np.ict.mad.studybuddy.feature.motivation.FavouriteScreen

@Composable
fun AppNav() {

    val nav = rememberNavController()

    val context = LocalContext.current
    val loginPrefs = remember { LoginPreferences(context) }

    val isLoggedIn by loginPrefs.isLoggedIn.collectAsState(initial = false)
    val savedUid by loginPrefs.uid.collectAsState(initial = "")
    val savedDisplayName by loginPrefs.displayName.collectAsState(initial = "")

    var autoLoginDone by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn, savedUid) {
        if (!autoLoginDone && isLoggedIn && !savedUid.isNullOrBlank()) {
            autoLoginDone = true
            nav.navigate("home/$savedUid/$savedDisplayName") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = nav, startDestination = "login") {

        // LOGIN SCREEN
        composable("login") {
            LoginScreen(
                loginPrefs = loginPrefs,
                onLoginSuccess = { uid, displayName ->
                    nav.navigate("home/$uid/$displayName") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateRegister = { nav.navigate("register") }
            )
        }

        // REGISTER SCREEN
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = { nav.popBackStack() }
            )
        }

        // HOME SCREEN
        composable(
            route = "home/{uid}/{displayName}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val uid = backStackEntry.arguments?.getString("uid").orEmpty()
            val displayName = backStackEntry.arguments?.getString("displayName").orEmpty()
            val scope = rememberCoroutineScope()

            HomeScreen(
                uid = uid,
                displayName = displayName,
                onOpenNotes = { nav.navigate("notes/$uid") },
                onOpenMotivation = { nav.navigate("motivation") },
                onLogout = {
                    scope.launch { loginPrefs.logout() }
                    nav.navigate("login") {
                        popUpTo("home/$uid/$displayName") { inclusive = true }
                    }
                }
            )
        }

        // NOTES LIST SCREEN
        composable(
            route = "notes/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStack ->

            val uid = backStack.arguments!!.getString("uid")!!

            NotesScreen(
                username = uid,  // now UID
                onEdit = { noteId ->
                    nav.navigate("editNote/$uid/$noteId")
                }
            )
        }

        // EDIT NOTE SCREEN
        composable(
            route = "editNote/{uid}/{noteId}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("noteId") { type = NavType.IntType }
            )
        ) { backStack ->

            val uid = backStack.arguments!!.getString("uid")!!
            val noteId = backStack.arguments!!.getInt("noteId")

            EditNoteScreen(
                username = uid,
                noteId = noteId,
                onBack = { nav.popBackStack() }
            )
        }

        composable("motivation") {
            MotivationScreen(onOpenFavourites = { nav.navigate("favourites") })
        }

        composable("favourites") {
            FavouriteScreen()
        }
    }
}
