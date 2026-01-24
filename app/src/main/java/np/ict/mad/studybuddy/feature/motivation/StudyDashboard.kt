package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import np.ict.mad.studybuddy.core.storage.HabitRepository
import np.ict.mad.studybuddy.core.storage.DailyHabitLog

data class StudyStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun StudyDashboardDialog(uid: String, onDismiss: () -> Unit) {
    val habitRepo = remember { HabitRepository() }
    var history by remember { mutableStateOf<List<DailyHabitLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        history = habitRepo.getHabitHistory(uid)
        isLoading = false
    }

    val totalSessions = history.size
    val totalHabitsDone = history.sumOf { it.completedCount }

    val avgHabits = if (totalSessions > 0) String.format("%.1f", totalHabitsDone.toFloat() / totalSessions) else "0"

    val streak = history.takeWhile { it.completedCount > 0 }.count()

    val stats = listOf(
        StudyStat("Current Streak", "$streak Days", Icons.Default.LocalFireDepartment, Color(0xFFE91E63)),
        StudyStat("Active Days", "$totalSessions Days", Icons.Default.CalendarMonth, Color(0xFF2196F3)),
        StudyStat("Habits Done", "$totalHabitsDone", Icons.Default.CheckCircle, Color(0xFF4CAF50)),
        StudyStat("Avg Habits/Day", avgHabits, Icons.Default.TrendingUp, Color(0xFFFF9800))
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Real Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }

                Text("Based on your checklist history", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(stats) { stat ->
                            StatCard(stat)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) { Text("Close Dashboard") }
            }
        }
    }
}

@Composable
fun StatCard(stat: StudyStat) {
    Card(
        colors = CardDefaults.cardColors(Color(0xFFF5F5F5)),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(stat.icon, contentDescription = null, tint = stat.color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(stat.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stat.label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}