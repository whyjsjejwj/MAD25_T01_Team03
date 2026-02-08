package np.ict.mad.studybuddy.core.storage

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Handles all Firestore + Storage operations for group chats
 * (sending messages, uploading files, etc.)
 */
class GroupsFirestore {

    //Firestore database instance
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    /**
     * Helper function to get the messages collection for a group
     * Path: groups/{groupId}/messages
     */
    private fun messagesCol(groupId: String) =
        db.collection("groups").document(groupId).collection("messages")

    /**
     * Uploads a PDF file to Firebase Storage.
     *
     * Storage path format:
     * groups/{groupId}/files/{uid}/{timestamp}_{fileName}
     *
     * Returns:
     * - download URL (String)
     * - file size in bytes (Long)
     */
    suspend fun uploadGroupPdf(
        groupId: String,
        uid: String,
        fileUri: Uri,
        fileName: String
    ): Pair<String, Long> {

        //Ensure file name is not empty
        val safeName = fileName.ifBlank { "note.pdf" }

        //Create a unique storage path using timestamp
        val path = "groups/$groupId/files/$uid/${System.currentTimeMillis()}_$safeName"

        //Reference to Firebase Storage location
        val ref = storage.reference.child(path)

        //Upload the file and wait until it finishes
        val uploadTask = ref.putFile(fileUri).await()

        //Get uploaded file size
        val size = uploadTask.metadata?.sizeBytes ?: 0L

        //Get downloadable URL for the uploaded file
        val url = ref.downloadUrl.await().toString()

        //Return both URL and file size
        return url to size
    }

    /**
     * Sends a normal text message to a group chat.
     */
    suspend fun sendTextMessage(
        groupId: String,
        senderUid: String,
        senderName: String,
        text: String
    ) {
        //Create a message object
        val msg = GroupMessage(
            senderUid = senderUid,
            senderName = senderName,
            type = "text",
            text = text,
            createdAt = System.currentTimeMillis()
        )

        //Save message to Firestore
        messagesCol(groupId).add(msg).await()
    }

    /**
     * Sends a file message (PDF) to a group chat.
     */
    suspend fun sendFileMessage(
        groupId: String,
        senderUid: String,
        senderName: String,
        fileName: String,
        fileUrl: String,
        fileSize: Long
    ) {
        //Create a file message object
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

        //Save file message to Firestore
        messagesCol(groupId).add(msg).await()
    }
}
