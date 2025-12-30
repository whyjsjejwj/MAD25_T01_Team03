package np.ict.mad.studybuddy.feature.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.core.storage.NoteCategory

@Composable
fun ManageSubjectsDialog(
    categories: List<NoteCategory>,
    onDismiss: () -> Unit,
    onDelete: (NoteCategory) -> Unit,
    onRename: (NoteCategory, String) -> Unit
) {
    var confirmDeleteTarget by remember { mutableStateOf<NoteCategory?>(null) }
    var renameTarget by remember { mutableStateOf<NoteCategory?>(null) }
    var renameText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage subjects") },
        text = {
            if (categories.isEmpty()) {
                Text("No subjects yet.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = {
                                renameTarget = cat
                                renameText = cat.name
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename subject")
                            }

                            IconButton(onClick = { confirmDeleteTarget = cat }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete subject")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )

    // Rename dialog
    val rt = renameTarget
    if (rt != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename subject") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = renameText.trim()
                        if (clean.isNotBlank()) {
                            onRename(rt, clean)
                            renameTarget = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Delete confirm dialog
    val dt = confirmDeleteTarget
    if (dt != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteTarget = null },
            title = { Text("Delete subject?") },
            text = {
                Text(
                    "“${dt.name}” will be deleted.\n" +
                            "Notes in this subject will be moved to Uncategorized."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(dt)
                        confirmDeleteTarget = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}
