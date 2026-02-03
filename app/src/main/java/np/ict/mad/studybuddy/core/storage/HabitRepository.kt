package np.ict.mad.studybuddy.core.storage

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// section data model
// stores all daily info: checkboxes, mood emoji, and diary text
data class DailyHabitLog(
    val date: String = "",
    val completedCount: Int = 0,
    val habits: Map<String, Boolean> = emptyMap(),
    // new fields for stage 2 features
    val mood: String = "",
    val diary: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

class HabitRepository {
    // section database setup
    private val db = FirebaseFirestore.getInstance()

    // section save checkboxes
    fun saveDailyProgress(uid: String, completedCount: Int, habits: Map<String, Boolean>) {
        // get today's date to use as the document id
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val log = hashMapOf(
            "date" to today,
            "completedCount" to completedCount,
            "habits" to habits,
            "timestamp" to Timestamp.now()
        )

        // save to firebase users -> uid -> habit_logs -> date
        // use merge so we don't overwrite existing mood/diary data
        db.collection("users").document(uid)
            .collection("habit_logs")
            .document(today)
            .set(log, SetOptions.merge())
    }

    // section fetch history
    // gets past 40 days of logs for the calendar and analytics
    suspend fun getHabitHistory(uid: String): List<DailyHabitLog> {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("habit_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .await()

            // convert firebase documents directly to our data class
            snapshot.toObjects(DailyHabitLog::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // section load today's data
    // checks if we already have checkboxes saved for today
    suspend fun getTodayHabits(uid: String): Map<String, Boolean>? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("habit_logs")
                .document(today)
                .get()
                .await()

            if (snapshot.exists()) {
                // cast the database map back to kotlin map
                snapshot.get("habits") as? Map<String, Boolean>
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // section save mood
    fun saveMood(uid: String, mood: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val data = hashMapOf(
            "mood" to mood,
            "date" to today,
            "timestamp" to Timestamp.now()
        )

        // merge is important here to not delete checkbox progress
        db.collection("users").document(uid)
            .collection("habit_logs").document(today)
            .set(data, SetOptions.merge())
    }

    // section save diary
    fun saveDiary(uid: String, content: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val data = hashMapOf(
            "diary" to content,
            "date" to today,
            "timestamp" to Timestamp.now()
        )

        db.collection("users").document(uid)
            .collection("habit_logs").document(today)
            .set(data, SetOptions.merge())
    }
}