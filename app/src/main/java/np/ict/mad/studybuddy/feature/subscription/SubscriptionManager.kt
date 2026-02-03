package np.ict.mad.studybuddy.feature.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

// section subscription levels
// define the three tiers and their hierarchy
enum class UserTier(val level: Int, val label: String) {
    BRONZE(0, "Free"),
    SILVER(1, "Silver"),
    GOLD(2, "Gold");

    // checks if the current tier is high enough for a feature
    fun hasAccess(required: UserTier): Boolean {
        return this.level >= required.level
    }
}

// section subscription logic
// singleton object to handle all subscription related tasks
object SubscriptionManager {
    private val db = FirebaseFirestore.getInstance()

    // holds the current status so ui can react to changes
    var userTier by mutableStateOf(UserTier.BRONZE)
    var expiryDate: Date? = null

    // section check status
    // fetches the user's tier from firebase and checks if it has expired
    suspend fun fetchUserSubscription(uid: String) {
        try {
            val snapshot = db.collection("users").document(uid).get().await()
            val tierName = snapshot.getString("tier") ?: "BRONZE"
            val expiryTimestamp = snapshot.getTimestamp("subscriptionExpiry")

            // check if there is an expiry date and if it is in the future
            if (expiryTimestamp != null && expiryTimestamp.toDate().after(Date())) {
                try {
                    // valid subscription found update local state
                    userTier = UserTier.valueOf(tierName)
                    expiryDate = expiryTimestamp.toDate()
                } catch (e: Exception) {
                    // fallback to bronze if data is corrupted
                    userTier = UserTier.BRONZE
                }
            } else {
                // subscription expired or doesnt exist reset to bronze
                userTier = UserTier.BRONZE
                expiryDate = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            userTier = UserTier.BRONZE
        }
    }

    // section purchase plan
    // simulates a purchase by setting the tier and giving 30 days access
    suspend fun purchaseSubscription(uid: String, tier: UserTier) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val newExpiry = calendar.time

        // prepare data to save to firebase
        val data = hashMapOf(
            "tier" to tier.name,
            "subscriptionExpiry" to newExpiry,
            "purchaseDate" to Date()
        )

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .await()

        // update local app state immediately
        userTier = tier
        expiryDate = newExpiry
    }

    // section cancel plan
    // downgrades the user back to bronze immediately
    suspend fun cancelSubscription(uid: String) {
        val data = hashMapOf(
            "tier" to "BRONZE",
            "subscriptionExpiry" to null
        )

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .await()

        userTier = UserTier.BRONZE
        expiryDate = null
    }
}