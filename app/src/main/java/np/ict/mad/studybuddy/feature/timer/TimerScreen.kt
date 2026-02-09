package np.ict.mad.studybuddy.feature.timer

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.util.Locale

import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

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
    var secondsInput by rememberSaveable { mutableStateOf("") }
    var presetName by rememberSaveable { mutableStateOf("") }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }

    val running by viewModel.isRunning.collectAsState()
    val paused by viewModel.isPaused.collectAsState()
    val timeLeft by viewModel.remainingTime.collectAsState()
    val lastDuration by viewModel.lastDuration.collectAsState()
    val presets by viewModel.presets.collectAsState()

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
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Study Timer", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(20.dp))

                if (running) {
                    val mins = timeLeft / 60
                    val secs = timeLeft % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (paused) {
                            Button(onClick = { viewModel.resumeTimer() }) { Text("Resume") }
                        } else {
                            Button(onClick = { viewModel.pauseTimer() }) { Text("Pause") }
                        }
                        Button(onClick = { viewModel.stopTimer() }) { Text("Stop & Reset") }
                    }
                } else {

                    // Seconds-only input (no KeyboardOptions to avoid dependency issues)
                    OutlinedTextField(
                        value = secondsInput,
                        onValueChange = {
                            secondsInput = it.filter { c -> c.isDigit() }
                            inputError = null
                        },
                        isError = inputError != null,
                        label = { Text("Seconds") },
                        modifier = Modifier.width(180.dp)
                    )

                    if (inputError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            inputError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                val secs = secondsInput.toLongOrNull() ?: 0
                                if (secs <= 0) {
                                    inputError = "Enter a valid time in seconds"
                                    return@Button
                                }
                                viewModel.setTimerFromSeconds(secs)
                                viewModel.startTimer()
                            }
                        ) { Text("Start Timer") }

                        Button(
                            enabled = lastDuration > 0,
                            onClick = {
                                viewModel.setTimerFromSeconds(lastDuration)
                                viewModel.startTimer()
                            }
                        ) { Text("Repeat Last") }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = presetName,
                            onValueChange = { presetName = it },
                            label = { Text("Preset Name") },
                            modifier = Modifier.width(180.dp)
                        )
                        Button(
                            onClick = {
                                val secs = secondsInput.toLongOrNull() ?: 0
                                if (presetName.isBlank()) {
                                    inputError = "Preset name required"
                                    return@Button
                                }
                                if (secs <= 0) {
                                    inputError = "Enter seconds to save preset"
                                    return@Button
                                }
                                viewModel.savePreset(presetName.trim(), secs)
                                presetName = ""
                            }
                        ) { Text("Save") }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (presets.isNotEmpty()) {
                        Text("Saved Timers", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(presets) { preset ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(
                                        onClick = { viewModel.loadPreset(preset, autoStart = true) }
                                    ) {
                                        val mm = preset.seconds / 60
                                        val ss = preset.seconds % 60
                                        Text("${preset.name} (${String.format("%02d:%02d", mm, ss)})")
                                    }
                                    IconButton(onClick = { viewModel.deletePreset(preset.name) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }

                // ---- VIDEO SECTION ----
                Spacer(Modifier.height(24.dp))
                Text("Study Video", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                VideoPickerRow(viewModel = viewModel)

                Spacer(Modifier.height(12.dp))

                TimerVideoPlayer(
                    videoUri = viewModel.videoUri.collectAsState().value,
                    videoState = viewModel.videoState.collectAsState().value
                )
            }
        }
    }
}

/* ---------------------------
   Video helpers and UI parts
   --------------------------- */

@Composable
private fun VideoPickerRow(viewModel: TimerViewModel) {
    val context = LocalContext.current
    val uri by viewModel.videoUri.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { result: Uri? ->
        if (result != null) {
            // Persist long-term read access for the selected video
            try {
                context.contentResolver.takePersistableUriPermission(
                    result,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some OEMs/devices may not allow persistable permissions from this flow; ignore
            }
            viewModel.setVideoUri(result)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { launcher.launch(arrayOf("video/*")) }) {
            Text(if (uri == null) "Pick Video" else "Change Video")
        }

        if (uri != null) {
            val name = rememberDisplayName(uri)
            Text(name ?: "Selected video")
            Button(onClick = { viewModel.clearVideoUri() }) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun TimerVideoPlayer(
    videoUri: Uri?,
    videoState: TimerViewModel.VideoState
) {
    if (videoUri == null) return

    val context = LocalContext.current

    AndroidView(
        factory = {
            VideoView(context).apply {
                setVideoURI(videoUri)
                setOnPreparedListener { mp ->
                    mp.isLooping = true // loop while timer is running
                    when (videoState) {
                        TimerViewModel.VideoState.PLAY -> start()
                        TimerViewModel.VideoState.PAUSE -> pause()
                        TimerViewModel.VideoState.STOP -> {
                            pause()
                            seekTo(0)
                        }
                    }
                }
            }
        },
        update = { view ->
            when (videoState) {
                TimerViewModel.VideoState.PLAY -> if (!view.isPlaying) view.start()
                TimerViewModel.VideoState.PAUSE -> view.pause()
                TimerViewModel.VideoState.STOP -> {
                    view.pause()
                    view.seekTo(0)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}

@Composable
private fun rememberDisplayName(uri: Uri?): String? {
    val context = LocalContext.current
    return remember(uri) {
        if (uri == null) return@remember null
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
    }
}