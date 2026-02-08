package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import np.ict.mad.studybuddy.core.storage.DailyHabitLog
import np.ict.mad.studybuddy.feature.subscription.SubscriptionManager
import np.ict.mad.studybuddy.feature.subscription.UserTier
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MoodCalendarCard(
    history: List<DailyHabitLog>,
    onSaveMood: (String) -> Unit,
    onSaveDiary: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    // section subscription check
    // verifies if user has silver access to unlock the mood/diary features
    // if not silver, the ui will show lock icons or upgrade prompts
    val isSilver = SubscriptionManager.userTier.hasAccess(UserTier.SILVER)

    // section calendar setup
    val calendar = Calendar.getInstance()
    // gets today's date in string format to compare against the history logs later
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // calculates how many days are in the current month to build the grid correctly
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // resets calendar to day 1 to find out which day of the week the month starts on
    // e.g. if the 1st is a wednesday, we need empty spaces for mon/tue
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // section current state
    // tracks the mood selected for "today" specifically to update the ui instantly
    var todayMood by remember {
        mutableStateOf(history.find { it.date == todayStr }?.mood ?: "")
    }

    // controls the diary popup visibility
    var showDiaryDialog by remember { mutableStateOf(false) }

    // holds the data for the specific day clicked on the calendar
    // allows the popup to know which date's diary to load
    var selectedDateLog by remember { mutableStateOf<DailyHabitLog?>(null) }
    var selectedDateStr by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Mood & Diary Journal",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A5633),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            // section quick mood selector
            // displays the 5 emoji options for quick selection
            Text("How are you today?", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val moods = listOf("🔥", "😴", "😊", "🤯", "😎")
                moods.forEach { emoji ->
                    val isSelected = todayMood == emoji
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFFFE0B2) else Color(0xFFFAFAFA))
                            .border(1.dp, if (isSelected) Color(0xFFFB8C00) else Color.Transparent, CircleShape)
                            .clickable {
                                // checks subscription level before allowing selection
                                if (isSilver) {
                                    todayMood = emoji
                                    onSaveMood(emoji)
                                } else {
                                    // triggers the upgrade screen if user is on free tier
                                    onUpgrade()
                                }
                            }
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(16.dp))

            // section calendar header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))

            // section day headers
            // renders s m t w t f s
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    Text(it, modifier = Modifier.width(35.dp), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // section calendar grid logic
            // calculates empty cells needed before the 1st of the month
            val offset = firstDayOfWeek - 1
            val totalCells = offset + daysInMonth
            // calculates how many rows are needed to fit all days
            val rows = (totalCells / 7) + if (totalCells % 7 != 0) 1 else 0

            Column {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 7) {
                            // calculates the exact day index in the grid (0 to 41)
                            val dayIndex = (r * 7) + c
                            // converts grid index to actual day number by subtracting the empty offset
                            val dayNum = dayIndex - offset + 1

                            // checks if the calculated number is a valid day in this month
                            if (dayNum in 1..daysInMonth) {
                                // builds the date string for this cell to check against history
                                val cellCal = Calendar.getInstance()
                                cellCal.set(Calendar.DAY_OF_MONTH, dayNum)
                                val cellDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cellCal.time)

                                // finds if we have a log for this specific date
                                val log = history.find { it.date == cellDateStr }
                                val mood = log?.mood ?: ""
                                val isToday = cellDateStr == todayStr

                                Box(
                                    modifier = Modifier
                                        .size(35.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isToday) Color(0xFFE3F2FD) else Color.Transparent)
                                        .border(1.dp, if (isToday) Color(0xFF2196F3) else Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (isSilver) {
                                                selectedDateStr = cellDateStr
                                                selectedDateLog = log
                                                // opens diary popup
                                                showDiaryDialog = true
                                            } else {
                                                onUpgrade()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // logic: if mood exists show emoji, else show date number
                                    if (mood.isNotEmpty()) {
                                        Text(mood, fontSize = 16.sp)
                                    } else {
                                        Text("$dayNum", fontSize = 12.sp, color = if(isToday) Color.Black else Color.Gray)
                                    }

                                    // shows a small orange dot if there is a diary entry but no mood emoji
                                    if (mood.isEmpty() && !log?.diary.isNullOrEmpty()) {
                                        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp).size(4.dp).background(Color(0xFFEF6C00), CircleShape))
                                    }
                                }
                            } else {
                                // renders empty spacer for offset days (days before the 1st)
                                Spacer(Modifier.width(35.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            if (!isSilver) {
                Spacer(Modifier.height(8.dp))
                Text("Upgrade to Silver to unlock Diary", fontSize = 10.sp, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }

    // section diary popup
    if (showDiaryDialog) {
        val isToday = selectedDateStr == todayStr
        var tempDiary by remember { mutableStateOf(selectedDateLog?.diary ?: "") }

        Dialog(onDismissRequest = { showDiaryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if(isToday) "Write Today's Diary" else "Diary Entry",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.weight(1f))
                        Text(selectedDateStr, fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(Modifier.height(16.dp))

                    if (selectedDateLog?.mood?.isNotEmpty() == true) {
                        Text("Mood: ${selectedDateLog!!.mood}", fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                    }

                    // section read only check
                    // prevents editing if the selected date is not today
                    // this ensures users can't cheat by backdating entries
                    OutlinedTextField(
                        value = tempDiary,
                        onValueChange = { if (isToday) tempDiary = it },
                        label = { Text(if (isToday) "Dear Diary..." else "No entry recorded") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        readOnly = !isToday, // critical logic for read-only mode
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isToday) Color(0xFFEF6C00) else Color.Gray,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    if (isToday) {
                        Button(
                            onClick = {
                                onSaveDiary(tempDiary)
                                showDiaryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFEF6C00)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Entry")
                        }
                    } else {
                        // shows a locked button for past dates
                        OutlinedButton(
                            onClick = { showDiaryDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Read Only (Past Date)")
                        }
                    }
                }
            }
        }
    }
}