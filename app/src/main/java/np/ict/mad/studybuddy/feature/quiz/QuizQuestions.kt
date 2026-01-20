package np.ict.mad.studybuddy.feature.quiz

// The format of the Questions for the quiz which would be fetched from firebase
data class QuizQuestionModel(
    val question: String = "", // Has default values to prevent crashes
    val options: List<String> = emptyList(),
    val answer: Int = 0,
    val subject: String = "",
    val educationLevels: List<String> = emptyList()
)

// To store the questions that the user got wrong, and show the correct answer
data class WrongAnswers(
    val question: String,
    val correctAnswer: String,
    val chosenAnswer: String
)

// Filter for subjects based on the education levels
fun allowedSubjectsFor(educationLevel: String): List<String> {
    return when (educationLevel) {
        "Primary 3",
        "Primary 4",
        "Primary 5",
        "Primary 6" -> listOf("math", "biology") // only these subjects are allowed for these education levels

        "Secondary 1",
        "Secondary 2",
        "Secondary 3",
        "Secondary 4" -> listOf("math", "biology", "geography")

        "JC 1",
        "JC 2" -> listOf("math", "biology", "geography")

        else -> emptyList() // makes it Unknown which will block the quiz
    }
}