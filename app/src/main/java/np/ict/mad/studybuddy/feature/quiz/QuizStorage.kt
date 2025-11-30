package np.ict.mad.studybuddy.feature.quiz


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import np.ict.mad.studybuddy.feature.quiz.QuizQuestionModel

class QuizStorage {

    private val db = FirebaseFirestore.getInstance()

    fun getQuestions(subject: String, onResult: (List<QuizQuestionModel>) -> Unit) {
        db.collection("quizQuestions")
            .whereEqualTo("subject", subject)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects<QuizQuestionModel>()
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getAllQuestions(onResult: (List<QuizQuestionModel>) -> Unit) {
        db.collection("quizQuestions")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects<QuizQuestionModel>()
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}