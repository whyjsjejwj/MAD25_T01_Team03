package np.ict.mad.studybuddy.feature.streaks

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

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

    // retrieves current streak from user to display in home screen
    suspend fun getStreak(uid: String): Int {
        val snap = db.collection("users").document(uid).get().await()
        return (snap.getLong("studyStreak") ?: 0L).toInt()
    }
}