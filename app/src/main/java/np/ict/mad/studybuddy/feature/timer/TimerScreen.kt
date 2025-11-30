package np.ict.mad.studybuddy.feature.timer

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Locale




@Composable
fun TimerScreen(
    nav: NavController,
    viewModel: TimerViewModel
) {
    var minutesInput by remember { mutableStateOf("") }

    val running by viewModel.isRunning.collectAsState()
    val paused by viewModel.isPaused.collectAsState()
    val timeLeft by viewModel.remainingTime.collectAsState()

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Study Timer", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(24.dp))

            // Show remaining time
            if (running) {
                val mins = timeLeft / 60
                val secs = timeLeft % 60
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!paused) {
                        Button(onClick = { viewModel.pauseTimer() }) {
                            Text("Pause")
                        }
                    } else {
                        Button(onClick = { viewModel.resumeTimer() }) {
                            Text("Resume")
                        }
                    }

                    Button(onClick = { viewModel.stopTimer(); nav.popBackStack() }) {
                        Text("Stop")
                    }
                }

                return@Column
            }

            // INPUT FOR NEW TIMER
            OutlinedTextField(
                value = minutesInput,
                onValueChange = { minutesInput = it.filter { c -> c.isDigit() } },
                label = { Text("Minutes") }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val totalSeconds = (minutesInput.toIntOrNull() ?: 0) * 60
                    viewModel.setTimer(totalSeconds.toLong())
                    viewModel.startTimer()
                    nav.popBackStack()
                }
            ) {
                Text("Start Timer")
            }
        }
    }
}


