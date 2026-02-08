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

/**
 * Dialog that allows users to manage their note subjects (categories).
 *
 * Users can:
 * - View all subjects
 * - Rename a subject
 * - Delete a subject
 */
@Composable
fun ManageSubjectsDialog(
    categories: List<NoteCategory>, //List of existing subjects
    onDismiss: () -> Unit, //Close the dialog
    onDelete: (NoteCategory) -> Unit,  //Callback when user deletes a subject
    onRename: (NoteCategory, String) -> Unit //Callback when user renames a subject
) {

    //Tracks which subject is being confirmed for deletion
    var confirmDeleteTarget by remember { mutableStateOf<NoteCategory?>(null) }

    //Tracks which subject is being renamed
    var renameTarget by remember { mutableStateOf<NoteCategory?>(null) }

    //Holds the new name entered by the user
    var renameText by remember { mutableStateOf("") }

    /**
     * MAIN dialog:
     * Shows the list of subjects with Rename and Delete buttons
     */
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage subjects") },
        text = {
            if (categories.isEmpty()) {
                // Case: no subjects exist
                Text("No subjects yet.")
            } else {
                LazyColumn(
                    // Scrollable list of subjects
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
                                //Subject name
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            //Rename button
                            IconButton(onClick = {
                                renameTarget = cat
                                renameText = cat.name
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename subject")
                            }

                            //Delete button
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
            //Done button just closes the dialog
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )

    /**
     * RENAME dialog:
     * Opens when user clicks the Edit icon
     */
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

    /**
     * DELETE confirmation dialog:
     * Prevents accidental deletion
     */
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
