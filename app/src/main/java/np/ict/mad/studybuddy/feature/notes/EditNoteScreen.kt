package np.ict.mad.studybuddy.feature.notes

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
fun EditNoteScreen(
    username: String,   // now UID
    noteId: Int,
    onBack: () -> Unit
) {
    val notesDb = remember { NotesFirestore() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // Load note data once when screen opens
    LaunchedEffect(noteId) {
        val note = notesDb.getNote(username, noteId)
        if (note != null) {
            title = note.title
            content = note.content
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Note") }) }
    ) { innerPadding ->

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
                    .height(220.dp),
                maxLines = 10
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // DELETE
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            notesDb.deleteNote(username, noteId)
                            onBack()   // return to NotesScreen
                        }
                    }
                ) {
                    Text("Delete")
                }

                // SAVE
                Button(
                    onClick = {
                        scope.launch {
                            val updatedNote = FirestoreNote(
                                id = noteId,
                                title = title.ifBlank { "Untitled" },
                                content = content
                            )

                            notesDb.updateNote(username, updatedNote)
                            onBack()  // return to NotesScreen
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
