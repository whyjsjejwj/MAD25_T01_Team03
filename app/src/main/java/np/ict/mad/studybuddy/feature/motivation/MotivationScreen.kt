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
import kotlinx.coroutines.launch
import np.ict.mad.studybuddy.core.storage.MotivationFirestore
import np.ict.mad.studybuddy.core.storage.QuotesFirestore
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationScreen(
    uid: String,
    displayName: String,
    email: String,
    onOpenHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenFavourites: () -> Unit
) {
    val quotesDb = remember { QuotesFirestore() }
    val motivationDb = remember { MotivationFirestore() }
    val scope = rememberCoroutineScope()

    var quotes by remember { mutableStateOf<List<MotivationItem>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    var loadingQuotes by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ---------------------------
    // STEP 1: Load quotes first
    // ---------------------------
    LaunchedEffect(Unit) {
        quotesDb.getQuotes { list ->
            quotes = list.take(5)
            loadingQuotes = false

            // DO NOT auto-select saved index anymore
            selectedIndex = null
        }
    }


    val selectedQuote = selectedIndex?.let { idx ->
        quotes.getOrNull(idx)
    }

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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        if (loadingQuotes) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE7C980))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ====================================
            // QUOTES SECTION
            // ====================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "Choose Your Quote of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5633)
                    )

                    Spacer(Modifier.height(12.dp))

                    quotes.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIndex = index },
                            colors = CardDefaults.cardColors(
                                if (isSelected) Color(0xFFFFF4CE) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected)
                                BorderStroke(2.dp, Color(0xFFC8A26A))
                            else null,
                            elevation = CardDefaults.cardElevation(
                                if (isSelected) 4.dp else 1.dp
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "\"${item.quote}\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF4A3928)
                                )
                                Text(
                                    item.author,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "My Quote of the Day:",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A5633)
                    )

                    if (selectedQuote != null) {
                        Text(
                            "\"${selectedQuote.quote}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4A3928)
                        )
                    } else {
                        Text(
                            "Tap a quote above to choose your quote of the day.",
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    val hasSelection = selectedQuote != null

                    Button(
                        onClick = {
                            if (selectedQuote != null && selectedIndex != null) {
                                motivationDb.addFavourite(uid, selectedQuote)
                                motivationDb.saveSelectedIndex(uid, selectedIndex!!)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = hasSelection,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (hasSelection) Color(0xFFE7C15A)   // deeper warm yellow when active
                                else Color(0xFFE7D9A8),               // pale disabled yellow
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFEDE7C8),
                            disabledContentColor = Color(0xFFB7A87A)
                        )
                    ) {
                        Text("Save Selected Quote to Favourites")
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onOpenFavourites,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Favourites", color = Color(0xFF7A5633))
                    }
                }
            }

            // ====================================
            // STUDY TIPS
            // ====================================
            val studyTips = listOf(
                "Use the Pomodoro technique: 25 minutes focus, 5 minutes break.",
                "Summarise what you learned in your own words.",
                "Test yourself instead of rereading notes.",
                "Remove distractions while studying.",
                "Plan tomorrow's study tasks at the end of each day."
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "Study Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5633)
                    )

                    studyTips.forEach { tip ->
                        Text("• $tip", color = Color(0xFF4A3928))
                    }
                }
            }

            // ====================================
            // HABITS
            // ====================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Build Consistent Study Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5633)
                    )

                    listOf(
                        "Study at the same time daily.",
                        "Set 1–3 realistic goals each session.",
                        "Take short breaks instead of long ones.",
                        "Review what you learned daily."
                    ).forEach {
                        Text("• $it", color = Color(0xFF4A3928))
                    }
                }
            }
        }
    }
}
