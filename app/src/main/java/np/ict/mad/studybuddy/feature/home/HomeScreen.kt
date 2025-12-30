package np.ict.mad.studybuddy.feature.home

import np.ict.mad.studybuddy.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.FirestoreNote
import np.ict.mad.studybuddy.core.storage.NoteCategory
import np.ict.mad.studybuddy.core.storage.NotesFirestore
import np.ict.mad.studybuddy.feature.notes.AddNoteFullScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavController,
    uid: String,
    displayName: String,
    email: String,
    onOpenProfile: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit,
    onLogout: () -> Unit
) {
    val notesDb = remember { NotesFirestore() }
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf<List<FirestoreNote>>(emptyList()) }

    // categories (subjects)
    var categories by remember { mutableStateOf<List<NoteCategory>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<NoteCategory?>(null) }

    // Full-screen Add Note state
    var showAddNote by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        notes = notesDb.getNotes(uid)
        categories = notesDb.getCategories(uid)

        // ✅ Do NOT auto-default to first category.
        // Leaving selectedCategory = null means "Uncategorized".
        selectedCategory = null
    }

    // ✅ GUARANTEED reset whenever Add Note opens
    LaunchedEffect(showAddNote) {
        if (showAddNote) {
            selectedCategory = null
        }
    }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                onClose = { scope.launch { drawerState.close() } },

                onOpenNotes = {
                    scope.launch { drawerState.close() }
                    nav.navigate("notes/$uid/$displayName/$email")
                },

                onOpenGroups = {
                    scope.launch { drawerState.close() }
                    nav.navigate("groups/$uid/$displayName/$email")
                },

                onOpenTimer = {
                    scope.launch { drawerState.close() }
                    onOpenTimer()
                },
                onOpenQuiz = {
                    scope.launch { drawerState.close() }
                    onOpenQuiz()
                },
                onOpenMotivation = {
                    scope.launch { drawerState.close() }
                    onOpenMotivation()
                },
                onOpenProfile = {
                    scope.launch { drawerState.close() }
                    onOpenProfile()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavBar(
                    selectedTab = BottomNavTab.HOME,
                    onHome = {},
                    onOpenQuiz = onOpenQuiz,
                    onOpenTimer = onOpenTimer,
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

                HomeTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Welcome back, $displayName!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Notes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { nav.navigate("notes/$uid/$displayName/$email") }
                        ) { Text("View All") }

                        IconButton(
                            onClick = {
                                newTitle = ""
                                newContent = ""
                                selectedCategory = null   // ✅ default to Uncategorized
                                showAddNote = true
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Note")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (notes.isEmpty()) {
                    Text(
                        "No notes yet. Tap + to create one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val previewNotes = notes.takeLast(4).reversed()

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        previewNotes.forEach { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { nav.navigate("editNote/$uid/${note.id}") },
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    Text(
                                        text = note.categoryName.ifBlank { "Uncategorized" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        note.content,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full-screen Add Note with subject picker
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
                            val created = notesDb.addCategory(uid, name)
                            categories = notesDb.getCategories(uid)
                            selectedCategory = created
                        }
                    },

                    onCancel = {
                        showAddNote = false
                        selectedCategory = null
                    },
                    onSave = {
                        scope.launch {
                            val newId = if (notes.isEmpty()) 1 else notes.maxOf { it.id } + 1
                            val cat = selectedCategory

                            notesDb.addNote(
                                uid,
                                FirestoreNote(
                                    id = newId,
                                    title = newTitle.ifBlank { "Untitled" },
                                    content = newContent,
                                    categoryId = cat?.id ?: "",
                                    categoryName = cat?.name ?: "Uncategorized"
                                )
                            )

                            notes = notesDb.getNotes(uid)
                            categories = notesDb.getCategories(uid)
                        }
                        showAddNote = false
                        selectedCategory = null
                    }
                )
            }
        }
    }
}

@Composable
fun HomeTopBar(
    onOpenDrawer: () -> Unit,
    onSearch: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenDrawer) {
            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
        }

        Spacer(Modifier.width(8.dp))

        Image(
            painterResource(id = R.drawable.studybuddylogo_cropped),
            contentDescription = "StudyBuddy Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
}

@Composable
fun AppDrawerContent(
    onClose: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Quick Navigation",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            DrawerItem("Notes", Icons.Default.EditNote, onOpenNotes)
            DrawerItem("Study Groups", Icons.Default.Chat, onOpenGroups)
            DrawerItem("Timer", Icons.Default.Timer, onOpenTimer)
            DrawerItem("Quiz", Icons.Default.School, onOpenQuiz)
            DrawerItem("Motivation", Icons.Default.Star, onOpenMotivation)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            DrawerItem("Profile", Icons.Default.Person, onOpenProfile)

            if (onLogout != null) {
                Spacer(Modifier.height(6.dp))
                DrawerItem(
                    title = "Logout",
                    icon = Icons.Default.Logout,
                    onClick = onLogout,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = LocalContentColor.current
) {
    NavigationDrawerItem(
        label = { Text(title) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null, tint = tint) },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
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
