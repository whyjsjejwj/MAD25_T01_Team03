package np.ict.mad.studybuddy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.feature.auth.ForgotPasswordScreen
import np.ict.mad.studybuddy.feature.auth.LoginPreferences
import np.ict.mad.studybuddy.feature.auth.LoginScreen
import np.ict.mad.studybuddy.feature.auth.RegisterScreen
import np.ict.mad.studybuddy.feature.home.HomeScreen
import np.ict.mad.studybuddy.feature.motivation.FavouriteScreen
import np.ict.mad.studybuddy.feature.motivation.MotivationScreen
import np.ict.mad.studybuddy.feature.notes.EditNoteScreen
import np.ict.mad.studybuddy.feature.notes.NotesScreen
import np.ict.mad.studybuddy.feature.profile.ProfileScreen
import np.ict.mad.studybuddy.feature.quiz.QuizScreen
import np.ict.mad.studybuddy.feature.timer.FloatingTimer
import np.ict.mad.studybuddy.feature.timer.TimerScreen
import np.ict.mad.studybuddy.feature.timer.TimerViewModel

@Composable
fun AppNav() {

    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val loginPrefs = remember { LoginPreferences(context) }
    val timerViewModel: TimerViewModel = viewModel()

    val isLoggedIn by loginPrefs.isLoggedIn.collectAsState(initial = false)
    val savedUid by loginPrefs.uid.collectAsState(initial = "")
    val savedDisplayName by loginPrefs.displayName.collectAsState(initial = "")
    val savedEmail by loginPrefs.email.collectAsState(initial = "")

    var autoLoginDone by remember { mutableStateOf(false) }

    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // AUTO-LOGIN
    LaunchedEffect(isLoggedIn, savedUid) {
        if (!autoLoginDone && isLoggedIn && savedUid.orEmpty().isNotBlank()) {
            autoLoginDone = true
            nav.navigate("home/$savedUid/$savedDisplayName/$savedEmail") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "login") {

            // LOGIN SCREEN
            composable("login") {
                LoginScreen(
                    loginPrefs = loginPrefs,
                    onLoginSuccess = { uid, displayName ->

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

                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val displayName = backStackEntry.arguments?.getString("displayName") ?: ""
                val email = backStackEntry.arguments?.getString("email") ?: ""

                HomeScreen(
                    nav = nav,
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    onOpenProfile = { nav.navigate("profile/$uid/$displayName/$email") },
                    onOpenTimer = { nav.navigate("timer/$uid/$displayName/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$displayName/$email") },
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

                val uid = backStack.arguments?.getString("uid") ?: ""
                val displayName = backStack.arguments?.getString("displayName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

                NotesScreen(
                    username = uid,
                    onEdit = { noteId -> nav.navigate("editNote/$uid/$noteId") },
                    onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                    onOpenTimer = { nav.navigate("timer/$uid/$displayName/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$displayName/$email") },
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

                val displayName = backStack.arguments?.getString("displayName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

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
            // QUIZ SCREEN
            composable(
                route = "quiz/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStack ->

                val uid = backStack.arguments?.getString("uid") ?: ""
                val displayName = backStack.arguments?.getString("displayName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

                QuizScreen(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                    onOpenTimer = { nav.navigate("timer/$uid/$displayName/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$displayName/$email") },
                    onOpenQuiz = { /* already here */ }
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

                val uid = backStack.arguments?.getString("uid") ?: ""
                val displayName = backStack.arguments?.getString("displayName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

                MotivationScreen(
                    uid = uid,
                    onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                    onOpenTimer = { nav.navigate("timer/$uid/$displayName/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$displayName/$email") },
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

                val uid = backStack.arguments?.getString("uid") ?: ""
                val displayName = backStack.arguments?.getString("displayName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

                FavouriteScreen(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                    onOpenTimer = { nav.navigate("timer/$uid/$displayName/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$displayName/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$displayName/$email") }
                )
            }

            // Timer Screen
            composable(
                route = "timer/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStack ->
                val uid = backStack.arguments?.getString("uid") ?: ""
                val displayName = backStack.arguments?.getString("displayName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

                TimerScreen(
                    nav = nav,
                    viewModel = timerViewModel,
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    onOpenHome = { nav.navigate("home/$uid/$displayName/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$displayName/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$displayName/$email") },
                    onOpenTimer = { /* Already here */ }
                )
            }

            composable("forgotPassword") {
                ForgotPasswordScreen(
                    onBackToLogin = { nav.popBackStack() }
                )
            }
        }
        // Removed condition to force show if running
        FloatingTimer(timerViewModel = timerViewModel)
    }
}
