package np.ict.mad.studybuddy.feature.quiz

data class QuizQuestionModel(
    val question: String,
    val options: List<String>,
    val answer: Int
)

val sampleQuestions = listOf(
    QuizQuestionModel(
        "What is 2 + 2?",
        listOf("1", "2", "3", "4"),
        3
    ),
    QuizQuestionModel(
        "Capital of France?",
        listOf("Paris", "London", "Bangkok", "Tokyo"),
        0
    ),
    QuizQuestionModel(
        "Which planet is known as the Red Planet?",
        listOf("Earth", "Mars", "Venus", "Jupiter"),
        1
    ),
    QuizQuestionModel(
        "What is it called when plants take in oxygen and give out carbon dioxide?",
        listOf("Photosynthesis", "Respiration", "Breathing", "Vapourising"),
        0
    ),
    QuizQuestionModel(
        "What is the probability of selecting a red ball when there are 2 blue and 3 red balls in a bag?",
        listOf("20%", "40%", "60%", "80%"),
        2
    )
)