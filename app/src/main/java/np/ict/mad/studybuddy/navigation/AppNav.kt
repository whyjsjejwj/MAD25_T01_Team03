package np.ict.mad.studybuddy.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.feature.auth.ForgotPasswordScreen
import np.ict.mad.studybuddy.feature.auth.LoginPreferences
import np.ict.mad.studybuddy.feature.auth.LoginScreen
import np.ict.mad.studybuddy.feature.auth.RegisterScreen
import np.ict.mad.studybuddy.feature.home.HomeScreen
import np.ict.mad.studybuddy.feature.profile.ProfileScreen
import np.ict.mad.studybuddy.feature.notes.NotesScreen
import np.ict.mad.studybuddy.feature.notes.EditNoteScreen
import np.ict.mad.studybuddy.feature.motivation.MotivationScreen
import np.ict.mad.studybuddy.feature.motivation.FavouriteScreen

@Composable
fun AppNav() {

    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val loginPrefs = remember { LoginPreferences(context) }

    val isLoggedIn by loginPrefs.isLoggedIn.collectAsState(initial = false)
    val savedUid by loginPrefs.uid.collectAsState(initial = "")
    val savedDisplayName by loginPrefs.displayName.collectAsState(initial = "")
    val savedEmail by loginPrefs.email.collectAsState(initial = "")

    var autoLoginDone by remember { mutableStateOf(false) }

    // AUTO-LOGIN
    LaunchedEffect(isLoggedIn, savedUid) {
        if (!autoLoginDone && isLoggedIn && savedUid.orEmpty().isNotBlank()) {
            autoLoginDone = true
            nav.navigate("home/$savedUid/$savedDisplayName/$savedEmail") {
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

                    // 🔥 FIXED: Get email from Firebase (NOT from savedEmail)
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

                    nav.navigate("home/$uid/$displayName/$email") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateRegister = { nav.navigate("register") },
                onNavigateForgotPassword = { nav.navigate("forgotPassword") }
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
            route = "home/{uid}/{displayName}/{email}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val uid = backStackEntry.arguments!!.getString("uid")!!
            val displayName = backStackEntry.arguments!!.getString("displayName")!!
            val email = backStackEntry.arguments!!.getString("email")!!

            HomeScreen(
                uid = uid,
                displayName = displayName,
                email = email,
                onOpenProfile = {
                    nav.navigate("profile/$uid/$displayName/$email")
                },
                onOpenNotes = { nav.navigate("notes/$uid/$displayName/$email") },
                onOpenMotivation = { nav.navigate("motivation/$uid/$displayName/$email") }
            )
        }

        // NOTES SCREEN
        composable(
            route = "notes/{uid}/{displayName}/{email}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStack ->

            val uid = backStack.arguments!!.getString("uid")!!
            val displayName = backStack.arguments!!.getString("displayName")!!
            val email = backStack.arguments!!.getString("email")!!

            NotesScreen(
                username = uid,
                onEdit = { noteId -> nav.navigate("editNote/$uid/$noteId") },
                onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                onOpenMotivation = { nav.navigate("motivation/$uid/$displayName/$email") }
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

        // PROFILE SCREEN
        composable(
            route = "profile/{uid}/{displayName}/{email}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStack ->

            val displayName = backStack.arguments!!.getString("displayName")!!
            val email = backStack.arguments!!.getString("email")!!

            ProfileScreen(
                displayName = displayName,
                email = email,
                onBack = { nav.popBackStack() },
                onLogout = {
                    scope.launch { loginPrefs.logout() }
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // MOTIVATION SCREEN
        composable(
            route = "motivation/{uid}/{displayName}/{email}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStack ->

            val uid = backStack.arguments!!.getString("uid")!!
            val displayName = backStack.arguments!!.getString("displayName")!!
            val email = backStack.arguments!!.getString("email")!!

            MotivationScreen(
                uid = uid,
                displayName = displayName,
                email = email,
                onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                onOpenNotes = { nav.navigate("notes/$uid/$displayName/$email") },
                onOpenMotivation = { /* already here, do nothing */ },
                onOpenFavourites = { nav.navigate("favourites/$uid/$displayName/$email") }
            )
        }

// FAVOURITES SCREEN
        composable(
            route = "favourites/{uid}/{displayName}/{email}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("displayName") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStack ->

            val uid = backStack.arguments!!.getString("uid")!!
            val displayName = backStack.arguments!!.getString("displayName")!!
            val email = backStack.arguments!!.getString("email")!!

            FavouriteScreen(
                uid = uid,
                displayName = displayName,
                email = email,
                onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                onOpenNotes = { nav.navigate("notes/$uid/$displayName/$email") },
                onOpenMotivation = { nav.navigate("motivation/$uid/$displayName/$email") }
            )
        }


        composable("forgotPassword") {
            ForgotPasswordScreen(
                onBackToLogin = { nav.popBackStack() }
            )
        }
    }
}
