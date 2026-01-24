package np.ict.mad.studybuddy.core.storage

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyHabitLog(
    val date: String = "",
    val completedCount: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)

class HabitRepository {
    private val db = FirebaseFirestore.getInstance()

    fun saveDailyProgress(uid: String, completedCount: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val log = DailyHabitLog(
            date = today,
            completedCount = completedCount,
            timestamp = Timestamp.now()
        )

        db.collection("users").document(uid)
            .collection("habit_logs")
            .document(today)
            .set(log)
    }

    suspend fun getHabitHistory(uid: String): List<DailyHabitLog> {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("habit_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()

            snapshot.toObjects(DailyHabitLog::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}