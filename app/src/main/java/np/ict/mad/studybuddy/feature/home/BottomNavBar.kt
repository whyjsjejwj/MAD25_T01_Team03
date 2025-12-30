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
    selectedTab: BottomNavTab,
    onHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.HOME,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.TIMER,
            onClick = onOpenTimer,
            icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
            label = { Text("Timer") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.QUIZ,
            onClick = onOpenQuiz,
            icon = { Icon(Icons.Default.School, contentDescription = "Quiz") },
            label = { Text("Quiz") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.MOTIVATION,
            onClick = onOpenMotivation,
            icon = { Icon(Icons.Default.Star, contentDescription = "Motivation") },
            label = { Text("Motivation") }
        )
    }
}

