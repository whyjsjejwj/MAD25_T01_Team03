package np.ict.mad.studybuddy.core.storage

data class GroupMessage(
    val senderUid: String = "",
    val senderName: String = "",
    val type: String = "text",      // "text" | "file"
    val text: String = "",

    // file payload
    val fileName: String = "",
    val fileUrl: String = "",
    val fileType: String = "",      // "pdf"
    val fileSize: Long = 0,

    val createdAt: Long = 0
)
