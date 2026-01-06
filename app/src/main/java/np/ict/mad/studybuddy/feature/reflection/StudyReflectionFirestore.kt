package np.ict.mad.studybuddy.feature.reflection


import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Handles saving the study reflections to firebase

class StudyReflectionFirestore {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("studyReflections")

    suspend fun addReflection(reflection: StudyReflection) {
        collection.add(reflection).await()
    }
}
