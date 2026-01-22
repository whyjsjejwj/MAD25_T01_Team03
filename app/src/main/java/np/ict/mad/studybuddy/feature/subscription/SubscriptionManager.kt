package np.ict.mad.studybuddy.feature.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

enum class UserTier(val level: Int, val label: String) {
    BRONZE(0, "Free"),
    SILVER(1, "Silver"),
    GOLD(2, "Gold");

    fun hasAccess(required: UserTier): Boolean {
        return this.level >= required.level
    }
}

object SubscriptionManager {
    var userTier by mutableStateOf(UserTier.BRONZE)

    suspend fun simulatePayment(tier: UserTier): Boolean {
        delay(2000)
        userTier = tier
        return true
    }
}