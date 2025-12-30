package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.FirestoreNote
import np.ict.mad.studybuddy.core.storage.NoteCategory
import np.ict.mad.studybuddy.core.storage.NotesFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    username: String,   // UID
    noteId: Int,
    onBack: () -> Unit
) {
    val notesDb = remember { NotesFirestore() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // categories
    var categories by remember { mutableStateOf<List<NoteCategory>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<NoteCategory?>(null) }

    // dropdown UI state
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }

    LaunchedEffect(noteId) {
        categories = notesDb.getCategories(username)

        val note = notesDb.getNote(username, noteId)
        if (note != null) {
            title = note.title
            content = note.content

            // try match existing category by id/name
            selectedCategory =
                categories.firstOrNull { it.id == note.categoryId }
                    ?: categories.firstOrNull { it.name == note.categoryName }
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

            // Subject picker
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Uncategorized",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("+ Add new subject…") },
                        onClick = {
                            expanded = false
                            newCatName = ""
                            showAddDialog = true
                        }
                    )

                    HorizontalDivider()

                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                expanded = false
                                selectedCategory = cat
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

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
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            notesDb.deleteNote(username, noteId)
                            onBack()
                        }
                    }
                ) { Text("Delete") }

                Button(
                    onClick = {
                        scope.launch {
                            val cat = selectedCategory
                            val updatedNote = FirestoreNote(
                                id = noteId,
                                title = title.ifBlank { "Untitled" },
                                content = content,
                                categoryId = cat?.id ?: "",
                                categoryName = cat?.name ?: "Uncategorized"
                            )

                            notesDb.updateNote(username, updatedNote)
                            onBack()
                        }
                    }
                ) { Text("Save") }
            }
        }
    }

    // Add new subject dialog (Edit screen)
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New subject") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Subject name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newCatName.trim()
                        if (clean.isNotBlank()) {
                            scope.launch {
                                val created = notesDb.addCategory(username, clean)
                                categories = notesDb.getCategories(username)
                                selectedCategory = created
                            }
                            showAddDialog = false
                        }
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
