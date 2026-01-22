package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class StudyStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun StudyDashboardDialog(onDismiss: () -> Unit) {
    val stats = listOf(
        StudyStat("Avg. Study Time", "45 mins", Icons.Default.AccessTime, Color(0xFF2196F3)),
        StudyStat("Longest Session", "120 mins", Icons.Default.EmojiEvents, Color(0xFFFF9800)),
        StudyStat("Shortest Session", "15 mins", Icons.Default.Timer, Color(0xFF4CAF50)),
        StudyStat("Total Sessions", "42", Icons.Default.CheckCircle, Color(0xFF9C27B0)),
        StudyStat("Current Streak", "5 Days", Icons.Default.LocalFireDepartment, Color(0xFFE91E63)),
        StudyStat("Focus Score", "85%", Icons.Default.Psychology, Color(0xFF009688))
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(500.dp) // Fixed height for the dashboard
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Study Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }

                Text("Your progress at a glance", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // The Stats Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 columns
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(stats) { stat ->
                        StatCard(stat)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Close Dashboard")
                }
            }
        }
    }
}

@Composable
fun StatCard(stat: StudyStat) {
    Card(
        colors = CardDefaults.cardColors(Color(0xFFF5F5F5)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(stat.icon, contentDescription = null, tint = stat.color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(stat.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stat.label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}