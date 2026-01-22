package np.ict.mad.studybuddy.feature.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@Composable
fun SubscriptionScreen(onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(onClick = onClose) { Text("Awesome!") }
            },
            title = { Text("Upgrade Successful") },
            text = { Text("Welcome to the ${SubscriptionManager.userTier.label} Club!") },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green) }
        )
    }

    if (isProcessing) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFFE7C15A))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Contacting Payment Gateway...", fontWeight = FontWeight.Bold)
                    Text("Please do not close the app.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upgrade StudyBuddy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Unlock focus tools & analytics", color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        TierCard(
            title = "Silver Tier",
            price = "$1.99",
            features = listOf("Shuffle Quotes", "Zen Focus Sounds", "Custom Backgrounds"),
            color = Color(0xFFC0C0C0),
            onClick = {
                scope.launch {
                    isProcessing = true
                    SubscriptionManager.simulatePayment(UserTier.SILVER)
                    isProcessing = false
                    showSuccess = true
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TierCard(
            title = "Gold Tier",
            price = "$4.99",
            features = listOf("Everything in Silver", "Daily Journal", "Progress Analytics", "PDF Export"),
            color = Color(0xFFFFD700),
            onClick = {
                scope.launch {
                    isProcessing = true
                    SubscriptionManager.simulatePayment(UserTier.GOLD)
                    isProcessing = false
                    showSuccess = true
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onClose) { Text("Cancel") }
    }
}

@Composable
fun TierCard(title: String, price: String, features: List<String>, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, color),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(price, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            features.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }
    }
}