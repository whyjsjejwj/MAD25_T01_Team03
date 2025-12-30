package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserDirectoryProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = ""
)

class UserDirectoryFirestore {

    private val db = FirebaseFirestore.getInstance()

    private fun directory() = db.collection("userDirectory")

    /**
     * Call this after login/register to ensure the user is searchable.
     */
    suspend fun upsertProfile(uid: String, displayName: String, email: String) {
        val cleanName = displayName.trim()
        val cleanEmail = email.trim().lowercase()

        directory()
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "displayName" to cleanName,
                    "email" to cleanEmail
                )
            )
            .await()
    }

    /**
     * Search by email exact match (fast + reliable).
     */
    suspend fun findByEmail(email: String): List<UserDirectoryProfile> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return emptyList()

        val snap = directory()
            .whereEqualTo("email", cleanEmail)
            .limit(20)
            .get()
            .await()

        return snap.documents.mapNotNull { it.toObject(UserDirectoryProfile::class.java) }
    }

    /**
     * Search by displayName prefix (case-insensitive-ish).
     * Firestore doesn't support true case-insensitive contains, so we do prefix.
     */
    suspend fun findByNamePrefix(namePrefix: String): List<UserDirectoryProfile> {
        val q = namePrefix.trim()
        if (q.isBlank()) return emptyList()

        val end = q + "\uf8ff"

        val snap = directory()
            .orderBy("displayName")
            .startAt(q)
            .endAt(end)
            .limit(20)
            .get()
            .await()

        return snap.documents.mapNotNull { it.toObject(UserDirectoryProfile::class.java) }
    }
}
