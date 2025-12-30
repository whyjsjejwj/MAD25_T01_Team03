package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DirectChatManager {

    private val db = FirebaseFirestore.getInstance()

    private fun groups() = db.collection("groups")

    private fun directId(uidA: String, uidB: String): String =
        listOf(uidA, uidB).sorted().joinToString("_")

    suspend fun createOrOpenDirectChat(
        myUid: String,
        otherUid: String
    ): String {
        require(myUid.isNotBlank() && otherUid.isNotBlank())
        require(myUid != otherUid)

        val gid = directId(myUid, otherUid)
        val ref = groups().document(gid)

        val existing = ref.get().await()
        if (existing.exists()) return gid

        val now = System.currentTimeMillis()

        // Must satisfy your existing group create rules:
        // createdBy == request.auth.uid
        // creator in members
        // joinCode is string
        ref.set(
            mapOf(
                "name" to "Direct Chat",
                "createdBy" to myUid,
                "createdAt" to now,
                "joinCode" to "DM",
                "members" to listOf(myUid, otherUid),
                "isDirect" to true,
                "directKey" to gid
            )
        ).await()

        return gid
    }
}
