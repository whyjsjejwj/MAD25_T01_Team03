package np.ict.mad.studybuddy.feature.groups

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import np.ict.mad.studybuddy.core.storage.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    groupId: String,
    uid: String,
    displayName: String,
    onBack: () -> Unit,
    notesDb: NotesFirestore = NotesFirestore()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val groupsDb = remember { GroupsFirestore() }

    // --- Group info ---
    var groupName by remember { mutableStateOf("Group Chat") }
    var ownerUid by remember { mutableStateOf("") }
    var members by remember { mutableStateOf<List<String>>(emptyList()) }
    var clearedAt by remember { mutableLongStateOf(0L) }

    // DM mode
    var isDirect by remember { mutableStateOf(false) }
    var directOtherName by remember { mutableStateOf<String?>(null) }

    // Members display names
    var memberNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loadingMembers by remember { mutableStateOf(false) }

    // Messages
    var messages by remember { mutableStateOf<List<GroupMessage>>(emptyList()) }

    // Input + UI states
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Dialogs / sheets
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var showMembers by remember { mutableStateOf(false) }

    // Note picker
    var showPicker by remember { mutableStateOf(false) }
    var myNotes by remember { mutableStateOf<List<FirestoreNote>>(emptyList()) }
    var loadingNotes by remember { mutableStateOf(false) }

    val isOwner = uid == ownerUid

    // ---------- helper functions ----------

    // Open PDF using whatever app can handle the link
    fun openPdf(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            // ignore
        }
    }

    // Load display names (simple loop)
    fun loadMemberNamesIfNeeded(uids: List<String>) {
        val missing = uids.filter { it.isNotBlank() && !memberNames.containsKey(it) }
        if (missing.isEmpty()) return

        scope.launch {
            loadingMembers = true
            try {
                val updated = memberNames.toMutableMap()
                for (mUid in missing) {
                    val name = try {
                        val doc = db.collection("users").document(mUid).get().await()
                        doc.getString("displayName") ?: "Unknown"
                    } catch (_: Exception) {
                        "Unknown"
                    }
                    updated[mUid] = name
                }
                memberNames = updated
            } finally {
                loadingMembers = false
            }
        }
    }

    // Load DM other person's name
    fun loadDirectOtherName(otherUid: String) {
        scope.launch {
            directOtherName = try {
                val dirDoc = db.collection("userDirectory").document(otherUid).get().await()
                val name1 = dirDoc.getString("displayName")
                if (!name1.isNullOrBlank()) name1
                else {
                    val userDoc = db.collection("users").document(otherUid).get().await()
                    userDoc.getString("displayName") ?: "Unknown"
                }
            } catch (_: Exception) {
                "Unknown"
            }
        }
    }

    // ---------- Firestore listeners ----------

    // Listen to group doc changes
    DisposableEffect(groupId) {
        val reg = db.collection("groups").document(groupId)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null || !snap.exists()) return@addSnapshotListener

                isDirect = snap.getBoolean("isDirect") ?: false
                groupName = snap.getString("name") ?: "Group Chat"
                ownerUid = snap.getString("createdBy") ?: ""

                val m = (snap.get("members") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                members = m

                clearedAt = snap.getLong("clearedAt") ?: 0L

                loadMemberNamesIfNeeded(m)

                if (isDirect) {
                    val other = m.firstOrNull { it != uid }
                    if (other != null) loadDirectOtherName(other) else directOtherName = "Unknown"
                } else {
                    directOtherName = null
                }
            }

        onDispose { reg.remove() }
    }

    // Listen to messages
    DisposableEffect(groupId) {
        val reg = db.collection("groups")
            .document(groupId)
            .collection("messages")
            .orderBy("createdAt")
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) return@addSnapshotListener
                messages = snap.toObjects(GroupMessage::class.java)
            }

        onDispose { reg.remove() }
    }

    val visibleMessages = remember(messages, clearedAt) {
        messages.filter { it.createdAt > clearedAt }
    }

    val topTitle = remember(isDirect, directOtherName, groupName) {
        if (isDirect) (directOtherName ?: "Chat") else groupName
    }

    // ---------- UI ----------

    Scaffold(
        topBar = {
            GroupChatTopBar(
                title = topTitle,
                isDirect = isDirect,
                isOwner = isOwner,
                onBack = onBack,
                onMembers = { showMembers = true },
                onEdit = {
                    newGroupName = groupName
                    showEditName = true
                },
                onClear = { showClearConfirm = true },
                onDelete = { showDeleteConfirm = true }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Message list (fills screen, with padding at bottom so it won't hide behind input bar)
            MessageListNoWeight(
                messages = visibleMessages,
                myUid = uid,
                ownerUid = ownerUid,
                isDirect = isDirect,
                onOpenPdf = { openPdf(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 78.dp) // space for input bar
            )

            // Input bar pinned to bottom
            InputBarNoWeight(
                input = input,
                sending = sending,
                onInputChange = { input = it },
                onAttach = {
                    scope.launch {
                        loadingNotes = true
                        error = null
                        try {
                            myNotes = notesDb.getNotes(uid)
                            showPicker = true
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to load notes"
                        } finally {
                            loadingNotes = false
                        }
                    }
                },
                onSend = {
                    scope.launch {
                        sending = true
                        error = null
                        try {
                            groupsDb.sendTextMessage(groupId, uid, displayName, input.trim())
                            input = ""
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to send"
                        } finally {
                            sending = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // Error message at top (if any)
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                )
            }
        }
    }

    // ---------- dialogs & sheets ----------

    ClearChatDialog(
        show = showClearConfirm && !isDirect,
        onDismiss = { showClearConfirm = false },
        onConfirm = {
            scope.launch {
                try {
                    val now = System.currentTimeMillis()
                    db.collection("groups").document(groupId)
                        .update("clearedAt", now)
                        .await()
                    clearedAt = now
                    showClearConfirm = false
                } catch (e: Exception) {
                    error = e.message ?: "Failed to clear chat"
                }
            }
        }
    )

    DeleteGroupDialog(
        show = showDeleteConfirm && !isDirect,
        onDismiss = { showDeleteConfirm = false },
        onConfirm = {
            scope.launch {
                try {
                    db.collection("groups").document(groupId).delete().await()
                    showDeleteConfirm = false
                    onBack()
                } catch (e: Exception) {
                    error = e.message ?: "Failed to delete group"
                }
            }
        }
    )

    EditGroupNameDialog(
        show = showEditName && !isDirect,
        newName = newGroupName,
        onNameChange = { newGroupName = it },
        onDismiss = { showEditName = false },
        onSave = {
            scope.launch {
                try {
                    db.collection("groups").document(groupId)
                        .update("name", newGroupName.trim())
                        .await()
                    showEditName = false
                } catch (e: Exception) {
                    error = e.message ?: "Failed to update name"
                }
            }
        }
    )

    MembersSheetNoWeight(
        show = showMembers && !isDirect,
        members = members,
        ownerUid = ownerUid,
        isOwner = isOwner,
        loadingMembers = loadingMembers,
        memberNames = memberNames,
        onDismiss = { showMembers = false },
        onRemove = { memberUid ->
            scope.launch {
                try {
                    val updated = members - memberUid
                    db.collection("groups").document(groupId)
                        .update("members", updated)
                        .await()
                } catch (e: Exception) {
                    error = e.message ?: "Failed to remove member"
                }
            }
        }
    )

    NotePickerDialog(
        show = showPicker,
        sending = sending,
        loadingNotes = loadingNotes,
        notes = myNotes,
        onDismiss = { if (!sending) showPicker = false },
        onPickNote = { note ->
            scope.launch {
                sending = true
                error = null
                try {
                    val pdfName = PdfUtils.safePdfName(note.title)
                    val pdfFile = PdfUtils.noteToPdfFile(
                        context = context,
                        title = note.title,
                        content = note.content,
                        fileName = pdfName
                    )

                    val (url, size) = groupsDb.uploadGroupPdf(
                        groupId = groupId,
                        uid = uid,
                        fileUri = pdfFile.toUri(),
                        fileName = pdfFile.name
                    )

                    groupsDb.sendFileMessage(
                        groupId = groupId,
                        senderUid = uid,
                        senderName = displayName,
                        fileName = pdfFile.name,
                        fileUrl = url,
                        fileSize = size
                    )

                    showPicker = false
                } catch (e: Exception) {
                    error = e.message ?: "Failed to send PDF"
                } finally {
                    sending = false
                }
            }
        }
    )
}

/* ---------------- Smaller UI parts below ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupChatTopBar(
    title: String,
    isDirect: Boolean,
    isOwner: Boolean,
    onBack: () -> Unit,
    onMembers: () -> Unit,
    onEdit: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (!isDirect) {
                TextButton(onClick = onMembers) { Text("Members") }
                if (isOwner) {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onClear) { Text("Clear") }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                }
            }
        }
    )
}

@Composable
private fun MessageListNoWeight(
    messages: List<GroupMessage>,
    myUid: String,
    ownerUid: String,
    isDirect: Boolean,
    onOpenPdf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        items(messages) { msg ->
            MessageCard(
                msg = msg,
                myUid = myUid,
                isOwnerSender = (!isDirect && msg.senderUid == ownerUid),
                onOpenPdf = { onOpenPdf(msg.fileUrl) }
            )
        }
    }
}

@Composable
private fun InputBarNoWeight(
    input: String,
    sending: Boolean,
    onInputChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier.padding(12.dp)
    ) {
        IconButton(
            onClick = onAttach,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach")
        }

        IconButton(
            enabled = input.isNotBlank() && !sending,
            onClick = onSend,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }

        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            placeholder = { Text("Message...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp, end = 52.dp) // leave space for buttons
        )
    }
}

/* ---------------- Dialogs / Sheets ---------------- */

@Composable
private fun ClearChatDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear chat") },
        text = { Text("This clears the chat for everyone. This cannot be undone.") },
        confirmButton = { Button(onClick = onConfirm) { Text("Clear") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeleteGroupDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete group") },
        text = { Text("This will permanently delete the group and all messages. This cannot be undone.") },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = onConfirm
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditGroupNameDialog(
    show: Boolean,
    newName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit group name") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = onNameChange,
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(enabled = newName.trim().isNotBlank(), onClick = onSave) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembersSheetNoWeight(
    show: Boolean,
    members: List<String>,
    ownerUid: String,
    isOwner: Boolean,
    loadingMembers: Boolean,
    memberNames: Map<String, String>,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit
) {
    if (!show) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Group Members",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            if (loadingMembers) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(members) { memberUid ->
                    val name = memberNames[memberUid] ?: "Loading..."
                    val isRowOwner = memberUid == ownerUid

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(end = 90.dp) // leave room for Remove button
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (isRowOwner) {
                                        Spacer(Modifier.width(8.dp))
                                        Text("👑")
                                    }
                                }
                                Text(
                                    text = if (isRowOwner) "Owner" else "Member",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isOwner && !isRowOwner) {
                                TextButton(
                                    onClick = { onRemove(memberUid) },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Text("Remove", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(modifier = Modifier.fillMaxWidth(), onClick = onDismiss) { Text("Close") }
        }
    }
}

@Composable
private fun NotePickerDialog(
    show: Boolean,
    sending: Boolean,
    loadingNotes: Boolean,
    notes: List<FirestoreNote>,
    onDismiss: () -> Unit,
    onPickNote: (FirestoreNote) -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send note as PDF") },
        text = {
            when {
                loadingNotes -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                notes.isEmpty() -> Text("You have no notes yet.")
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notes.take(8).forEach { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !sending) { onPickNote(note) },
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(note.title.ifBlank { "Untitled" })
                                    Spacer(Modifier.height(4.dp))
                                    Text(note.content, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = !sending, onClick = onDismiss) { Text("Close") }
        }
    )
}

// Message bubble
@Composable
private fun MessageCard(
    msg: GroupMessage,
    myUid: String,
    isOwnerSender: Boolean,
    onOpenPdf: () -> Unit
) {
    val isMine = msg.senderUid == myUid

    val myBubble = MaterialTheme.colorScheme.primaryContainer
    val myText = MaterialTheme.colorScheme.onPrimaryContainer

    val otherBubble = MaterialTheme.colorScheme.surfaceVariant
    val otherText = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMine) myBubble else otherBubble,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 6.dp,
                bottomEnd = if (isMine) 6.dp else 16.dp
            ),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .widthIn(max = 320.dp)
            ) {
                if (!isMine) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            msg.senderName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = otherText
                        )
                        if (isOwnerSender) {
                            Spacer(Modifier.width(6.dp))
                            Text("👑", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                if (msg.type == "file") {
                    Text("📄 ${msg.fileName}", color = if (isMine) myText else otherText)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onOpenPdf) { Text("Open PDF") }
                } else {
                    Text(msg.text, color = if (isMine) myText else otherText)
                }
            }
        }
    }
}
