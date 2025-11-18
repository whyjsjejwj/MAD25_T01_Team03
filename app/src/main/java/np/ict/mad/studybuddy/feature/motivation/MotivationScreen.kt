package np.ict.mad.studybuddy.feature.motivation

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MotivationScreen(
    onOpenFavourites: () -> Unit
) {
    val ctx = LocalContext.current
    val storage = remember { MotivationStorage(ctx) }

    // Temporary: Static daily quote (you can randomize later)
    val dailyQuote = "Success is the sum of small efforts repeated daily."
    val dailyAuthor = "— Anonymous"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = dailyQuote,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = dailyAuthor,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                storage.saveFavourite(
                    MotivationItem(
                        quote = dailyQuote,
                        author = dailyAuthor
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save to Favourites")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenFavourites,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Favourites")
        }
    }
}
