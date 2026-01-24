package np.ict.mad.studybuddy.feature.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

enum class UserTier(val level: Int, val label: String) {
    BRONZE(0, "Free"),
    SILVER(1, "Silver"),
    GOLD(2, "Gold");

    fun hasAccess(required: UserTier): Boolean {
        return this.level >= required.level
    }
}

object SubscriptionManager {
    private val db = FirebaseFirestore.getInstance()

    var userTier by mutableStateOf(UserTier.BRONZE)
    var expiryDate: Date? = null

    suspend fun fetchUserSubscription(uid: String) {
        try {
            val snapshot = db.collection("users").document(uid).get().await()
            val tierName = snapshot.getString("tier") ?: "BRONZE"
            val expiryTimestamp = snapshot.getTimestamp("subscriptionExpiry")

            if (expiryTimestamp != null && expiryTimestamp.toDate().after(Date())) {
                try {
                    userTier = UserTier.valueOf(tierName)
                    expiryDate = expiryTimestamp.toDate()
                } catch (e: Exception) {
                    userTier = UserTier.BRONZE
                }
            } else {
                userTier = UserTier.BRONZE
                expiryDate = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            userTier = UserTier.BRONZE
        }
    }

    suspend fun purchaseSubscription(uid: String, tier: UserTier) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val newExpiry = calendar.time

        val data = hashMapOf(
            "tier" to tier.name,
            "subscriptionExpiry" to newExpiry,
            "purchaseDate" to Date()
        )

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .await()

        userTier = tier
        expiryDate = newExpiry
    }

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