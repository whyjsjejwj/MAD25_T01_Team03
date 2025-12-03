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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.R

@Composable
fun LoginScreen(
    loginPrefs: LoginPreferences,                   // used to save "Remember me" data
    onLoginSuccess: (String, String) -> Unit,       //navigate to home after login
    onNavigateRegister: () -> Unit,                 // go to Register page
    onNavigateForgotPassword: () -> Unit            // go to forgot password page
) {

    // FirebaseAuth is Firebase's built-in login system. I must get an instance of it
    // using getInstance() before I can call any authentication functions such as
    // signInWithEmailAndPassword or createUserWithEmailAndPassword. Without this
    // instance, the app cannot log in users or access their UID.
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    // User input fields + UI states
    // rememberSaveable keeps the values if screen rotates
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFFFFF4DB),
            Color(0xFFF3D67C)
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

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.studybuddylogo_cropped),
                    contentDescription = "StudyBuddy Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 0.dp)
                )

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },  // update input + clear old error
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next, // shows “Next” button on keyboard
                        keyboardType = KeyboardType.Email // email friendly keyboard allows @
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation =        // hide or show password
                        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {              // "Show / Hide" toggle button
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(if (showPassword) "Hide" else "Show")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done, // Finish entering password, good practice
                        keyboardType = KeyboardType.Password //password optimised keyboard
                                                             // no auto correct etc

                    )
                )

                Spacer(Modifier.height(8.dp))

                // Remember Me + Forgot Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Checkbox for “Remember Me"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text("Remember Me")
                    }

                    // Clickable text for forgot password
                    Text(
                        text = "Forgot Password?",
                        modifier = Modifier.clickable { onNavigateForgotPassword() },
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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

                // LOGIN BUTTON
                Button(
                    onClick = {

                        // Basic validation
                        if (email.isEmpty() || password.isEmpty()) {
                            error = "Please fill in all fields."
                            return@Button
                        }

                        // Firebase login using email & password
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { result ->
                                val uid = result.user!!.uid   // Firebase user ID

                                firestore.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        val displayName = doc.getString("displayName") ?: ""

                                        // Save login details if user ticked Remember Me
                                        scope.launch {
                                            if (rememberMe) {
                                                loginPrefs.saveLogin(uid, email, displayName)
                                            }
                                        }

                                        // Navigate to Home using callback
                                        onLoginSuccess(uid, displayName)
                                    }
                                    .addOnFailureListener {
                                        error = "Failed to load profile."
                                    }
                            }
                            .addOnFailureListener {
                                error = "Invalid email or password."
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Sign In", fontSize = 16.sp)
                }

                Spacer(Modifier.height(12.dp))

                // Register text
                Text(
                    "Don't have an account? Register here",
                    modifier = Modifier.clickable { onNavigateRegister() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
