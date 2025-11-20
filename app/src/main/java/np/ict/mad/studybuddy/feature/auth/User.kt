package np.ict.mad.studybuddy.feature.auth

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val password: String
)
