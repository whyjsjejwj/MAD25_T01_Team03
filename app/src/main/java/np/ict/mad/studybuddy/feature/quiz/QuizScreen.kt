package np.ict.mad.studybuddy.feature.quiz

import android.graphics.Color.red
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

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
        ) {
            if (loading){
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                return@Column
            }

            when (stage) {

                // -------------------------
                // QUIZ HOME PAGE
                // -------------------------
                "home" -> {
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
                        /*modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedQuestions = sampleQuestions.shuffled().take(3)
                            stage = "question"
                            questionIndex = 0
                            score = 0
                            /*quizDb.getQuizQuestions { questionsFromDb ->
                                selectedQuestions = questionsFromDb.shuffled().take(3)
                                stage = "question"
                                questionIndex = 0
                                score = 0*/
                        }
                    ) {
                        Text("Start Quiz")
                    }*/
                }
                //------------------------------------------------
                // CATEGORY SELECTION PAGE
                //------------------------------------------------
                "category" -> {
                    Text("Choose Category", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(24.dp))

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


                    Spacer(Modifier.height(12.dp))

                    // Biology
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { loadCategory("math") }
                    ) { Text("Math") }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { loadCategory("biology") }
                    ) { Text("Biology") }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { loadCategory("geography") }
                    ) { Text("Geography") }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { loadCategory(null) }
                    ) { Text("Mixed") }

                    Spacer(Modifier.height(30.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { stage = "home" },
                    ) {
                        Text("Back")
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