package np.ict.mad.studybuddy.feature.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
fun SubscriptionScreen(uid: String, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val currentTier = SubscriptionManager.userTier
    val isSubscribed = currentTier != UserTier.BRONZE

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = { Button(onClick = onClose) { Text("OK") } },
            title = { Text("Success") },
            text = { Text("Your subscription has been updated.") },
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
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFE7C15A))
                    Spacer(Modifier.height(16.dp))
                    Text("Updating Plan...", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Subscription Plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        if (isSubscribed) {
            Text("Current Plan: ${currentTier.label}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        } else {
            Text("Unlock focus tools & analytics", color = Color.Gray)
        }

        Spacer(Modifier.height(24.dp))

        TierCard(
            title = "Silver Tier",
            price = "$1.99 / mo",
            features = listOf("Zen Focus Sounds", "Remove Ads", "Support the Devs"),
            color = Color(0xFFC0C0C0),
            onClick = {
                scope.launch {
                    isProcessing = true
                    SubscriptionManager.purchaseSubscription(uid, UserTier.SILVER)
                    isProcessing = false
                    showSuccess = true
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        TierCard(
            title = "Gold Tier",
            price = "$4.99 / mo",
            features = listOf("Everything in Silver", "Progress Analytics", "Study Dashboard"),
            color = Color(0xFFFFD700),
            onClick = {
                scope.launch {
                    isProcessing = true
                    SubscriptionManager.purchaseSubscription(uid, UserTier.GOLD)
                    isProcessing = false
                    showSuccess = true
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        if (isSubscribed) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        isProcessing = true
                        SubscriptionManager.cancelSubscription(uid)
                        isProcessing = false
                        showSuccess = true
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cancel Subscription")
            }
            Spacer(Modifier.height(8.dp))
        }

        TextButton(onClick = onClose) { Text("Close") }
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
            features.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray) }
        }
    }
}