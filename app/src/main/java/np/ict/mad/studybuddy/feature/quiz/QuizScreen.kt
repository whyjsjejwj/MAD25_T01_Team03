package np.ict.mad.studybuddy.feature.quiz

import android.R
import android.graphics.Color.red
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.max
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

    var selectedQuestions by remember { mutableStateOf(listOf<QuizQuestionModel>()) } // questions selected for the quiz
    val current = selectedQuestions.getOrNull(questionIndex) // current question
    var wrongAnswers by remember { mutableStateOf(listOf<WrongAnswers>()) } // questions the user got wrong

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
            // for loading and to show the loading icon
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
                    // to load questions for the category or mixed
                    fun loadCategory(subject: String?) {
                        loading = true // shows the loading icon
                        if (subject == null) {
                            // Load ALL questions
                            quizDb.getAllQuestions { list ->
                                selectedQuestions = list.shuffled().take(3)
                                loading = false
                                stage = "question"
                                questionIndex = 0
                                score = 0
                                wrongAnswers = emptyList()
                            }
                        } else {
                            // load questions by subject
                            quizDb.getQuestions(subject) { list ->
                                selectedQuestions = list.shuffled().take(3)
                                loading = false
                                stage = "question"
                                questionIndex = 0
                                score = 0
                                wrongAnswers = emptyList()
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
                            // question header - shows the current qn and how many questions in total
                            Text(
                                "Question ${questionIndex + 1} of ${selectedQuestions.size}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(current.question, style = MaterialTheme.typography.bodyLarge)

                            // for the options
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                current.options.forEachIndexed { index, option ->
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            // checking if selected option is correct
                                            if (index == current.answer)
                                            {
                                                score++ // add to score if correct
                                            } else {
                                                // else add question to wrong answer list
                                                wrongAnswers = wrongAnswers + WrongAnswers(
                                                    question = current.question,
                                                    correctAnswer = current.options[current.answer],
                                                    chosenAnswer = current.options[index]
                                                )
                                            }
                                            // move to next question or the result page if final qn
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
                            // to determine message AND message colour based on score
                            val resultMessage: String
                            val resultColour: Color

                            when {
                                score == selectedQuestions.size -> {
                                    // Full marks
                                    resultMessage = "Congratulations! You aced this!"
                                    resultColour = Color(0xFF4CAF50) // Green
                                }
                                score >= selectedQuestions.size / 2.0 -> {
                                    // Passed but not full marks
                                    resultMessage = "Keep trying, you've got this!"
                                    resultColour = Color(0xFF4CAF50)
                                }
                                else -> {
                                    // Failed
                                    resultMessage = "Try harder next time"
                                    resultColour = Color.Red
                                }
                            }
                            // to display score and results message
                            Text("Quiz Completed!", style = MaterialTheme.typography.headlineLarge)
                            Text(
                                "Your Score: $score / ${selectedQuestions.size}",
                                style = MaterialTheme.typography.titleLarge,
                                color = resultColour
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                resultMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = resultColour
                            )

                            // asking user if they would like to restart quiz (brings them back to category selection again) or go home page
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { stage = "home" }) {
                                Text("Restart Quiz")
                            }

                            Button(modifier = Modifier.fillMaxWidth(), onClick = onOpenHome) {
                                Text("Return to Home")
                            }

                            // to display wrong answers (if any)
                            if (wrongAnswers.isNotEmpty()) {

                                Spacer(Modifier.height(32.dp))

                                Text(
                                    "Questions you answered incorrectly:",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Spacer(Modifier.height(12.dp))

                                // to make it scrollable for users to view it easily
                                val scrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 300.dp)
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ){
                                    wrongAnswers.forEach { w ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFF7F7F7)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {

                                                Text(
                                                    text = "Question:",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    w.question,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                Spacer(Modifier.height(12.dp))

                                                Text(
                                                    text = "Your Answer:",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    text = w.chosenAnswer,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.Red
                                                )


                                                Spacer(Modifier.height(12.dp))

                                                Text(
                                                    text = "Correct Answer:",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    w.correctAnswer,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}