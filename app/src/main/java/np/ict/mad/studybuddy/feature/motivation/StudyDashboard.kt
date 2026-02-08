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

// simple data class to hold the stat info for the ui cards
// makes it easier to pass data around to the grid components
data class StudyStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDashboardScreen(nav: NavController, uid: String) {
    // section database setup
    // initializes the repository to fetch user data
    val habitRepo = remember { HabitRepository() }

    // stores the history logs retrieved from firebase
    // uses mutablestateof to trigger a ui refresh once data arrives
    var history by remember { mutableStateOf<List<DailyHabitLog>>(emptyList()) }

    // shows a loading spinner while fetching data to prevent a blank screen
    var isLoading by remember { mutableStateOf(true) }

    // controls the visibility of the help popup
    var showInfoDialog by remember { mutableStateOf(false) }

    // section fetch data
    // uses launchedeffect to fetch data only once when the screen opens
    LaunchedEffect(uid) {
        // gets the user's entire habit history
        history = habitRepo.getHabitHistory(uid)
        // hides the loading spinner once data is ready
        isLoading = false
    }

    // section calculate statistics
    // 1. calculates total days user has used the app (based on log count)
    val totalSessions = history.size

    // 2. calculates total number of checkboxes ticked ever
    val totalHabitsDone = history.sumOf { it.completedCount }

    // 3. calculates average habits per day
    // handles division by zero error if total sessions is 0
    val avgHabits = if (totalSessions > 0) String.format("%.1f", totalHabitsDone.toFloat() / totalSessions) else "0"

    // 4. consistency score calculation
    // assumes 4 daily tasks, checks how many they actually did vs max possible
    val maxPossibleHabits = totalSessions * 4
    val consistency = if (maxPossibleHabits > 0) {
        (totalHabitsDone.toFloat() / maxPossibleHabits) * 100
    } else {
        0f
    }
    val consistencyString = String.format("%.0f%%", consistency)

    // prepares the list of stats to display in the grid
    val stats = listOf(
        StudyStat("Consistency", consistencyString, Icons.Default.PieChart, Color(0xFFE91E63)),
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
                // info button to explain what the stats mean
                // good for ux so users understand the numbers
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Help", tint = Color(0xFF6D4C41))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF8E1),
                    titleContentColor = Color(0xFF6D4C41)
                )
            )
        }
    ) { innerPadding ->

        // section info popup
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("Dashboard Guide", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Here is how your stats are calculated:", style = MaterialTheme.typography.bodyMedium)
                        Divider()
                        InfoRow(Icons.Default.PieChart, "Consistency", "Percentage of daily tasks you completed on days you studied.")
                        InfoRow(Icons.Default.CalendarMonth, "Active Days", "Total number of days you logged at least one study habit.")
                        InfoRow(Icons.Default.CheckCircle, "Total Habits", "The sum of all individual checklist items completed.")
                        InfoRow(Icons.Default.TrendingUp, "Avg / Day", "Average number of tasks completed per active study day.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Got it")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // section loading state
        // displays a spinner if data is still being fetched from firebase
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE7C15A))
            }
        } else {
            // uses lazycolumn to list stats and history items efficiently
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // section header
                item {
                    Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF6D4C41))
                    Text("Your study performance at a glance.", color = Color.Gray)
                }

                // section stats grid
                // displays the 4 cards calculated earlier in a 2x2 grid layout
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

                // section recent history list
                // shows a scrollable list of days where the user studied
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

// helper composable for the help dialog rows
@Composable
fun InfoRow(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6D4C41), modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// reusable stat card component
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

// reusable history item component
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

            // color codes the badge based on how many habits were done
            // green if they did all 4, orange if less
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