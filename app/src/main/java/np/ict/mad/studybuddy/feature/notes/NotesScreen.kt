package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.FirestoreNote
import np.ict.mad.studybuddy.core.storage.NotesFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    username: String,   // now actually Firebase UID
    onEdit: (Int) -> Unit
) {
    val notesDb = remember { NotesFirestore() }
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf<List<FirestoreNote>>(emptyList()) }

    // Load notes when screen is first shown, or when username changes
    LaunchedEffect(username) {
        notes = notesDb.getNotes(username)
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
                .padding(24.dp)
                .fillMaxSize()
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
                            .clickable { onEdit(note.id) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }

    // Add note dialog
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
                        scope.launch {
                            val newId = if (notes.isEmpty()) 1 else notes.maxOf { it.id } + 1

                            val newNote = FirestoreNote(
                                id = newId,
                                title = newTitle.ifBlank { "Untitled" },
                                content = newContent
                            )

                            notesDb.addNote(username, newNote)
                            notes = notesDb.getNotes(username)  // refresh
                        }
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
