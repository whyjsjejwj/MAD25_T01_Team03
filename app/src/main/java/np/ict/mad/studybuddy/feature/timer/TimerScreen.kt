package np.ict.mad.studybuddy.feature.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab
import java.util.*

@Composable
fun TimerScreen(
    nav: NavController,
    viewModel: TimerViewModel,
    uid: String,
    displayName: String,
    email: String,
    onOpenHome: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenTimer: () -> Unit
) {
    // State for user input in the minutes and seconds text fields
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }

    // Observing timer state from the ViewModel
    val running by viewModel.isRunning.collectAsState() // true if the timer is active
    val paused by viewModel.isPaused.collectAsState()   // true if the timer is paused
    val timeLeft by viewModel.remainingTime.collectAsState() // remaining time in seconds

    // Scaffold provides a consistent layout structure with a bottom navigation bar
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.TIMER, // Highlight the Timer tab
                onHome = onOpenHome,
                onOpenQuiz = onOpenQuiz,
                onOpenTimer = onOpenTimer,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { padding ->
        // Box to center the content and respect padding from Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Column arranges UI vertically
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Screen title
                Text("Study Timer", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(24.dp)) // Spacing between title and content

                if (running) {
                    // When Timer is active, display remaining time
                    val mins = timeLeft / 60
                    val secs = timeLeft % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(Modifier.height(20.dp))

                    // Aligning the Row containing pause/resume and reset buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (!paused) {
                            // Timer is running, show pause button
                            Button(onClick = { viewModel.pauseTimer() }) {
                                Text("Pause")
                            }
                        } else {
                            // Timer is paused, show resume button
                            Button(onClick = { viewModel.resumeTimer() }) {
                                Text("Resume")
                            }
                        }

                        // Reset button stops the timer and resets values
                        Button(onClick = {
                            viewModel.stopTimer()
                        }) {
                            Text("Reset")
                        }
                    }

                } else {
                    // Timer is not running, show input fields for minutes and seconds
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Minutes input field: only digits allowed
                        OutlinedTextField(
                            value = minutesInput,
                            onValueChange = { minutesInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Minutes") },
                            modifier = Modifier.width(100.dp)
                        )
                        // Seconds input field: only digits allowed
                        OutlinedTextField(
                            value = secondsInput,
                            onValueChange = { secondsInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Seconds") },
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Button to start a new timer with user input
                    Button(
                        onClick = {
                            val minutes = minutesInput.toLongOrNull() ?: 0
                            val seconds = secondsInput.toLongOrNull() ?: 0
                            viewModel.setTimerWithSeconds(minutes, seconds) // Set total duration
                            viewModel.startTimer() // Start countdown
                        }
                    ) {
                        Text("Start Timer")
                    }

                    Spacer(Modifier.height(8.dp))

                    // Button to repeat last saved timer
                    Button(
                        onClick = {
                            val savedDuration = viewModel.getSavedDuration()
                            if (savedDuration > 0) {
                                viewModel.setTimer(savedDuration) // Set timer to last saved duration
                                viewModel.startTimer() // Start countdown
                            }
                        },
                        enabled = viewModel.getSavedDuration() > 0 // Disable if no saved duration
                    ) {
                        Text("Repeat Last Timer")
                    }
                }
            }
        }
    }
}
