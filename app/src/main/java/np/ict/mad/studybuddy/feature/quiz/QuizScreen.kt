package np.ict.mad.studybuddy.feature.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
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
    var stage by remember { mutableStateOf("home") }
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

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
        ) {

            when (stage) {

                // -------------------------
                // QUIZ HOME PAGE
                // -------------------------
                "home" -> {
                    Text("Quiz Zone", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(16.dp))
                    Text("Test your knowledge now!", style = MaterialTheme.typography.bodyLarge)

                    Spacer(Modifier.height(32.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedQuestions = sampleQuestions.shuffled().take(3)
                            stage = "question"
                            questionIndex = 0
                            score = 0
                        }
                    ) {
                        Text("Start Quiz")
                    }
                }

                // -------------------------
                // QUESTION PAGE
                // -------------------------
                "question" -> {
                    if (current != null) {

                        Text(
                            "Question ${questionIndex + 1} of ${selectedQuestions.size}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(16.dp))

                        Text(current.question, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(24.dp))

                        current.options.forEachIndexed { index, option ->

                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
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

                // -------------------------
                // RESULT PAGE
                // -------------------------
                "result" -> {
                    Text("Quiz Completed!", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Your Score: $score / ${selectedQuestions.size}",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { stage = "home" }
                    ) {
                        Text("Restart Quiz")
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenHome
                    ) {
                        Text("Return to Home")
                    }
                }
            }
        }
    }
}