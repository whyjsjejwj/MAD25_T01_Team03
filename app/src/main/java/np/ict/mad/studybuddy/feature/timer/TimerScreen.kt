package np.ict.mad.studybuddy.feature.timer

import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab
import java.util.Locale

@Composable
fun TimerScreen(
    nav: NavController,
    viewModel: TimerViewModel,
    onOpenHome: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenTimer: () -> Unit
) {
    var minInput by remember { mutableStateOf("") }
    var secInput by remember { mutableStateOf("") }

    val running by viewModel.isRunning.collectAsState()
    val paused by viewModel.isPaused.collectAsState()
    val timeLeft by viewModel.remainingTime.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(BottomNavTab.TIMER, onOpenHome, onOpenQuiz, onOpenTimer, onOpenMotivation)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Study Timer", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(30.dp))

            if (running) {
                val displayMins = timeLeft / 60
                val displaySecs = timeLeft % 60
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", displayMins, displaySecs),
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { if (paused) viewModel.startTimer(0,0) else viewModel.pauseTimer() }) {
                        Text(if (paused) "Resume" else "Pause")
                    }
                    Button(onClick = { viewModel.stopTimer() }) { Text("Reset") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minInput,
                        onValueChange = { minInput = it.take(2).filter { c -> c.isDigit() } },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = secInput,
                        onValueChange = { secInput = it.take(2).filter { c -> c.isDigit() } },
                        label = { Text("Sec") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val m = minInput.toIntOrNull() ?: 0
                        val s = secInput.toIntOrNull() ?: 0
                        viewModel.startTimer(m, s)
                    }
                ) { Text("Start Study Session") }
            }

            Spacer(Modifier.height(40.dp))
            VideoPickerSection(viewModel)

            Spacer(Modifier.height(16.dp))
            val videoUri by viewModel.videoUri.collectAsState()
            val videoState by viewModel.videoState.collectAsState()

            if (videoUri != null) {
                TimerVideoPlayer(videoUri, videoState)
            }
        }
    }
}

@Composable
fun TimerVideoPlayer(videoUri: Uri?, videoState: TimerViewModel.VideoState) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                // Improve audio performance for emulators
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
                setAudioAttributes(audioAttributes)

                setVideoURI(videoUri)
                setOnPreparedListener { it.isLooping = true }
            }
        },
        update = { view ->
            when (videoState) {
                TimerViewModel.VideoState.PLAY -> if (!view.isPlaying) view.start()
                TimerViewModel.VideoState.PAUSE -> view.pause()
                TimerViewModel.VideoState.STOP -> { view.pause(); view.seekTo(0) }
            }
        },
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    )
}

@Composable
fun VideoPickerSection(viewModel: TimerViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.setVideoUri(uri)
        }
    }
    Button(onClick = { launcher.launch(arrayOf("video/*")) }) {
        Text("Select Study Ambience Video")
    }
}