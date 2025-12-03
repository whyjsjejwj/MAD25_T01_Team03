package np.ict.mad.studybuddy.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


//Using enum cuz it's type-safe and I don't need to compare strings which may affect if typo occur.
enum class BottomNavTab { HOME, TIMER, QUIZ, MOTIVATION }

@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab, //the screen tells which tab is currently active
    onHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.HOME, //highlight Home if we are on Home screen
            onClick = onHome, //when clicked, navigate to Home
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.TIMER, //highlight Timer only if timer screen is active
            onClick = onOpenTimer,
            icon = { Icon(Icons.Default.Timer, contentDescription = "Study Timer") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.QUIZ, //highlight Quiz if selectedTab == QUIZ
            onClick = onOpenQuiz,
            icon = { Icon(Icons.Default.School, contentDescription = "Quiz") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.MOTIVATION, //highlight Motivation when it's the current tab
            onClick = onOpenMotivation,
            icon = { Icon(Icons.Default.Star, contentDescription = "Motivation") }
        )
    }
}
