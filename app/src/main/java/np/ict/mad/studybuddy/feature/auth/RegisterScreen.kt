package np.ict.mad.studybuddy.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import np.ict.mad.studybuddy.R

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit, // called when registration completes (go back to Login)
    onBackToLogin: () -> Unit      // navigate back to Login screen
) {

    // Firebase instances for auth and saving profile
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Form input states (saved across rotation)
    var email by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    // Toggles for showing / hiding passwords
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF4A90E2), Color(0xFF3A7BD5))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Card container for the registration form
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Image(
                    painter = painterResource(id = R.drawable.studybuddylogo_cropped),
                    contentDescription = "StudyBuddy Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(bottom = 4.dp)
                )

                Text("Create Account", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Display name
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it; error = null },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        // Toggle between show/hide confirm password
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(if (showPassword) "Hide" else "Show")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Confirm password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Text(if (showConfirmPassword) "Hide" else "Show")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Error text
                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Register button
                Button(
                    onClick = {
                        // Basic validation for empty fields
                        if (email.isBlank() || displayName.isBlank() ||
                            password.isBlank() || confirmPassword.isBlank()
                        ) {
                            error = "Please fill in all fields."
                            return@Button
                        }

                        // Check both passwords match
                        if (password != confirmPassword) {
                            error = "Passwords do not match."
                            return@Button
                        }

                        loading = true

                        scope.launch {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val uid = auth.currentUser!!.uid

                                        val profile = mapOf(
                                            "email" to email,
                                            "displayName" to displayName
                                        )

                                        firestore.collection("users")
                                            .document(uid)
                                            .set(profile)
                                            .addOnSuccessListener {
                                                loading = false
                                                onRegisterSuccess()
                                            }
                                            .addOnFailureListener {
                                                loading = false
                                                error = "Failed to save profile."
                                            }

                                    } else {
                                        loading = false
                                        error = task.exception?.message ?: "Registration failed."
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Create Account", fontSize = 16.sp)
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onBackToLogin) {
                    Text("Already have an account? Sign In")
                }
            }
        }
    }

    // Simple loading indicator when registration is in progress
    if (loading) {
        CircularProgressIndicator()
    }
}
