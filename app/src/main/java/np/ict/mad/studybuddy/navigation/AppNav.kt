package np.ict.mad.studybuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import np.ict.mad.studybuddy.feature.auth.LoginScreen
import np.ict.mad.studybuddy.feature.home.HomeScreen
import np.ict.mad.studybuddy.feature.notes.NotesScreen
import np.ict.mad.studybuddy.feature.motivation.MotivationScreen
import np.ict.mad.studybuddy.feature.motivation.FavouriteScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { username ->
                    nav.navigate("home/$username") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "home/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStack ->
            val username = backStack.arguments?.getString("username").orEmpty()
            HomeScreen(
                username = username,
                onOpenNotes = { nav.navigate("notes") },
                onOpenMotivation = { nav.navigate("motivation") }
            )
        }

        composable("notes") { NotesScreen() }

        composable("motivation") {
            MotivationScreen(
                onOpenFavourites = { nav.navigate("favourites") }
            )
        }

        composable("favourites") {
            FavouriteScreen()
        }

    }
}