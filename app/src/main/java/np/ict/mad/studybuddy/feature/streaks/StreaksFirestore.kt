package np.ict.mad.studybuddy.feature.streaks

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

// holds the streak related info for the UI
data class StreakInfo(
    val studyStreak: Int, // the current streak count (e.g. 1 days, 2 days)
    val lastStudyDate: String // the last date the user submitted a reflection
)
class StreaksFirestore {

    private val db = FirebaseFirestore.getInstance() // the reference to firebase db

    // will return today's date as a string (using sg timezone)
    private fun todayString(): String{
        val sg = ZoneId.of("Asia/Singapore")
        return LocalDate.now(sg).toString()
    }

    // return yesterday's date as a string - used to see if the streak continues
    private fun yesterdayString(): String{
        val sg = ZoneId.of("Asia/Singapore")
        return LocalDate.now(sg).minusDays(1).toString()
    }

    // record the study activity for the user
    suspend fun recordStudyActivity(uid: String){
        // reference to the user document on firebase
        val userRef = db.collection("users").document(uid)

        val today = todayString() //todaydate
        val yesterday = yesterdayString() //yesterdaydate

        db.runTransaction { tx ->
            val snap = tx.get(userRef)

            // reads the existing streak data (if no data, sets the default values)
            val lastDate = snap.getString("lastStudyDate") ?: ""
            val currentStreak = (snap.getLong("studyStreak") ?: 0L).toInt()

            // determines the new streak (same day reflection submitted;unchanged,consecutive submission;change streak)
            val newStreak = when {
                lastDate == today -> maxOf(currentStreak,1) // to prevent streak from getting stuck at 0 even after reflection submitted
                lastDate == yesterday -> currentStreak + 1
                else -> 1
            }

            // merging streak data to the user document on firebase
            tx.set(
                userRef,
                mapOf(
                    "studyStreak" to newStreak,
                    "lastStudyDate" to today,
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            null
        }.await()
    }

    // retrieves current streak and last study date from user to display in home screen
    suspend fun getStreak(uid: String): StreakInfo {
        val snap = db.collection("users").document(uid).get().await()
        val streak = (snap.getLong("studyStreak") ?: 0L).toInt() // defaults to safe values if fields doesnt exist
        val lastDate = snap.getString("lastStudyDate") ?: "" // defaults to safe values if fields doesnt exist
        return StreakInfo(studyStreak = streak, lastStudyDate = lastDate)
    }

    // determines if user should see the streak warning message
    // Conditions: user has an active streak and last reflection submitted was yesterday (means today can continue streak)
    fun shouldShowStreakWarning(info: StreakInfo): Boolean {
        val today = todayString()
        val yesterday = yesterdayString()

        // Warns user only if streak is active AND last study was yesterday
        return info.studyStreak > 0 && info.lastStudyDate == yesterday
    }

    // the warning message that will appear if user doesnt do reflection
    fun streakWarningMessage(info: StreakInfo): String {
        return "Do a Study Reflection today to keep your ${info.studyStreak}-day streak!"
    }
}