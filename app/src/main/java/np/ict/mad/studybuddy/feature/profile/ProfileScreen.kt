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
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.feature.auth.LoginPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    displayName: String,
    email: String,
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onChangePassword: () -> Unit = {}
) {
    val context = LocalContext.current

    // using my LoginPreferences to read theme & maybe future profile data
    val prefs = remember { LoginPreferences(context) }

    // coroutine scope tied to this screen (so it cancels when I leave)
    val scope = rememberCoroutineScope()

    // reading the saved theme from DataStore (Flow -> UI updates automatically)
    val currentTheme by prefs.theme.collectAsState(initial = "system")
    var showThemeDialog by remember { mutableStateOf(false) }

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

            // Account Section
            SettingsSectionTitle("Account")

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
