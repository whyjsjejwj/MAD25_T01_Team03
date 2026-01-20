package np.ict.mad.studybuddy.feature.quiz


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import np.ict.mad.studybuddy.feature.quiz.QuizQuestionModel

// Handles fetching questions from Firebase Firestore to quiz
class QuizStorage {

    private val db = FirebaseFirestore.getInstance()

    // to fetch questions Firebase from that category
    fun getQuestions(subject: String, educationLevel: String, onResult: (List<QuizQuestionModel>) -> Unit) {
        db.collection("quizQuestions")
            .whereEqualTo("subject", subject) // Filter by subject (Math, Biology, etc)
            .whereArrayContains("educationLevels", educationLevel) // Filter by education Level (Secondary 1, Secondary 2, etc)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects<QuizQuestionModel>() //Converts the Firestore docs to Kotlin objects
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList()) // Returns empty list if something happens
            }
    }

    // to fetch questions from Firebase IF user chooses Mixed category (No filtering)
    fun getAllQuestions(educationLevel: String, onResult: (List<QuizQuestionModel>) -> Unit) {
        db.collection("quizQuestions")
            .whereArrayContains("educationLevels", educationLevel)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects<QuizQuestionModel>() //Converts the Firestore docs to Kotlin objects
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList()) // Returns empty list if something happens
            }
    }
}