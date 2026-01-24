package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import np.ict.mad.studybuddy.core.storage.HabitRepository
import np.ict.mad.studybuddy.core.storage.DailyHabitLog

data class StudyStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDashboardScreen(nav: NavController, uid: String) {
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
        StudyStat("Total Habits", "$totalHabitsDone", Icons.Default.CheckCircle, Color(0xFF4CAF50)),
        StudyStat("Avg / Day", avgHabits, Icons.Default.TrendingUp, Color(0xFFFF9800))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF8E1),
                    titleContentColor = Color(0xFF6D4C41)
                )
            )
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE7C15A))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF6D4C41))
                    Text("Your study consistency at a glance.", color = Color.Gray)
                }


                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCardFull(stats[0], Modifier.weight(1f))
                            StatCardFull(stats[1], Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCardFull(stats[2], Modifier.weight(1f))
                            StatCardFull(stats[3], Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Recent Activity Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF6D4C41))
                }

                if (history.isEmpty()) {
                    item {
                        Text("No activity recorded yet. Start checking off your daily habits!", color = Color.Gray)
                    }
                } else {
                    items(history) { log ->
                        HistoryItemCard(log)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardFull(stat: StudyStat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(stat.icon, contentDescription = null, tint = stat.color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(stat.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stat.label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun HistoryItemCard(log: DailyHabitLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Text(log.date, fontWeight = FontWeight.Medium)
            }

            Surface(
                color = if (log.completedCount >= 4) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${log.completedCount} Habits",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = if (log.completedCount >= 4) Color(0xFF2E7D32) else Color(0xFFE65100),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}