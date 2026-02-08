package np.ict.mad.studybuddy.feature.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.DirectChatManager
import np.ict.mad.studybuddy.core.storage.UserDirectoryFirestore
import np.ict.mad.studybuddy.core.storage.UserDirectoryProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageSearchScreen(
    myUid: String,
    myDisplayName: String,
    onBack: () -> Unit,
    onOpenChat: (groupId: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val directoryDb = remember { UserDirectoryFirestore() }
    val dmManager = remember { DirectChatManager() }

    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<UserDirectoryProfile>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun doSearch() {
        val q = query.trim()
        if (q.isBlank()) {
            results = emptyList()
            return
        }

        scope.launch {
            loading = true
            error = null
            try {
                //If query contains "@", treat it like an email search
                //Else treat it like a name prefix search
                results = if (q.contains("@")) {
                    directoryDb.findByEmail(q)
                } else {
                    directoryDb.findByNamePrefix(q)
                    //Remove invalid users and prevent searching yourself
                }.filter { it.uid.isNotBlank() && it.uid != myUid }
            } catch (e: Exception) {
                //Display error instead of crashing app
                error = e.message ?: "Search failed"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            //Search input box
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search by email or name") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            //Search button
            Button(
                onClick = { doSearch() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Searching..." else "Search")
            }

            //Show error if any
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            //Show "No users found" if search completed but no results
            if (!loading && results.isEmpty() && query.isNotBlank()) {
                Text("No users found.")
            }

            //Display search results in a scrollable list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results, key = { it.uid }) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()

                            //Clicking a user starts (or opens) a DM chat
                            .clickable {
                                scope.launch {

                                    //createOrOpenDirectChat returns the groupId for the DM
                                    val gid = dmManager.createOrOpenDirectChat(myUid, user.uid)
                                    onOpenChat(gid) //navigate to chat screen
                                }
                            }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            //Display name + email for the user
                            Text(user.displayName.ifBlank { "Unnamed" }, style = MaterialTheme.typography.titleMedium)
                            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
