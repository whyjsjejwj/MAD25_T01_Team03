package np.ict.mad.studybuddy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.UserDirectoryFirestore
import np.ict.mad.studybuddy.feature.auth.*
import np.ict.mad.studybuddy.feature.groups.DirectMessageSearchScreen
import np.ict.mad.studybuddy.feature.groups.GroupChatScreen
import np.ict.mad.studybuddy.feature.groups.GroupsScreen
import np.ict.mad.studybuddy.feature.home.HomeScreen
import np.ict.mad.studybuddy.feature.motivation.FavouriteScreen
import np.ict.mad.studybuddy.feature.motivation.MotivationScreen
import np.ict.mad.studybuddy.feature.notes.EditNoteScreen
import np.ict.mad.studybuddy.feature.notes.NotesScreen
import np.ict.mad.studybuddy.feature.profile.ProfileScreen
import np.ict.mad.studybuddy.feature.quiz.QuizScreen
import np.ict.mad.studybuddy.feature.reflection.StudyReflectionScreen
import np.ict.mad.studybuddy.feature.timer.*

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

    val directoryDb = remember { UserDirectoryFirestore() }

    // ---------- AUTO LOGIN ----------
    LaunchedEffect(isLoggedIn, savedUid, savedDisplayName, savedEmail) {
        val uid = savedUid.orEmpty()
        val name = savedDisplayName.orEmpty()
        val email = savedEmail.orEmpty()

        if (!autoLoginDone && isLoggedIn && savedUid.orEmpty().isNotBlank()) {
            autoLoginDone = true

            // ensure user is searchable for DM
            scope.launch {
                runCatching {
                    directoryDb.upsertProfile(uid, name, email)
                }
            }

            nav.navigate("home/$savedUid/$savedDisplayName/$savedEmail") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Helper: ALWAYS go to Home (not backstack)
    fun goHome(uid: String, name: String, email: String) {
        nav.navigate("home/$uid/$name/$email") {
            launchSingleTop = true
            popUpTo("home/{uid}/{displayName}/{email}") { inclusive = false }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = nav,
            startDestination = "login"
        ) {

            // ---------- LOGIN ----------
            composable("login") {
                LoginScreen(
                    loginPrefs = loginPrefs,
                    onLoginSuccess = { uid, displayName ->
                        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

                        // ensure user is searchable for DM
                        scope.launch {
                            runCatching {
                                directoryDb.upsertProfile(uid, displayName, email)
                            }
                        }

                        nav.navigate("home/$uid/$displayName/$email") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateRegister = { nav.navigate("register") },
                    onNavigateForgotPassword = { nav.navigate("forgotPassword") }
                )
            }

            // ---------- REGISTER ----------
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

            // ---------- HOME ----------
            composable(
                "home/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                HomeScreen(
                    nav = nav,
                    uid = uid,
                    displayName = name,
                    email = email,
                    onOpenProfile = { nav.navigate("profile/$uid/$name/$email") },
                    onOpenTimer = { nav.navigate("timer/$uid/$name/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$name/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$name/$email") },
                    onLogout = {
                        scope.launch { loginPrefs.logout() }
                        FirebaseAuth.getInstance().signOut()
                        nav.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- GROUPS ----------
            composable(
                "groups/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                GroupsScreen(
                    uid = uid,
                    displayName = name,
                    onOpenGroup = { groupId ->
                        nav.navigate("groupChat/$groupId/$uid/$name")
                    },
                    onNewDirectMessage = {
                        nav.navigate("dmSearch/$uid/$name")
                    },
                    onBack = { nav.popBackStack() }
                )
            }

            // ---------- NEW DM SEARCH ----------
            composable(
                "dmSearch/{uid}/{displayName}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!

                DirectMessageSearchScreen(
                    myUid = uid,
                    myDisplayName = name,
                    onBack = { nav.popBackStack() },
                    onOpenChat = { groupId ->
                        nav.navigate("groupChat/$groupId/$uid/$name")
                    }
                )
            }

            // ---------- GROUP CHAT ----------
            composable(
                "groupChat/{groupId}/{uid}/{displayName}",
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType }
                )
            ) { entry ->
                val groupId = entry.arguments!!.getString("groupId")!!
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!

                GroupChatScreen(
                    groupId = groupId,
                    uid = uid,
                    displayName = name,
                    onBack = { nav.popBackStack() }
                )
            }

            // ---------- NOTES ----------
            composable(
                "notes/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                NotesScreen(
                    username = uid,
                    onEdit = { noteId -> nav.navigate("editNote/$uid/$noteId") },
                    onOpenHome = { goHome(uid, name, email) },
                    onOpenTimer = { nav.navigate("timer/$uid/$name/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$name/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$name/$email") }
                )
            }

            // ---------- EDIT NOTE ----------
            composable(
                "editNote/{uid}/{noteId}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("noteId") { type = NavType.IntType }
                )
            ) { entry ->
                EditNoteScreen(
                    username = entry.arguments!!.getString("uid")!!,
                    noteId = entry.arguments!!.getInt("noteId"),
                    onBack = { nav.popBackStack() }
                )
            }

            // ---------- PROFILE ----------
            composable(
                "profile/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                ProfileScreen(
                    displayName = entry.arguments?.getString("displayName") ?: "",
                    email = entry.arguments?.getString("email") ?: "",
                    onBack = { nav.popBackStack() }
                )
            }

            // ---------- QUIZ ----------
            composable(
                "quiz/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                QuizScreen(
                    uid = uid,
                    displayName = name,
                    email = email,
                    onOpenHome = { goHome(uid, name, email) },
                    onOpenTimer = { nav.navigate("timer/$uid/$name/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$name/$email") },
                    onOpenQuiz = {}
                )
            }

            // ---------- STUDY REFLECTION ----------
            composable(
                "reflection/{uid}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!

                StudyReflectionScreen(
                    uid = uid,
                    onBack = { nav.popBackStack() }
                )
            }

            // ---------- MOTIVATION ----------
            composable(
                "motivation/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                MotivationScreen(
                    uid = uid,
                    onOpenHome = { goHome(uid, name, email) },
                    onOpenTimer = { nav.navigate("timer/$uid/$name/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$name/$email") },
                    onOpenMotivation = {},
                    onOpenFavourites = { nav.navigate("favourites/$uid/$name/$email") }
                )
            }

            // ---------- FAVOURITES ----------
            composable(
                "favourites/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                FavouriteScreen(
                    uid = uid,
                    displayName = name,
                    email = email,
                    onOpenHome = { goHome(uid, name, email) },
                    onOpenTimer = { nav.navigate("timer/$uid/$name/$email") },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$name/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$name/$email") }
                )
            }

            // ---------- TIMER ----------
            composable(
                "timer/{uid}/{displayName}/{email}",
                arguments = listOf(
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("displayName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { entry ->
                val uid = entry.arguments!!.getString("uid")!!
                val name = entry.arguments!!.getString("displayName")!!
                val email = entry.arguments!!.getString("email")!!

                TimerScreen(
                    nav = nav,
                    viewModel = timerViewModel,
                    uid = uid,
                    displayName = name,
                    email = email,
                    onOpenHome = { goHome(uid, name, email) },
                    onOpenQuiz = { nav.navigate("quiz/$uid/$name/$email") },
                    onOpenMotivation = { nav.navigate("motivation/$uid/$name/$email") },
                    onOpenTimer = {}
                )
            }

            // ---------- FORGOT PASSWORD ----------
            composable("forgotPassword") {
                ForgotPasswordScreen(onBackToLogin = { nav.popBackStack() })
            }
        }

        // Floating overlay timer
        FloatingTimer(timerViewModel = timerViewModel)
    }
}
