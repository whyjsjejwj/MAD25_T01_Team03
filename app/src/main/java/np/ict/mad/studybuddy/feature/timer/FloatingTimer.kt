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
    // Observing the state of the timer from the ViewModel
    // 'isRunning' indicates if the timer is currently active
    val isRunning by timerViewModel.isRunning.collectAsState()
    // 'isPaused' indicates if the timer is temporarily paused
    val isPaused by timerViewModel.isPaused.collectAsState()
    // 'timeLeft' stores the remaining seconds in the countdown
    val timeLeft by timerViewModel.remainingTime.collectAsState()

    // Mutable state for dragging the timer around the screen
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Mutable state for dynamically resizing the timer card
    var width by remember { mutableStateOf(150.dp) }
    var height by remember { mutableStateOf(120.dp) }

    // Used to convert pixels to Dp when resizing
    val density = LocalDensity.current

    // Only show the floating timer when it is running
    if (isRunning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f) // Ensure timer is displayed on top of other UI elements
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Background color
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Shadow for visual separation
                modifier = Modifier
                    // Position the timer based on user drag offsets
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    // Make the card draggable
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume() // Mark the drag gesture as handled
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .align(Alignment.TopEnd) // Start at top-right to avoid BottomBar overlap
                    .padding(top = 100.dp, end = 16.dp) // Avoid status bar and screen edges
                    .size(width, height) // Initial size, can be resized
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Row for the close button, aligned to the top-right of the timer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { timerViewModel.stopTimer() }, // Stop the timer and remove it
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Convert remaining seconds to minutes and seconds
                        val mins = timeLeft / 60
                        val secs = timeLeft % 60
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.titleMedium // Timer display style
                        )

                        // Button to pause/resume timer depending on current state
                        IconButton(onClick = {
                            if (isPaused) {
                                timerViewModel.startTimer(0,0)
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

                    // Resize handle at the bottom-right corner of the timer
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume() // Mark gesture as handled
                                    with(density) {
                                        // Dynamically resize width and height based on drag amount
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
