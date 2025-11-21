package np.ict.mad.studybuddy.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class BottomNavTab { HOME, NOTES, QUIZ, MOTIVATION }

@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab,
    onHome: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.HOME,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.NOTES,
            onClick = onOpenNotes,
            icon = { Icon(Icons.Default.Edit, contentDescription = "Notes") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.QUIZ,
            onClick = { /* Not implemented */ },
            icon = { Icon(Icons.Default.School, contentDescription = "Quiz") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomNavTab.MOTIVATION,
            onClick = onOpenMotivation,
            icon = { Icon(Icons.Default.Star, contentDescription = "Motivation") }
        )
    }
}
