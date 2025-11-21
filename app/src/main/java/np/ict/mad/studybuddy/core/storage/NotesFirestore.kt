package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class FirestoreNote(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class NotesFirestore {

    private val db = FirebaseFirestore.getInstance()

    private fun notesCollection(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("notes")

    suspend fun getNotes(uid: String): List<FirestoreNote> {
        val snapshot = notesCollection(uid)
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.toObjects(FirestoreNote::class.java)
    }

    suspend fun getNote(uid: String, noteId: Int): FirestoreNote? {
        val doc = notesCollection(uid)
            .document(noteId.toString())
            .get()
            .await()

        return doc.toObject(FirestoreNote::class.java)
    }

    suspend fun addNote(uid: String, note: FirestoreNote) {
        val finalNote = note.copy(createdAt = System.currentTimeMillis())

        notesCollection(uid)
            .document(finalNote.id.toString())
            .set(finalNote)
            .await()
    }

    suspend fun updateNote(uid: String, note: FirestoreNote) {
        val docRef = notesCollection(uid).document(note.id.toString())
        val existing = docRef.get().await().toObject(FirestoreNote::class.java)

        val finalNote = note.copy(
            createdAt = existing?.createdAt ?: note.createdAt
        )

        docRef.set(finalNote).await()
    }

    suspend fun deleteNote(uid: String, noteId: Int) {
        notesCollection(uid)
            .document(noteId.toString())
            .delete()
            .await()
    }
}
