package np.ict.mad.studybuddy.feature.home

import np.ict.mad.studybuddy.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.core.storage.FirestoreNote
import np.ict.mad.studybuddy.core.storage.NotesFirestore

@Composable
fun HomeScreen(
    uid: String,
    displayName: String,
    email: String,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val notesDb = remember { NotesFirestore() }
    var notes by remember { mutableStateOf<List<FirestoreNote>>(emptyList()) }

    LaunchedEffect(uid) {
        notes = notesDb.getNotes(uid)
    }

    val latestNote = notes.lastOrNull()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.HOME,
                onHome = {},
                onOpenNotes = onOpenNotes,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // ---------- TOP BAR ----------
            TopBar(
                displayName = displayName,
                email = email,
                onOpenProfile = onOpenProfile
            )

            Spacer(Modifier.height(16.dp))

            // ---------- WELCOME ----------
            Text(
                text = "Welcome back, $displayName!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // ---------- TWO SUMMARY CARDS ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Study Streak
                SummaryCard(
                    title = "Study Streak",
                    subtitle = "0 Days",
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Total Notes",
                    subtitle = "${notes.size} Notes",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------- LATEST NOTE CARD ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Your Latest Note",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    if (latestNote == null) {
                        Text(
                            "No notes yet. Tap the Notes tab to create one.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            latestNote.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            latestNote.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = onOpenNotes,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("View All")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ================================================================
// TOP BAR
// ================================================================
@Composable
fun TopBar(
    displayName: String,
    email: String,
    onOpenProfile: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(id = R.drawable.studybuddylogo_cropped),
            contentDescription = "StudyBuddy Logo",
            modifier = Modifier.size(100.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile") },
                        onClick = {
                            showMenu = false
                            onOpenProfile()
                        }
                    )
                }
            }
        }
    }
}
