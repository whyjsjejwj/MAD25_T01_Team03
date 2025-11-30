package np.ict.mad.studybuddy.feature.quiz

data class QuizQuestionModel(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: Int = 0,
    val subject: String = ""
)

data class WrongAnswers(
    val question: String,
    val correctAnswer: String,
    val chosenAnswer: String
)

val sampleQuestions = listOf(
    QuizQuestionModel(
        "What is 2 + 2?",
        listOf("1", "2", "3", "4"),
        3,
        subject = "math"
    ),
    QuizQuestionModel(
        "Capital of France?",
        listOf("Paris", "London", "Bangkok", "Tokyo"),
        0,
        subject = "geography"
    ),
    QuizQuestionModel(
        "Capital of South Korea?",
        listOf("Busan", "Incheon", "Daejeon", "Seoul"),
        3,
        subject = "geography"
    ),
    QuizQuestionModel(
        "What is it called when plants take in oxygen and give out carbon dioxide?",
        listOf("Photosynthesis", "Respiration", "Breathing", "Vapourising"),
        0,
        "biology"
    ),
    QuizQuestionModel(
        "What is the probability of selecting a red ball when there are 2 blue and 3 red balls in a bag?",
        listOf("20%", "40%", "60%", "80%"),
        2,
        "math"
    )
)