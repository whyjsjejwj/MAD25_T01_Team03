package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.FirestoreNote
import np.ict.mad.studybuddy.core.storage.NoteCategory
import np.ict.mad.studybuddy.core.storage.NotesFirestore
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    username: String,       // UID
    onEdit: (Int) -> Unit,
    onOpenHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit
) {
    val notesDb = remember { NotesFirestore() }
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf<List<FirestoreNote>>(emptyList()) }

    // categories (subjects)
    var categories by remember { mutableStateOf<List<NoteCategory>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("ALL") } // ALL or categoryId

    // full-screen add note state
    var showAddNote by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<NoteCategory?>(null) }

    // manage subjects dialog
    var showManageSubjects by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        notes = notesDb.getNotes(username)
        categories = notesDb.getCategories(username)
    }

    // GUARANTEED reset: whenever Add Note opens, force Uncategorized
    LaunchedEffect(showAddNote) {
        if (showAddNote) {
            selectedCategory = null
        }
    }

    val filteredNotes = remember(notes, selectedCategoryId) {
        if (selectedCategoryId == "ALL") notes
        else notes.filter { it.categoryId == selectedCategoryId }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes Manager") }) },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newTitle = ""
                    newContent = ""
                    selectedCategory = null   // default to Uncategorized
                    showAddNote = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },

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
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text("Your Notes", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            // Subject filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryId == "ALL",
                    onClick = { selectedCategoryId = "ALL" },
                    label = { Text("All") }
                )

                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Manage Subjects UI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showManageSubjects = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manage subjects", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Rename or delete your subjects",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("Edit", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (filteredNotes.isEmpty()) {
                Text(
                    if (notes.isEmpty())
                        "You have no notes yet. Tap '+' to add one."
                    else
                        "No notes in this subject yet."
                )
            } else {
                val sorted = filteredNotes.sortedByDescending { it.createdAt }

                sorted.forEach { note ->
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
                            Text(
                                text = note.categoryName.ifBlank { "Uncategorized" },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(note.title, style = MaterialTheme.typography.titleMedium)

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

    // Full-screen Add Note (force fresh instance)
    if (showAddNote) {
        key(showAddNote) {
            AddNoteFullScreen(
                title = newTitle,
                content = newContent,
                onTitleChange = { newTitle = it },
                onContentChange = { newContent = it },

                categories = categories,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                onAddNewCategory = { name ->
                    scope.launch {
                        val created = notesDb.addCategory(username, name)
                        categories = notesDb.getCategories(username)
                        selectedCategory = created
                    }
                },

                onCancel = {
                    showAddNote = false
                    selectedCategory = null // reset for next open
                },
                onSave = {
                    scope.launch {
                        val newId = if (notes.isEmpty()) 1 else notes.maxOf { it.id } + 1
                        val cat = selectedCategory

                        notesDb.addNote(
                            username,
                            FirestoreNote(
                                id = newId,
                                title = newTitle.ifBlank { "Untitled" },
                                content = newContent,
                                categoryId = cat?.id ?: "",
                                categoryName = cat?.name ?: "Uncategorized"
                            )
                        )

                        notes = notesDb.getNotes(username)
                        categories = notesDb.getCategories(username)
                    }
                    showAddNote = false
                    selectedCategory = null // reset for next open
                }
            )
        }
    }

    // Manage Subjects dialog (delete + rename)
    if (showManageSubjects) {
        ManageSubjectsDialog(
            categories = categories,
            onDismiss = { showManageSubjects = false },
            onDelete = { cat ->
                scope.launch {
                    //save to firebase
                    notesDb.deleteCategoryAndUnassignNotes(username, cat.id)

                    categories = notesDb.getCategories(username)
                    notes = notesDb.getNotes(username)

                    if (selectedCategoryId == cat.id) selectedCategoryId = "ALL"
                }
            },
            onRename = { cat, newName ->
                val clean = newName.trim()
                if (clean.isBlank()) return@ManageSubjectsDialog

                // Optimistic UI update
                categories = categories.map { c -> if (c.id == cat.id) c.copy(name = clean) else c }
                notes = notes.map { n -> if (n.categoryId == cat.id) n.copy(categoryName = clean) else n }

                scope.launch {
                    //saving to database
                    notesDb.renameCategory(username, cat.id, clean)
                    notesDb.updateNotesCategoryName(username, cat.id, clean)

                    categories = notesDb.getCategories(username)
                    notes = notesDb.getNotes(username)
                }
            }
        )
    }
}
