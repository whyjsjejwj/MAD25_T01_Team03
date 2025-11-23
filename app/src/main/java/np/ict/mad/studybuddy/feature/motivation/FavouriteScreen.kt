package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    uid: String,
    displayName: String,
    email: String,
    onOpenHome: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit
) {
    val ctx = LocalContext.current
    val storage = remember { MotivationStorage(ctx) }
    var favourites by remember { mutableStateOf(storage.getFavourites()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favourite Quotes") }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.MOTIVATION,
                onHome = onOpenHome,
                onOpenNotes = onOpenNotes,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            if (favourites.isEmpty()) {
                Text("You have no favourite quotes yet.")
            } else {
                LazyColumn {
                    items(favourites) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    item.quote,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    item.author,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(12.dp))
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
    }
}
