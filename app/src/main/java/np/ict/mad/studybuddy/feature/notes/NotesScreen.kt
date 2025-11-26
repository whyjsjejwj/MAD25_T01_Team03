package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.FirestoreNote
import np.ict.mad.studybuddy.core.storage.NotesFirestore
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    username: String,       // Firebase UID
    onEdit: (Int) -> Unit,
    onOpenHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz : () -> Unit,
    onOpenMotivation: () -> Unit
) {

    // My Firestore helper class to load and save notes
    val notesDb = remember { NotesFirestore() }

    // I use this scope to run Firestore operations in the background (e.g. add/update notes).
    // 'rememberCoroutineScope' makes sure the coroutine follows the NotesScreen lifecycle.
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf<List<FirestoreNote>>(emptyList()) }

    // Load notes when screen shows
    LaunchedEffect(username) {
        notes = notesDb.getNotes(username)
    }

    // for add new note
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember  { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes Manager") }) },

        // Floating "+" button. When clicked, I reset the input fields and open the Add Note dialog.
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newTitle = ""
                    newContent = ""
                    showAddDialog = true
                }
            ) { Text("+") }
        },

        // Bottom navigation to switch between main features
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.HOME,
                onHome = onOpenHome,
                onOpenQuiz = onOpenQuiz,
                onOpenTimer = onOpenTimer,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
        ) {

            Text("Your Notes", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            if (notes.isEmpty()) {
                Text("You have no notes yet. Tap '+' to add one.")
            } else {

                // Display each note inside a clickable Card
                notes.forEach { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onEdit(note.id) }, // Go to EditNoteScreen
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Note title
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))

                            // Content for notes preview (max 2 lines)
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

    // popup dialog for adding a new note
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Note") },
            text = {
                Column {
                    // Title input field
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Content input field
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Content") },
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },

            // Confirm button: save note to Firestore
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {

                            // Auto-generate ID if no notes yet, use 1, else take max ID + 1
                            val newId = if (notes.isEmpty()) 1 else notes.maxOf { it.id } + 1

                            // Create note object
                            notesDb.addNote(
                                username,
                                FirestoreNote(
                                    id = newId,
                                    title = newTitle.ifBlank { "Untitled" },
                                    content = newContent
                                )
                            )

                            // Refresh notes after adding
                            notes = notesDb.getNotes(username)
                        }

                        // Close dialog
                        showAddDialog = false
                    }
                ) {
                    Text("Add")
                }
            },

            // Cancel button just closes the dialog
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
