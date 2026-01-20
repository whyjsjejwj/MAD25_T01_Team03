package np.ict.mad.studybuddy.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import np.ict.mad.studybuddy.feature.auth.LoginPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    displayName: String,
    email: String,
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onChangeEducationLevel: () -> Unit = {}
) {
    val context = LocalContext.current

    // using my LoginPreferences to read theme & maybe future profile data
    val prefs = remember { LoginPreferences(context) }

    // coroutine scope tied to this screen (so it cancels when I leave)
    val scope = rememberCoroutineScope()

    val firestore = remember { FirebaseFirestore.getInstance() } // to retrieve the user profile data

    // reading the saved theme from DataStore (Flow -> UI updates automatically)
    val currentTheme by prefs.theme.collectAsState(initial = "system")
    var showThemeDialog by remember { mutableStateOf(false) }

    var educationLevel by remember { mutableStateOf("Loading...") }
    var eduLoading by remember { mutableStateOf(true) }

    var showEduDialog by remember { mutableStateOf(false) }
    var eduSaving by remember {mutableStateOf(false)}
    var eduError by remember {mutableStateOf<String?>(null)}

    // education levels the users can change to
    val eduOptions = listOf(
        "Unknown",
        "Primary 3",
        "Primary 4",
        "Primary 5",
        "Primary 6",
        "Secondary 1",
        "Secondary 2",
        "Secondary 3",
        "Secondary 4",
        "JC 1",
        "JC 2",
        "Poly",
        "University"
    )

    LaunchedEffect(uid) {
        eduLoading = true
        try {
            val users = firestore.collection("users")
            val uidRef = users.document(uid)
            val uidDoc = uidRef.get().await()

            val existingEmail = uidDoc.getString("email").orEmpty()
            val existingName = uidDoc.getString("displayName").orEmpty()
            val existingEdu = uidDoc.getString("educationLevel")

            val safeEdu = existingEdu?.takeIf { it.isNotBlank() } ?: "Unknown"
            educationLevel = safeEdu

            uidRef.set(
                mapOf(
                    "email" to email.trim(),
                    "displayName" to displayName.trim(),
                    "educationLevel" to safeEdu
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            // If something fails, still show a safe default
            educationLevel = "Unknown"
        } finally {
            eduLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Avatar initials from displayName (e.g. "John Doe" -> "JD")
            val initials = displayName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .take(2)
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(displayName, style = MaterialTheme.typography.headlineSmall)
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(32.dp))

            if (eduLoading) {
                Text("Education Level: Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(
                    "Education Level: $educationLevel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(28.dp))

            // Account Section
            SettingsSectionTitle("Account")

            ProfileOption(
                icon = Icons.Default.School,
                text = if (eduLoading) "Education Level (Loading...)" else "Education Level ($educationLevel)",
                onClick = {
                    if (!eduLoading) {
                        eduError = null
                        showEduDialog = true
                    }
                }
            )

            ProfileOption(
                icon = Icons.Default.Edit,
                text = "Edit Profile",
                onClick = onEdit
            )

            ProfileOption(
                icon = Icons.Default.Lock,
                text = "Change Password",
                onClick = onChangePassword
            )

            Spacer(Modifier.height(32.dp))

            // App settings
            SettingsSectionTitle("App Settings")

            ProfileOption(
                icon = Icons.Default.Palette,
                text = "Theme ($currentTheme)",
                onClick = { showThemeDialog = true }
            )

            ProfileOption(
                icon = Icons.Default.Settings,
                text = "General Settings",
                onClick = {} // not implemented yet
            )
        }
    }

    // Education Level dialog
    if (showEduDialog) {
        var tempSelection by remember(educationLevel) { mutableStateOf(educationLevel) }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!eduSaving) showEduDialog = false
            },
            title = { Text("Set Education Level") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Text(
                        "This will be used to filter quiz questions based on your level.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = tempSelection,
                            onValueChange = {},
                            label = { Text("Education Level") }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            eduOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        tempSelection = opt
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (eduError != null) {
                        Text(eduError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !eduSaving,
                    onClick = {
                        eduSaving = true
                        eduError = null

                        // debug
                        //android.util.Log.d("EduLevel", "Saving to users/$uid as $tempSelection (email=$email, name=$displayName)")

                        firestore.collection("users")
                            .document(uid)
                            .set(
                                mapOf(
                                    "educationLevel" to tempSelection,
                                    "email" to email.trim(),
                                    "displayName" to displayName.trim()
                                ),
                                SetOptions.merge()
                            )
                            .addOnSuccessListener {
                                educationLevel = tempSelection
                                eduSaving = false
                                showEduDialog = false

                                // debug
                                android.util.Log.d("EduLevel", "Saved OK. Now reading back users/$uid ...")
                                firestore.collection("users").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        android.util.Log.d("EduLevel", "ReadBack users/$uid => ${doc.data}")
                                    }
                            }
                            .addOnFailureListener { e ->
                                eduSaving = false
                                eduError = e.message ?: "Failed to save education level."
                            }
                    }
                ) {
                    Text(if (eduSaving) "Saving..." else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !eduSaving,
                    onClick = { showEduDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Theme chooser dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeOption("Light", "light", currentTheme) {
                        scope.launch { prefs.setTheme("light") }
                        showThemeDialog = false
                    }

                    Spacer(Modifier.height(10.dp))

                    ThemeOption("Dark", "dark", currentTheme) {
                        scope.launch { prefs.setTheme("dark") }
                        showThemeDialog = false
                    }

                    Spacer(Modifier.height(10.dp))

                    ThemeOption("System Default", "system", currentTheme) {
                        scope.launch { prefs.setTheme("system") }
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ThemeOption(
    label: String,
    value: String,
    selected: String,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .background(
                if (selected == value) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ProfileOption(
    icon: Any,
    text: String,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 18.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                contentDescription = null,
                tint = iconColor
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}
