package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.core.storage.MotivationFirestore
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
    val motivationDb = remember { MotivationFirestore() }

    var favourites by remember { mutableStateOf<List<MotivationItem>>(emptyList()) }

    // Load favourites from Firebase
    LaunchedEffect(uid) {
        motivationDb.getFavourites(uid) { list ->
            favourites = list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Favourite Quotes") })
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(favourites) { item ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {

                                Text(item.quote, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(item.author, style = MaterialTheme.typography.bodyMedium)

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        motivationDb.removeFavourite(uid, item) { success ->
                                            if (success) {
                                                favourites = favourites.filter {
                                                    it.quote != item.quote || it.author != item.author
                                                }
                                            }
                                        }
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
