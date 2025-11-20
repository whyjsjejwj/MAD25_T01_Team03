package np.ict.mad.studybuddy.feature.home

import np.ict.mad.studybuddy.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import np.ict.mad.studybuddy.core.storage.Note
import np.ict.mad.studybuddy.core.storage.NotesStorage
import androidx.compose.material.icons.filled.School

@Composable
fun HomeScreen(
    username: String,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit,
    onLogout: () -> Unit
) {
    val displayName = if (username.isBlank()) "Student" else username

    // ===== FETCH NOTES FOR HOME =====
    val context = LocalContext.current
    val notesStorage = remember { NotesStorage(context) }

    var notes by remember { mutableStateOf(listOf<Note>()) }

    LaunchedEffect(username) {
        notes = notesStorage.getUserNotes(username)
    }

    val latestNote = notes.maxByOrNull { it.id }

    Scaffold(
        bottomBar = {
            BottomNavBar(
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
            TopBar(onLogout = onLogout)

            Spacer(Modifier.height(16.dp))

            // ---------- WELCOME TEXT ----------
            Text(
                text = "Welcome back, $displayName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFA000)
            )

            Spacer(Modifier.height(16.dp))

            // ---------- 2 CARDS ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Study Streak card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(Color(0xFFFFF8E1)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Study Streak", color = Color.Gray)
                        Text("0 Days", fontWeight = FontWeight.Bold)
                    }
                }

                // Where you left off card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(Color(0xFFFFF8E1)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Where you left off", color = Color.Gray)

                        if (latestNote == null) {
                            Text("—", color = Color.DarkGray)
                        } else {
                            Text(latestNote.title, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- NOTES PREVIEW CARD ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color(0xFFFFF9C4)),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Your Latest Note",
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    if (latestNote == null) {
                        Text(
                            "No notes yet. Tap the Notes tab to create one.",
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            latestNote.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            latestNote.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            color = Color.DarkGray
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

// ================================================================
// TOP BAR
// ================================================================
@Composable
fun TopBar(onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(id = R.drawable.studybuddylogo_cutout),
            contentDescription = "StudyBuddy Logo",
            modifier = Modifier.size(100.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = { /* Search later */ }) {
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
                        text = { Text("Logout") },
                        onClick = {
                            showMenu = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

// ================================================================
// BOTTOM NAV BAR
// ================================================================
@Composable
fun BottomNavBar(
    onHome: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {

        NavigationBarItem(
            selected = true,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onOpenNotes,
            icon = { Icon(Icons.Default.Edit, contentDescription = "Notes") }
        )

        // QUIZ ICON (NOT CONNECTED)
        NavigationBarItem(
            selected = false,
            onClick = { /* Not implemented*/ },
            icon = { Icon(Icons.Filled.School, contentDescription = "Quiz") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onOpenMotivation,
            icon = { Icon(Icons.Default.Star, contentDescription = "Motivation") }
        )
    }
}


