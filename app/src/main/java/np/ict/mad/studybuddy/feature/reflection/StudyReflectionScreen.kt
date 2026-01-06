package np.ict.mad.studybuddy.feature.reflection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyReflectionScreen(
    uid: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val reflectionDb = remember { StudyReflectionFirestore() }

    var subject by remember { mutableStateOf("") } // subject the reflection is about
    var reflection by remember { mutableStateOf("") } // contents of reflection
    var mood by remember { mutableStateOf("") } // how the user felt when reflecting
    var saving by remember { mutableStateOf(false) } // to save it to firebase properly

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Reflection",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text boxes for the reflection (3 questions only)
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("What did you study today? (E.g. Math, Biology)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reflection,
                onValueChange = { reflection = it },
                label = { Text("What did you do/learn in this study session?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            OutlinedTextField(
                value = mood,
                onValueChange = { mood = it },
                label = { Text("How do you feel after the study session ended?") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving,
                onClick = {
                    saving = true

                    // saving of reflection to firebase
                    scope.launch{
                        try{
                            reflectionDb.addReflection(
                                StudyReflection(
                                    uid = uid,
                                    subject = subject,
                                    reflection = reflection,
                                    mood = mood
                                )
                            )
                            saving = false
                            onBack()
                        }catch (e: Exception) {
                            saving = false
                        }
                    }
                }
            ) {
                Text(if (saving) "Saving..." else "Save Reflection")
            }
        }
    }
}
