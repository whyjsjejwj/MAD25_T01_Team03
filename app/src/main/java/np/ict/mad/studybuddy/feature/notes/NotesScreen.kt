package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.core.storage.Note
import np.ict.mad.studybuddy.core.storage.NotesStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    username: String,
    onEdit: (Int) -> Unit
) {
    val context = LocalContext.current
    val notesStorage = remember { NotesStorage(context) }

    // Notes list
    var notes by remember { mutableStateOf(listOf<Note>()) }

    // Load notes on screen start
    LaunchedEffect(true) {
        notes = notesStorage.getUserNotes(username)
    }

    // Add note dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes Manager") }) },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newTitle = ""
                    newContent = ""
                    showAddDialog = true
                }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Text(
                text = "Your Notes",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            if (notes.isEmpty()) {
                Text("You have no notes yet. Tap '+' to add one.")
            } else {
                notes.forEach { note ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                onEdit(note.id)
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }

    // ============================
    // ADD NOTE DIALOG
    // ============================
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Note") },

            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 10
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        // Save new note
                        notesStorage.addNote(
                            username = username,
                            title = newTitle.ifBlank { "Untitled" },
                            content = newContent
                        )

                        // Refresh list
                        notes = notesStorage.getUserNotes(username)

                        // Close dialog
                        showAddDialog = false
                    }
                ) {
                    Text("Add")
                }
            },

            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
