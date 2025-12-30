package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class FirestoreNote(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // ✅ subject/category
    val categoryId: String = "",
    val categoryName: String = "Uncategorized"
)

data class NoteCategory(
    val id: String = "",
    val name: String = ""
)

class NotesFirestore {

    private val db = FirebaseFirestore.getInstance()

    private fun notesCollection(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("notes")

    private fun categoriesCollection(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("categories")

    // --------------------
    // NOTES
    // --------------------

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

    // --------------------
    // CATEGORIES (SUBJECTS)
    // --------------------

    suspend fun getCategories(uid: String): List<NoteCategory> {
        val snap = categoriesCollection(uid).get().await()
        return snap.documents.map { doc ->
            NoteCategory(
                id = doc.id,
                name = doc.getString("name") ?: "Unnamed"
            )
        }.sortedBy { it.name.lowercase() }
    }

    suspend fun addCategory(uid: String, name: String): NoteCategory {
        val clean = name.trim()
        require(clean.isNotBlank())

        // prevent duplicates (case-insensitive)
        val existing = getCategories(uid).firstOrNull { it.name.equals(clean, ignoreCase = true) }
        if (existing != null) return existing

        val id = UUID.randomUUID().toString()

        categoriesCollection(uid)
            .document(id)
            .set(mapOf("name" to clean))
            .await()

        return NoteCategory(id = id, name = clean)
    }

    /**
     * ✅ Delete a category and move any notes using it to "Uncategorized".
     */
    suspend fun deleteCategoryAndUnassignNotes(uid: String, categoryId: String) {
        require(categoryId.isNotBlank())

        // Update notes in chunks (batch limit ~500)
        while (true) {
            val snap = notesCollection(uid)
                .whereEqualTo("categoryId", categoryId)
                .limit(400)
                .get()
                .await()

            if (snap.isEmpty) break

            val batch = db.batch()
            snap.documents.forEach { doc ->
                batch.update(
                    doc.reference,
                    mapOf(
                        "categoryId" to "",
                        "categoryName" to "Uncategorized"
                    )
                )
            }
            batch.commit().await()
        }

        // Delete the category document
        categoriesCollection(uid)
            .document(categoryId)
            .delete()
            .await()
    }

    /**
     * ✅ FAST: rename only the category document
     * (use this for instant UI response)
     */
    suspend fun renameCategory(uid: String, categoryId: String, newName: String) {
        val clean = newName.trim()
        require(categoryId.isNotBlank())
        require(clean.isNotBlank())

        // prevent duplicates (case-insensitive), excluding itself
        val existing = getCategories(uid).firstOrNull {
            it.id != categoryId && it.name.equals(clean, ignoreCase = true)
        }
        if (existing != null) return

        categoriesCollection(uid)
            .document(categoryId)
            .update("name", clean)
            .await()
    }

    /**
     * ✅ SLOW: update categoryName in all notes that reference the categoryId
     * (run this in background)
     */
    suspend fun updateNotesCategoryName(uid: String, categoryId: String, newName: String) {
        val clean = newName.trim()
        require(categoryId.isNotBlank())
        require(clean.isNotBlank())

        while (true) {
            val snap = notesCollection(uid)
                .whereEqualTo("categoryId", categoryId)
                .limit(400)
                .get()
                .await()

            if (snap.isEmpty) break

            val batch = db.batch()
            snap.documents.forEach { doc ->
                batch.update(doc.reference, "categoryName", clean)
            }
            batch.commit().await()
        }
    }
}
