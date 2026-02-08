package np.ict.mad.studybuddy.core.storage

/**
 * Represents a single message in a group or direct chat.
 * - normal text messages
 * - file messages (PDF)
 */
data class GroupMessage(
    //UID of the user who sent the message
    val senderUid: String = "",

    //Display name of the sender (shown in UI)
    val senderName: String = "",

    // Message type:
    // "text" = normal chat message
    // "file" = file attachment (PDF)
    val type: String = "text",      // "text" | "file"

    //Text content (used when type == "text")
    val text: String = "",

    // ---------- File message fields ----------
    // These fields are used ONLY when type == "file"

    // Original file name
    val fileName: String = "",

    //Download URL from Firebase Storage
    val fileUrl: String = "",

    //File type (currently only "pdf")
    val fileType: String = "",      // "pdf"

    //File size in bytes
    val fileSize: Long = 0,

    //Timestamp when the message was created
    val createdAt: Long = 0
)
