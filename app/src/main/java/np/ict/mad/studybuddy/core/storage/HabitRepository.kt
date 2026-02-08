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
// data class created to match firestore fields exactly
// default values are needed so firebase can convert the document back to an object automatically
data class DailyHabitLog(
    val date: String = "",
    val completedCount: Int = 0,
    val habits: Map<String, Boolean> = emptyMap(),
    // added these new fields for stage 2 features (mood tracking and diary)
    val mood: String = "",
    val diary: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

class HabitRepository {
    // section database setup
    // gets the instance of firestore to communicate with the cloud database
    private val db = FirebaseFirestore.getInstance()

    // section save checkboxes
    fun saveDailyProgress(uid: String, completedCount: Int, habits: Map<String, Boolean>) {
        // generates today's date (e.g. 2024-02-08) to use as the unique document id
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val log = hashMapOf(
            "date" to today,
            "completedCount" to completedCount,
            "habits" to habits,
            "timestamp" to Timestamp.now()
        )

        // essential: uses setoptions.merge() here so as to update the checkboxes without
        // accidentally deleting any existing mood or diary entries for the day
        db.collection("users").document(uid)
            .collection("habit_logs")
            .document(today)
            .set(log, SetOptions.merge())
    }

    // section fetch history
    // gets the last 40 days of logs to populate the calendar and analytics
    // suspend function used here so the database call happens in the background
    // prevents freezing the app ui while waiting for response
    suspend fun getHabitHistory(uid: String): List<DailyHabitLog> {
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("habit_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING) // sorting to show newest first
                .limit(40) // limiting to 40 to save data usage
                .get()
                .await()

            // helper function converts raw firebase documents directly into dailyhabitlog objects
            snapshot.toObjects(DailyHabitLog::class.java)
        } catch (e: Exception) {
            // returns empty list if something fails so the app doesn't crash
            emptyList()
        }
    }

    // section load today's data
    // checks if the user already ticked some boxes today to restore the state
    suspend fun getTodayHabits(uid: String): Map<String, Boolean>? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("habit_logs")
                .document(today)
                .get()
                .await()

            if (snapshot.exists()) {
                // if data exists, casts the 'habits' field back to a map
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

        // similar to above, merge is critical here to ensure the mood is saved
        // without wiping out the checkbox progress made earlier
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