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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationScreen(
    uid: String,
    displayName: String,
    email: String,
    onOpenHome: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenFavourites: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { MotivationStorage(context) }

    // Top 5 quotes
    val topQuotes = remember { QuotesData.quotes.take(5) }

    // ❗ Start with NO selection
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedQuote = selectedIndex?.let { topQuotes[it] }

    // Study tips
    val studyTips = listOf(
        "Use the Pomodoro technique: 25 minutes focus, 5 minutes break.",
        "Summarise what you learned in your own words.",
        "Test yourself instead of rereading notes.",
        "Remove distractions while studying.",
        "Plan tomorrow’s study tasks at the end of each day."
    )

    // Light cream background gradient
    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFFFFFDF7),
            Color(0xFFFFF7E8)
        )
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
                onOpenNotes = onOpenNotes,
                onOpenMotivation = onOpenMotivation
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --------------------------
            // 1) QUOTES SECTION
            // --------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Choose Your Quote of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5633)
                    )

                    topQuotes.forEachIndexed { index, item ->
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
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "\"${item.quote}\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF4A3928)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.author,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Text(
                        "My Quote of the Day:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A5633)
                    )

                    if (selectedQuote == null) {
                        Text(
                            text = "Tap a quote above to choose your quote of the day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "\"${selectedQuote.quote}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4A3928)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ---------- Save button with dynamic colour ----------
                    val isSelected = selectedQuote != null

                    Button(
                        onClick = {
                            if (selectedQuote != null) {
                                storage.saveFavourite(
                                    MotivationItem(
                                        quote = selectedQuote.quote,
                                        author = selectedQuote.author
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected)
                                Color(0xFFE7C980)   // gold when usable
                            else
                                Color(0xFFD6D6D6),  // grey when nothing selected
                            contentColor = Color.White
                        ),
                        enabled = isSelected
                    ) {
                        Text("Save Selected Quote to Favourites")
                    }

                    OutlinedButton(
                        onClick = onOpenFavourites,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF7A5633)
                        )
                    ) {
                        Text("View Favourites")
                    }
                }
            }

            // --------------------------
            // 2) STUDY TIPS
            // --------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Study Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5633)
                    )

                    studyTips.forEach { tip ->
                        Text(
                            "• $tip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A3928)
                        )
                    }
                }
            }

            // --------------------------
            // 3) CONSISTENT HABITS
            // --------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Build Consistent Study Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5633)
                    )

                    Text("Try to:", color = Color(0xFF4A3928))

                    Text("• Study at the same time daily.", color = Color(0xFF4A3928))
                    Text("• Set 1–3 realistic goals.", color = Color(0xFF4A3928))
                    Text("• Take short breaks instead of long ones.", color = Color(0xFF4A3928))
                    Text("• Review what you learned daily.", color = Color(0xFF4A3928))
                }
            }
        }
    }
}
