package np.ict.mad.studybuddy.feature.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

// Data class representing a group document in Firestore.
// We map Firestore fields into this Kotlin model for easier UI rendering.
data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val members: List<String> = emptyList(),
    val joinCode: String = "",
    val isDirect: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    uid: String,
    displayName: String,
    onOpenGroup: (groupId: String) -> Unit,
    onNewDirectMessage: () -> Unit,   //NEW: opens DM search screen
    onBack: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var groups by remember { mutableStateOf<List<StudyGroup>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }

    //choose action dialog (group vs individual)
    var showNewChooser by remember { mutableStateOf(false) }

    //map uid -> name for DM title rendering
    var directoryNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Loads display names for DM participants so we can show "John" instead of a raw uid.
    // Uses a cache so we don't fetch the same user repeatedly.
    suspend fun loadDirectoryNames(uids: List<String>) {
        val missing = uids.filter { it.isNotBlank() && !directoryNames.containsKey(it) }
        if (missing.isEmpty()) return

        try {
            // Fetch multiple names concurrently using async + awaitAll for better performance
            val pairs = missing.map { targetUid ->
                scope.async {
                    // 1) Try userDirectory first (fast lookup)
                    try {
                        val d = db.collection("userDirectory").document(targetUid).get().await()
                        val dn = d.getString("displayName")
                        if (!dn.isNullOrBlank()) return@async (targetUid to dn)
                    } catch (_: Exception) { }

                    // 2) Fallback to users collection if directory not available
                    try {
                        val u = db.collection("users").document(targetUid).get().await()
                        val un = u.getString("displayName") ?: "Unknown"
                        return@async (targetUid to un)
                    } catch (_: Exception) {
                        return@async (targetUid to "Unknown")
                    }
                }
            }.awaitAll()

            val m = directoryNames.toMutableMap()
            pairs.forEach { (k, v) -> m[k] = v }
            directoryNames = m
        } catch (_: Exception) {
            // ignore; UI will show fallback text below
        }
    }

    // Loads all groups where the current user is a member.
    // This updates the groups list in state, which automatically updates the UI.
    suspend fun refresh() {
        loading = true
        error = null
        try {
            val snap = db.collection("groups")
                .whereArrayContains("members", uid)
                .get()
                .await()

            // Convert Firestore documents into StudyGroup objects
            val mapped = snap.documents.map { doc ->
                StudyGroup(
                    id = doc.id,
                    name = doc.getString("name") ?: "Untitled",
                    createdBy = doc.getString("createdBy") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    members = (doc.get("members") as? List<String>) ?: emptyList(),
                    joinCode = doc.getString("joinCode") ?: "",
                    isDirect = doc.getBoolean("isDirect") ?: false
                )
            }

            groups = mapped

            // Preload names for DMs so DM rows can show the other person's name
            val dmOtherUids = mapped
                .filter { it.isDirect }
                .mapNotNull { g -> g.members.firstOrNull { it != uid } }
                .distinct()

            loadDirectoryNames(dmOtherUids)

        } catch (e: Exception) {
            error = e.message ?: "Failed to load groups"
        } finally {
            loading = false
        }
    }

    // Automatically load groups when screen opens or uid changes
    LaunchedEffect(uid) { refresh() }

    // simple random join code generator
    fun generateJoinCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // avoids confusing 0/O/1/I
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    // ensure join code is unique (simple loop)
    suspend fun generateUniqueJoinCode(): String {
        while (true) {
            val code = generateJoinCode()
            val exists = db.collection("groups")
                .whereEqualTo("joinCode", code)
                .limit(1)
                .get()
                .await()
                .documents
                .isNotEmpty()

            if (!exists) return code
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Groups") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                //Join button opens join dialog
                actions = {
                    TextButton(onClick = { showJoin = true }) { Text("Join") }
                }
            )
        },

        // Floating action button opens chooser (create group vs DM)
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewChooser = true }) {
                Icon(Icons.Default.Add, contentDescription = "New")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            //Empty state when user has not joined any groups yet
            if (groups.isEmpty() && !loading) {
                Text("No groups yet.")
                Spacer(Modifier.height(12.dp))
                Text("Tap + to create a group or start a 1-to-1 chat, or join using a join code.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(groups, key = { it.id }) { g ->

                        // If this is a direct message,
                        // find the OTHER user's UID
                        val otherUid = remember(g.id, g.members) {
                            if (g.isDirect) g.members.firstOrNull { it != uid } else null
                        }

                        // Decide what title to show on the card
                        // - Group chat → show group name
                        // - Direct message → show other user's name
                        val displayTitle = remember(g.name, g.isDirect, otherUid, directoryNames) {
                            if (g.isDirect) {
                                val n = otherUid?.let { directoryNames[it] }
                                n ?: "Chat"  //fallback if name not loaded yet
                            } else {
                                g.name
                            }
                        }

                        //Each group is displayed as a clickable card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenGroup(g.id) }, //Navigate to chat screen
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(displayTitle, style = MaterialTheme.typography.titleMedium)

                                Spacer(Modifier.height(4.dp))

                                // Show join code only for group chats
                                if (!g.isDirect) {
                                    Text(
                                        text = "Join Code: ${g.joinCode.ifBlank { "(missing)" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    // Direct messages do not have join codes
                                    Text(
                                        text = "Direct message",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog that appears when user taps the + button
    // Lets user choose between creating a group or starting a DM
    if (showNewChooser) {
        AlertDialog(
            onDismissRequest = { showNewChooser = false },
            title = { Text("Start new") },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("What would you like to create?")

                    // Option to create a new group
                    Button(
                        onClick = {
                            showNewChooser = false
                            showCreate = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create group")
                    }

                    // Option to start a 1-to-1 direct message
                    OutlinedButton(
                        onClick = {
                            showNewChooser = false
                            onNewDirectMessage()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("New message (1-to-1)")
                    }
                }
            },

            // No confirm button needed
            confirmButton = {},

            // Cancel stays at the bottom automatically
            dismissButton = {
                TextButton(onClick = { showNewChooser = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    // Dialog for joining a group using a join code
    if (showJoin) {
        var code by remember { mutableStateOf("") }
        var joinError by remember { mutableStateOf<String?>(null) }
        var joining by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!joining) showJoin = false },
            title = { Text("Join Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().trim(); joinError = null },
                        label = { Text("Join code (e.g. AB12CD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (joinError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(joinError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !joining && code.isNotBlank(),
                    onClick = {
                        scope.launch {
                            joining = true
                            joinError = null
                            try {

                                //Firestore query to find group by join code
                                val snap = db.collection("groups")
                                    .whereEqualTo("joinCode", code)
                                    .limit(1)
                                    .get()
                                    .await()

                                if (snap.documents.isEmpty()) {
                                    joinError = "Group not found."
                                } else {
                                    val doc = snap.documents.first()

                                    // prevent joining a DM by code
                                    val isDirect = doc.getBoolean("isDirect") ?: false
                                    if (isDirect) {
                                        joinError = "This join code is not for groups."
                                        return@launch
                                    }

                                    val groupId = doc.id

                                    //Safely add user to members list without duplicates
                                    db.collection("groups").document(groupId)
                                        .update("members", FieldValue.arrayUnion(uid))
                                        .await()

                                    showJoin = false
                                    refresh()
                                    onOpenGroup(groupId)
                                }
                            } catch (e: Exception) {
                                joinError = e.message ?: "Failed to join"
                            } finally {
                                joining = false
                            }
                        }
                    }
                ) { Text(if (joining) "Joining..." else "Join") }
            },
            dismissButton = {
                OutlinedButton(enabled = !joining, onClick = { showJoin = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    //Dialog for creating a new group
    if (showCreate) {
        var name by remember { mutableStateOf("") }
        var createError by remember { mutableStateOf<String?>(null) }
        var creating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!creating) showCreate = false },
            title = { Text("Create Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; createError = null },
                        label = { Text("Group name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (createError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(createError!!, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("A short join code will be generated automatically.")
                }
            },
            confirmButton = {
                Button(
                    enabled = !creating && name.isNotBlank(),
                    onClick = {
                        scope.launch {
                            creating = true
                            createError = null
                            try {

                                //Generate a short, unique join code
                                val joinCode = generateUniqueJoinCode()

                                //Data stored in Firestore for a group
                                val data = mapOf(
                                    "name" to name.trim(),
                                    "createdBy" to uid,
                                    "createdAt" to System.currentTimeMillis(),
                                    "members" to listOf(uid),
                                    "joinCode" to joinCode
                                )

                                val ref = db.collection("groups").add(data).await()

                                showCreate = false
                                refresh()
                                onOpenGroup(ref.id)
                            } catch (e: Exception) {
                                createError = e.message ?: "Failed to create group"
                            } finally {
                                creating = false
                            }
                        }
                    }
                ) { Text(if (creating) "Creating..." else "Create") }
            },
            dismissButton = {
                OutlinedButton(enabled = !creating, onClick = { showCreate = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
