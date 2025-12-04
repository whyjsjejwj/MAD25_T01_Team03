package np.ict.mad.studybuddy.feature.quiz

// The format of the Questions for the quiz which would be fetched from firebase
data class QuizQuestionModel(
    val question: String = "", // Has default values to prevent crashes
    val options: List<String> = emptyList(),
    val answer: Int = 0,
    val subject: String = ""
)

// To store the questions that the user got wrong, and show the correct answer
data class WrongAnswers(
    val question: String,
    val correctAnswer: String,
    val chosenAnswer: String
)