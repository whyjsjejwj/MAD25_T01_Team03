package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Manages creation and retrieval of 1-to-1 (direct) chats.
 * Ensures there is ONLY one direct chat between any two users.
 */
class DirectChatManager {

    //Firestore database instance
    private val db = FirebaseFirestore.getInstance()

    /**
     * Shortcut to the "groups" collection in Firestore
     */
    private fun groups() = db.collection("groups")

    /**
     * Generates a unique, deterministic ID for a direct chat
     * between two users.
     * Example:
     * uidA = "user1", uidB = "user2"
     * Result = "user1_user2"
     * Sorting ensures:
     * directId(A, B) == directId(B, A)
     */
    private fun directId(uidA: String, uidB: String): String =
        listOf(uidA, uidB).sorted().joinToString("_")

    /**
     * Creates a new direct chat OR opens an existing one
     * between the current user and another user.
     * Returns:
     * - groupId of the direct chat
     */
    suspend fun createOrOpenDirectChat(
        myUid: String,
        otherUid: String
    ): String {

        //Basic safety checks
        require(myUid.isNotBlank() && otherUid.isNotBlank())
        require(myUid != otherUid)

        val gid = directId(myUid, otherUid)

        //Reference to the group document
        val ref = groups().document(gid)

        //Check if the direct chat already exists
        val existing = ref.get().await()
        if (existing.exists())
        // If it exists, just return its ID
        return gid

        val now = System.currentTimeMillis()

        /**
         * Create a new direct chat group
         * This follows the same structure as normal groups,
         * but with isDirect = true
         */
        ref.set(
            mapOf(
                "name" to "Direct Chat",
                "createdBy" to myUid,
                "createdAt" to now,
                "joinCode" to "DM", // not used for joining
                "members" to listOf(myUid, otherUid),
                "isDirect" to true, // marks this as a DM
                "directKey" to gid // helps identify this chat
            )
        ).await()

        //Return the new group's ID
        return gid
    }
}
