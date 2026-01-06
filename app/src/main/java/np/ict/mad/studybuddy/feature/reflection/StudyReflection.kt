package np.ict.mad.studybuddy.feature.reflection

import com.google.firebase.Timestamp

// format of study reflection; same for when saving to firebase
data class StudyReflection (
    val uid: String = "",
    val subject: String = "",
    val reflection: String = "",
    val mood: String = "",
    val createdAt: Timestamp = Timestamp.now()
)