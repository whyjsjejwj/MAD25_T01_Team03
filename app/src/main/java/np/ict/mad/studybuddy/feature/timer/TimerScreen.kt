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
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }

    val running by viewModel.isRunning.collectAsState()
    val paused by viewModel.isPaused.collectAsState()
    val timeLeft by viewModel.remainingTime.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.TIMER,
                onHome = onOpenHome,
                onOpenQuiz = onOpenQuiz,
                onOpenTimer = onOpenTimer,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Study Timer", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(24.dp))

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

                        Button(onClick = {
                            viewModel.stopTimer()
                        }) {
                            Text("Reset")
                        }
                    }

                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minutesInput,
                            onValueChange = { minutesInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Minutes") },
                            modifier = Modifier.width(100.dp)
                        )
                        OutlinedTextField(
                            value = secondsInput,
                            onValueChange = { secondsInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Seconds") },
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val minutes = minutesInput.toLongOrNull() ?: 0
                            val seconds = secondsInput.toLongOrNull() ?: 0
                            viewModel.setTimerWithSeconds(minutes, seconds)
                            viewModel.startTimer()
                        }
                    ) {
                        Text("Start Timer")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val savedDuration = viewModel.getSavedDuration()
                            if (savedDuration > 0) {
                                viewModel.setTimer(savedDuration)
                                viewModel.startTimer()
                            }
                        },
                        enabled = viewModel.getSavedDuration() > 0
                    ) {
                        Text("Repeat Last Timer")
                    }
                }
            }
        }
    }
}
