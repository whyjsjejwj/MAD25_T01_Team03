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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.R
import np.ict.mad.studybuddy.feature.subscription.SubscriptionManager
import np.ict.mad.studybuddy.feature.subscription.UserTier
// --- IMPORT YOUR THEME COLORS ---
import np.ict.mad.studybuddy.ui.theme.PurpleGrey40 // Brown
import np.ict.mad.studybuddy.ui.theme.Pink40       // Cream
import np.ict.mad.studybuddy.ui.theme.Purple40     // Gold

@Composable
fun ZenSoundPlayer(onUpgrade: () -> Unit) {
    val context = LocalContext.current
    val isSilver = SubscriptionManager.userTier.hasAccess(UserTier.SILVER)

    var currentSound by remember { mutableStateOf<Int?>(null) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
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
        colors = CardDefaults.cardColors(containerColor = Pink40), // <--- Cream Background
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🎵 Zen Focus Sounds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PurpleGrey40 // <--- Brown Text
            )
            Text(
                "Enhance your concentration",
                style = MaterialTheme.typography.bodySmall,
                color = PurpleGrey40.copy(alpha = 0.7f) // Lighter Brown
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSilver) {
                // ROW 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SoundButton("Rain", R.raw.rain, currentSound == R.raw.rain) { playSound(R.raw.rain) }
                    SoundButton("Fire", R.raw.fire, currentSound == R.raw.fire) { playSound(R.raw.fire) }
                    SoundButton("Noise", R.raw.white_noise, currentSound == R.raw.white_noise) { playSound(R.raw.white_noise) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // ROW 2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SoundButton("Nature", R.raw.nature, currentSound == R.raw.nature) { playSound(R.raw.nature) }
                    SoundButton("Night", R.raw.night, currentSound == R.raw.night) { playSound(R.raw.night) }
                }
            } else {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGrey40),
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
fun SoundButton(label: String, resId: Int, isPlaying: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isPlaying) PurpleGrey40 else Color.White) // <--- Brown when active
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = label,
                tint = if (isPlaying) Color.White else PurpleGrey40 // <--- Brown icon
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = PurpleGrey40)
    }
}