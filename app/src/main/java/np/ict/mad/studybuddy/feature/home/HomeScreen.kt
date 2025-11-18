package np.ict.mad.studybuddy.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    username: String,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, $username!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Notes button
        Button(
            onClick = onOpenNotes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Notes Manager")
        }

        Spacer(Modifier.height(12.dp))

        // Motivation Hub button
        Button(
            onClick = onOpenMotivation,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Motivation Hub")
        }

        Spacer(Modifier.height(16.dp))

        // Logout button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
    }
}
