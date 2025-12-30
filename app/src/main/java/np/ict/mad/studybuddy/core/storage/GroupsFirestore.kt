package np.ict.mad.studybuddy.core.storage

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class GroupsFirestore {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun messagesCol(groupId: String) =
        db.collection("groups").document(groupId).collection("messages")

    /**
     * Upload a PDF to Storage under:
     * groups/{groupId}/files/{uid}/{timestamp}_{fileName}
     *
     * Returns Pair(downloadUrl, fileSizeBytes)
     */
    suspend fun uploadGroupPdf(
        groupId: String,
        uid: String,
        fileUri: Uri,
        fileName: String
    ): Pair<String, Long> {

        val safeName = fileName.ifBlank { "note.pdf" }
        val path = "groups/$groupId/files/$uid/${System.currentTimeMillis()}_$safeName"

        val ref = storage.reference.child(path)

        val uploadTask = ref.putFile(fileUri).await()
        val size = uploadTask.metadata?.sizeBytes ?: 0L
        val url = ref.downloadUrl.await().toString()

        return url to size
    }

    suspend fun sendTextMessage(
        groupId: String,
        senderUid: String,
        senderName: String,
        text: String
    ) {
        val msg = GroupMessage(
            senderUid = senderUid,
            senderName = senderName,
            type = "text",
            text = text,
            createdAt = System.currentTimeMillis()
        )
        messagesCol(groupId).add(msg).await()
    }

    suspend fun sendFileMessage(
        groupId: String,
        senderUid: String,
        senderName: String,
        fileName: String,
        fileUrl: String,
        fileSize: Long
    ) {
        val msg = GroupMessage(
            senderUid = senderUid,
            senderName = senderName,
            type = "file",
            fileName = fileName,
            fileUrl = fileUrl,
            fileType = "pdf",
            fileSize = fileSize,
            createdAt = System.currentTimeMillis()
        )
        messagesCol(groupId).add(msg).await()
    }
}
