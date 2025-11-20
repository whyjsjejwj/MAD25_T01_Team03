package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import np.ict.mad.studybuddy.core.storage.NotesStorage
import np.ict.mad.studybuddy.core.storage.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    username: String,
    noteId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notesStorage = remember { NotesStorage(context) }

    var note by remember { mutableStateOf<Note?>(null) }

    // Load specific note
    LaunchedEffect(true) {
        val userNotes = notesStorage.getUserNotes(username)
        note = userNotes.find { it.id == noteId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Note") }
            )
        }
    ) { innerPadding ->

        note?.let { currentNote ->

            var title by remember { mutableStateOf(currentNote.title) }
            var content by remember { mutableStateOf(currentNote.content) }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(24.dp)
                    .fillMaxSize()
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    maxLines = Int.MAX_VALUE
                )

                Spacer(Modifier.height(20.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        notesStorage.updateNote(
                            username = username,
                            noteId = noteId,
                            newTitle = title,
                            newContent = content
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }

                Spacer(Modifier.height(10.dp))

                // DELETE BUTTON
                OutlinedButton(
                    onClick = {
                        notesStorage.deleteNote(username, noteId)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        } ?: run {
            // If note not found
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                Text("Note not found.")
            }
        }
    }
}
