package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun FavouriteScreen() {
    val ctx = LocalContext.current
    val storage = remember { MotivationStorage(ctx) }

    var favourites by remember { mutableStateOf(storage.getFavourites()) }

    Column(Modifier.padding(16.dp)) {
        Text("Your Favourite Quotes", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(favourites) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.quote, style = MaterialTheme.typography.bodyLarge)
                        Text(item.author, style = MaterialTheme.typography.bodyMedium)

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                storage.removeFavourite(item)
                                favourites = storage.getFavourites()
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}
