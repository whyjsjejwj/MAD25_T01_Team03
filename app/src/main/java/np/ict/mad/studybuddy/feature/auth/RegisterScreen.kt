package np.ict.mad.studybuddy.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import np.ict.mad.studybuddy.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    // education level options users can choose from
    val educationOptions = listOf(
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
    )

    var educationLevel by rememberSaveable { mutableStateOf("") } // store chosen value for education level
    var eduExpanded by remember { mutableStateOf(false) } // dropdown open/close

    // ✅ Match Login gradient
    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFFFFFBF2),  // soft cream
            Color(0xFFF3D67C)   // warm yellow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ✅ Logo similar sizing to Login
                Image(
                    painter = painterResource(id = R.drawable.studybuddylogo_cropped),
                    contentDescription = "StudyBuddy Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 0.dp)
                )

                Spacer(Modifier.height(4.dp))

                Text("Create Account", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it; error = null },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(Modifier.height(12.dp))

                // for the change education level for the users
                ExposedDropdownMenuBox(
                    expanded = eduExpanded,
                    onExpandedChange = { eduExpanded = !eduExpanded }
                ) {
                    OutlinedTextField(
                        value = educationLevel,
                        onValueChange = {}, // user selects from menu
                        readOnly = true,
                        label = { Text("Education Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eduExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = eduExpanded,
                        onDismissRequest = { eduExpanded = false }
                    ) {
                        educationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    educationLevel = option
                                    eduExpanded = false
                                    error = null
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation =
                        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(
                                if (showPassword) "Hide" else "Show",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Password
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation =
                        if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Text(
                                if (showConfirmPassword) "Hide" else "Show",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    )
                )

                // Error message
                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    enabled = !loading,
                    onClick = {
                        if (email.isBlank() || displayName.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            error = "Please fill in all fields."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            error = "Passwords do not match."
                            return@Button
                        }
                        if (educationLevel.isBlank()) {
                            error = "Please select your education level."
                            return@Button
                        }

                        scope.launch {
                            loading = true
                            error = null
                            try {
                                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                val uid = auth.currentUser?.uid ?: throw Exception("No user.")

                                val profile = mapOf(
                                    "email" to email.trim(),
                                    "displayName" to displayName.trim(),
                                    "educationLevel" to educationLevel
                                )

                                firestore.collection("users")
                                    .document(uid)
                                    .set(profile)
                                    .await()

                                loading = false
                                onRegisterSuccess()
                            } catch (e: Exception) {
                                loading = false
                                error = e.message ?: "Registration failed."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(if (loading) "Creating..." else "Create Account", fontSize = 16.sp)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Already have an account? Sign In",
                    modifier = Modifier.clickable { onBackToLogin() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
