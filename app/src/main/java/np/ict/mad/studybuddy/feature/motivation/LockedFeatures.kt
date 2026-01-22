package np.ict.mad.studybuddy.feature.motivation

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.ict.mad.studybuddy.R
import np.ict.mad.studybuddy.feature.subscription.SubscriptionManager
import np.ict.mad.studybuddy.feature.subscription.UserTier

@Composable
fun ZenSoundPlayer(onUpgrade: () -> Unit) {
    val context = LocalContext.current
    val isSilver = SubscriptionManager.userTier.hasAccess(UserTier.SILVER)

    var currentSound by remember { mutableStateOf<Int?>(null) }

    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    fun playSound(resId: Int) {
        if (currentSound == resId) {
            mediaPlayer.reset()
            currentSound = null
        } else {
            try {
                mediaPlayer.reset()
                val descriptor = context.resources.openRawResourceFd(resId)
                mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()

                mediaPlayer.prepare()
                mediaPlayer.setLooping(true)
                mediaPlayer.start()
                currentSound = resId
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(Color(0xFFE0F7FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🎵 Zen Focus Sounds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Enhance your concentration", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            if (isSilver) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SoundButton("Rain", R.raw.rain, currentSound == R.raw.rain) { playSound(R.raw.rain) }
                    SoundButton("Fire", R.raw.fire, currentSound == R.raw.fire) { playSound(R.raw.fire) }
                    SoundButton("Noise", R.raw.white_noise, currentSound == R.raw.white_noise) { playSound(R.raw.white_noise) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SoundButton("Nature", R.raw.nature, currentSound == R.raw.nature) { playSound(R.raw.nature) }
                    SoundButton("Night", R.raw.night, currentSound == R.raw.night) { playSound(R.raw.night) }
                }

            } else {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock with Silver Tier")
                }
            }
        }
    }
}

@Composable
fun ConsistencyDashboard(onUpgrade: () -> Unit) {
    val isGold = SubscriptionManager.userTier.hasAccess(UserTier.GOLD)

    Card(
        colors = CardDefaults.cardColors(Color(0xFFFFF8E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥 Study Consistency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (isGold) {
                    Text("Streak: 12 Days", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isGold) {
                Text("Last 30 Days Activity:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))

                val fakeHistory = remember {
                    List(30) { kotlin.random.Random.nextBoolean() }
                }

                Column {
                    repeat(3) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(10) { colIndex ->
                                val index = rowIndex * 10 + colIndex
                                val didStudy = fakeHistory.getOrElse(index) { false }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (didStudy) Color(0xFF4CAF50)
                                            else Color(0xFFE0E0E0)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("You are in the top 10% of students!", fontSize = 12.sp, color = Color.Gray)

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onUpgrade,
                        colors = ButtonDefaults.buttonColors(Color(0xFFFFD700))
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Unlock Dashboard (Gold)")
                    }
                }
            }
        }
    }
}

@Composable
fun SoundButton(label: String, resId: Int, isPlaying: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isPlaying) Color(0xFF009688) else Color.White)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                contentDescription = label,
                tint = if (isPlaying) Color.White else Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}