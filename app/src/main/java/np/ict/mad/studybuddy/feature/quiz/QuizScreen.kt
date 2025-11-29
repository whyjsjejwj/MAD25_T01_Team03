package np.ict.mad.studybuddy.feature.quiz

import android.R
import android.graphics.Color.red
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import np.ict.mad.studybuddy.feature.home.BottomNavBar
import np.ict.mad.studybuddy.feature.home.BottomNavTab

@Composable
fun QuizScreen(
    uid: String,
    displayName: String,
    email: String,
    onOpenHome: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenQuiz: () -> Unit,
) {
    val quizDb = remember { QuizStorage() }

    var stage by remember { mutableStateOf("home") }
    var loading by remember { mutableStateOf(false) }
    var questionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }

    var selectedQuestions by remember { mutableStateOf(listOf<QuizQuestionModel>()) }
    /*val questions = sampleQuestions*/
    val current = selectedQuestions.getOrNull(questionIndex)

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.QUIZ,
                onHome = onOpenHome,
                onOpenTimer = onOpenTimer,
                onOpenMotivation = onOpenMotivation,
                onOpenQuiz = onOpenQuiz
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            if (loading){
                // Center the loading indicator
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }

            when (stage) {

                // -------------------------
                // QUIZ HOME PAGE
                // -------------------------
                "home" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Quiz Zone", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(16.dp))
                            Text("Choose a quiz category.", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(32.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { stage = "category" }
                            ) {
                                Text("Start")
                            }
                        }
                    }
                }
                //------------------------------------------------
                // CATEGORY SELECTION PAGE
                //------------------------------------------------
                "category" -> {

                    fun loadCategory(subject: String?) {
                        loading = true
                        if (subject == null) {
                            // Load ALL questions
                            quizDb.getAllQuestions { list ->
                                selectedQuestions = list.shuffled().take(3)
                                loading = false
                                stage = "question"
                                questionIndex = 0
                                score = 0
                            }
                        } else {
                            quizDb.getQuestions(subject) { list ->
                                selectedQuestions = list.shuffled().take(3)
                                loading = false
                                stage = "question"
                                questionIndex = 0
                                score = 0
                            }
                        }
                    }


                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Choose Category", style = MaterialTheme.typography.headlineLarge)

                        Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory("math") }) {
                            Text("Math")
                        }

                        Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory("biology") }) {
                            Text("Biology")
                        }

                        Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory("geography") }) {
                            Text("Geography")
                        }

                        Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory(null) }) {
                            Text("Mixed")
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(modifier = Modifier.fillMaxWidth(), onClick = { stage = "home" }) {
                            Text("Back")
                        }
                    }
                }

                // -------------------------
                // QUESTION PAGE
                // -------------------------
                "question" -> {
                    if (current != null) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Question ${questionIndex + 1} of ${selectedQuestions.size}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(current.question, style = MaterialTheme.typography.bodyLarge)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                current.options.forEachIndexed { index, option ->
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            if (index == current.answer) score++
                                            if (questionIndex + 1 < selectedQuestions.size) {
                                                questionIndex++
                                            } else {
                                                stage = "result"
                                            }
                                        }
                                    ) {
                                        Text(option)
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------
                // RESULT PAGE
                // -------------------------
                "result" -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val resultMessage: String
                            val resultColor: Color

                            when {
                                score == selectedQuestions.size -> {
                                    // Full marks
                                    resultMessage = "Congratulations! You aced this!"
                                    resultColor = Color(0xFF4CAF50) // Green
                                }
                                score >= selectedQuestions.size / 2.0 -> {
                                    // Passed but not full marks
                                    resultMessage = "Keep trying, you've got this!"
                                    resultColor = Color(0xFF4CAF50)
                                }
                                else -> {
                                    // Failed
                                    resultMessage = "Try harder next time"
                                    resultColor = Color.Red
                                }
                            }
                            Text("Quiz Completed!", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                "Your Score: $score / ${selectedQuestions.size}",
                                style = MaterialTheme.typography.titleLarge,
                                color = resultColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                resultMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = resultColor
                            )

                            Button(modifier = Modifier.fillMaxWidth(), onClick = { stage = "home" }) {
                                Text("Restart Quiz")
                            }

                            Button(modifier = Modifier.fillMaxWidth(), onClick = onOpenHome) {
                                Text("Return to Home")
                            }
                        }
                    }
                }
            }
        }
    }
}