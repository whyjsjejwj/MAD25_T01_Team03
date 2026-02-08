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
// defines the three tiers and their hierarchy levels
// using an enum makes it easy to add more tiers later like 'platinum'
enum class UserTier(val level: Int, val label: String) {
    BRONZE(0, "Free"),
    SILVER(1, "Silver"),
    GOLD(2, "Gold");

    // checks if the current tier is high enough for a feature
    // logic: if my level (e.g. 2) is >= required level (e.g. 1), allow access
    fun hasAccess(required: UserTier): Boolean {
        return this.level >= required.level
    }
}

// section subscription logic
// singleton object used here so the subscription state is global
// ensures the same user tier is accessible from any screen in the app
object SubscriptionManager {
    private val db = FirebaseFirestore.getInstance()

    // holds current tier in a mutable state so the ui updates automatically
    // whenever the tier changes (e.g. from bronze to gold)
    var userTier by mutableStateOf(UserTier.BRONZE)
    var expiryDate: Date? = null

    // section check status
    // fetches user tier from firebase and strictly checks if the subscription date has expired
    suspend fun fetchUserSubscription(uid: String) {
        try {
            val snapshot = db.collection("users").document(uid).get().await()
            val tierName = snapshot.getString("tier") ?: "BRONZE"
            val expiryTimestamp = snapshot.getTimestamp("subscriptionExpiry")

            // logic: checks if there is an expiry date AND if that date is in the future
            if (expiryTimestamp != null && expiryTimestamp.toDate().after(Date())) {
                try {
                    // valid subscription found, updates local state
                    userTier = UserTier.valueOf(tierName)
                    expiryDate = expiryTimestamp.toDate()
                } catch (e: Exception) {
                    // fallback to bronze if data is corrupted
                    userTier = UserTier.BRONZE
                }
            } else {
                // crucial: resets to bronze if the subscription date has passed (expired)
                userTier = UserTier.BRONZE
                expiryDate = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // fails safe to bronze on error
            userTier = UserTier.BRONZE
        }
    }

    // section purchase plan
    // simulates a purchase by calculating a new date 30 days from now
    suspend fun purchaseSubscription(uid: String, tier: UserTier) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30) // adds 30 days to current time
        val newExpiry = calendar.time

        // prepares data to save to firebase
        val data = hashMapOf(
            "tier" to tier.name,
            "subscriptionExpiry" to newExpiry,
            "purchaseDate" to Date()
        )

        // uses merge to update subscription info without overwriting other user data
        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .await()

        // updates local app state immediately so user sees the upgrade instantly
        userTier = tier
        expiryDate = newExpiry
    }

    // section cancel plan
    // immediately downgrades user to bronze for demo purposes
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