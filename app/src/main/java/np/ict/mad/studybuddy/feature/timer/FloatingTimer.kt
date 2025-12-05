package np.ict.mad.studybuddy.feature.timer

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun FloatingTimer(timerViewModel: TimerViewModel) {
    val isRunning by timerViewModel.isRunning.collectAsState()
    val isPaused by timerViewModel.isPaused.collectAsState()
    val timeLeft by timerViewModel.remainingTime.collectAsState()

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var width by remember { mutableStateOf(150.dp) }
    var height by remember { mutableStateOf(120.dp) }
    val density = LocalDensity.current

    if (isRunning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f) // Ensure it is on top
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .align(Alignment.TopEnd) // Changed to TopEnd to avoid BottomBar overlap
                    .padding(top = 100.dp, end = 16.dp) // Padding to avoid status bar and edge
                    .size(width, height)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Close button row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { timerViewModel.stopTimer() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        val mins = timeLeft / 60
                        val secs = timeLeft % 60
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(onClick = {
                            if (isPaused) {
                                timerViewModel.resumeTimer()
                            } else {
                                timerViewModel.pauseTimer()
                            }
                        }) {
                            if (isPaused) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            } else {
                                Icon(Icons.Default.Pause, contentDescription = "Pause")
                            }
                        }
                    }
                    // Resize handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    with(density) {
                                        width += dragAmount.x.toDp()
                                        height += dragAmount.y.toDp()
                                    }
                                }
                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.AspectRatio,
                            contentDescription = "Resize",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}