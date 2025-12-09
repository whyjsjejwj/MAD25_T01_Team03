package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.core.storage.MotivationFirestore
import np.ict.mad.studybuddy.core.storage.QuotesFirestore
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationScreen(
    uid: String,
    onOpenHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenFavourites: () -> Unit
) {
    // initialize database helpers
    val quotesDb = remember { QuotesFirestore() }
    val motivationDb = remember { MotivationFirestore() }

    // state variables for quotes list
    var quotes by remember { mutableStateOf<List<MotivationItem>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // --- Daily Habit Checklist ---
    // hardcoded list of habits
    val habits = listOf(
        "Study at least 25 minutes",
        "Review yesterday’s notes",
        "Write 3 key points learned",
        "Plan tomorrow’s task"
    )

    // track which habits are checked (true/false)
    var habitStatus by remember {
        mutableStateOf(habits.associateWith { false })
    }

    // calculate progress for the progress bar
    val completedHabits = habitStatus.values.count { it }
    val totalHabits = habits.size

    // --- Load Quote Data From Firebase ---
    // fetch data when screen loads
    LaunchedEffect(Unit) {
        quotesDb.getQuotes { list ->
            quotes = list.take(5)
            selectedIndex = null   // DO NOT auto-select
        }
    }

    // helper to get the selected quote object
    val selectedQuote = selectedIndex?.let { idx ->
        quotes.getOrNull(idx)
    }

    // Background
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFFFFFDF7), Color(0xFFFFF7E8))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motivation Hub", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFFE7C980))
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.MOTIVATION,
                onHome = onOpenHome,
                onOpenQuiz = onOpenQuiz,
                onOpenTimer = onOpenTimer,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { innerPadding ->

        // main content column with scrolling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 20.dp
                )
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {

            // quote selector card component
            QuoteSelectorCard(
                quotes = quotes,
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it },
                selectedQuote = selectedQuote,
                // logic to save quote to firebase
                onSave = {
                    if (selectedQuote != null && selectedIndex != null) {
                        motivationDb.addFavourite(uid, selectedQuote)
                        motivationDb.saveSelectedIndex(uid, selectedIndex!!)
                    }
                },
                onOpenFavourites = onOpenFavourites
            )

            // daily habits card component
            DailyChecklistCard(
                habits = habits,
                habitStatus = habitStatus,
                onHabitToggle = { habit ->
                    // update map state when checkbox is clicked
                    habitStatus = habitStatus.toMutableMap().apply {
                        this[habit] = !(this[habit] ?: false)
                    }
                },
                completedHabits = completedHabits,
                totalHabits = totalHabits
            )

            // tips section
            FlashcardTipsSection()
        }
    }
}

@Composable
fun QuoteSelectorCard(
    quotes: List<MotivationItem>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    selectedQuote: MotivationItem?,
    onSave: () -> Unit,
    onOpenFavourites: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Choose Your Quote of the Day",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A5633)
            )

            Spacer(Modifier.height(12.dp))

            // loop through quotes and display them
            quotes.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(index) },
                    colors = CardDefaults.cardColors(
                        if (isSelected) Color(0xFFFFF4CE) else Color.White
                    ),
                    border = if (isSelected)
                        BorderStroke(2.dp, Color(0xFFC8A26A))
                    else null,
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(
                        if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("\"${item.quote}\"", color = Color(0xFF4A3928))
                        Text(item.author, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // save button
            Button(
                onClick = onSave,
                enabled = selectedQuote != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedQuote != null) Color(0xFFE7C15A)
                        else Color(0xFFE7D9A8),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Save Selected Quote to Favourites")
            }

            OutlinedButton(
                onClick = onOpenFavourites,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Favourites", color = Color(0xFF7A5633))
            }
        }
    }
}

@Composable
fun DailyChecklistCard(
    habits: List<String>,
    habitStatus: Map<String, Boolean>,
    onHabitToggle: (String) -> Unit,
    completedHabits: Int,
    totalHabits: Int
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Daily Study Checklist",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A5633)
            )

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = completedHabits / totalHabits.toFloat(),
                color = Color(0xFFE7C15A),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "$completedHabits / $totalHabits completed",
                color = Color(0xFF4A3928)
            )

            Spacer(Modifier.height(12.dp))

            // Checkbox list
            habits.forEach { habit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHabitToggle(habit) }
                ) {
                    Checkbox(
                        checked = habitStatus[habit] ?: false,
                        onCheckedChange = { onHabitToggle(habit) }
                    )
                    Text(habit, color = Color(0xFF4A3928))
                }
            }
        }
    }
}

@Composable
fun FlashcardTipsSection() {

    val flashcards = listOf(
        "Active Recall" to "Test yourself instead of rereading notes.",
        "Pomodoro Technique" to "25 minutes focus, 5 minutes break.",
        "Spaced Repetition" to "Review content on increasing intervals.",
        "Avoid Distractions" to "Put your phone away while studying."
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        flashcards.forEach { (title, desc) ->
            FlashcardTip(title, desc)
        }
    }
}

@Composable
fun FlashcardTip(title: String, desc: String) {

    // state to track if card is flipped
    var flipped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { flipped = !flipped },
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // flip logic
            if (!flipped) {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF7A5633))
                Text("Tap to reveal →", color = Color.Gray)
            } else {
                Text(desc, color = Color(0xFF4A3928))
                Text("Tap to hide ↑", color = Color.Gray)
            }
        }
    }
}