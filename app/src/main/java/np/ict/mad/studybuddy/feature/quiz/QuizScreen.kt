package np.ict.mad.studybuddy.feature.quiz


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
import kotlinx.coroutines.tasks.await
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
    // Firestore access class
    val quizDb = remember { QuizStorage() }

    // To retrieve user profile data
    val firestore = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }

    // Stores the education level from Firebase
    var educationLevel by remember { mutableStateOf<String?>(null) }

    // for loading when fetching the education level
    var eduLoading by remember { mutableStateOf(true) }

    // fetches education level when screen opens so the qns can be filtered based on education level
    LaunchedEffect(uid) {
        eduLoading = true
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            educationLevel = doc.getString("educationLevel") ?: "Unknown"
        } catch (e: Exception) {
            educationLevel = "Unknown"
        } finally {
            eduLoading = false
        }
    }

    // Controls which part of the quiz the user is at (Home, Category, Results)
    var stage by remember { mutableStateOf("home") }

    // Just shows the loading indicator when fetching from Firebase
    var loading by remember { mutableStateOf(false) }

    // The question the user is currently at
    var questionIndex by remember { mutableStateOf(0) }

    // Score user gets at the end of the quiz
    var score by remember { mutableStateOf(0) }

    // List of questions selected from the Firebase
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
                // To make the loading indicator in the center
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }

            // shows loading icon when waiting to fetch education level
            if (eduLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }

            val edu = educationLevel ?: "Unknown"
            val allowedSubjects = allowedSubjectsFor(edu)

            // disables quiz if user doesnt have education level
            if (allowedSubjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Quiz Locked", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Please set your Education Level in Profile first.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onOpenHome, modifier = Modifier.fillMaxWidth()) {
                            Text("Go Back")
                        }
                    }
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
                            quizDb.getAllQuestions(edu) { list ->
                                selectedQuestions = list.shuffled().take(3)
                                loading = false
                                stage = "question"
                                questionIndex = 0
                                score = 0
                                wrongAnswers = emptyList()
                            }
                        } else {
                            // load questions by subject
                            quizDb.getQuestions(subject, edu) { list ->
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

                        // only shows categories allowed for the education level
                        if ("math" in allowedSubjects) {
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory("math") }) {
                                Text("Math")
                            }
                        }

                        if ("biology" in allowedSubjects) {
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory("biology") }) {
                                Text("Biology")
                            }
                        }

                        if ("geography" in allowedSubjects) {
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { loadCategory("geography") }) {
                                Text("Geography")
                            }
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
                                        .heightIn(max = 350.dp)
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ){
                                    // Looping through each wrong answer and display a card for each question gotten wrong
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