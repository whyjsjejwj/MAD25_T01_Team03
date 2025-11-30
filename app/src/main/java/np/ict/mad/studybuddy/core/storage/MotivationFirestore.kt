package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.studybuddy.feature.motivation.MotivationItem

class MotivationFirestore {

    private val db = FirebaseFirestore.getInstance()

    private fun userDoc(uid: String) =
        db.collection("users").document(uid)

    fun addFavourite(
        uid: String,
        item: MotivationItem,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val data = mapOf(
            "quote" to item.quote,
            "author" to item.author,
            "savedAt" to System.currentTimeMillis()
        )

        // 1) Check if the quote already exists
        userDoc(uid)
            .collection("motivationFavourites")
            .whereEqualTo("quote", item.quote)
            .whereEqualTo("author", item.author)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // Already saved — do not add
                    onComplete(true)
                    return@addOnSuccessListener
                }

                // 2) If not found, add as new favourite
                userDoc(uid)
                    .collection("motivationFavourites")
                    .add(data)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


    fun getFavourites(uid: String, onResult: (List<MotivationItem>) -> Unit) {
        userDoc(uid)
            .collection("motivationFavourites")
            .orderBy("savedAt")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val quote = doc.getString("quote")
                    val author = doc.getString("author") ?: ""
                    if (quote != null) MotivationItem(quote, author) else null
                }
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun saveSelectedIndex(uid: String, index: Int, onComplete: (Boolean) -> Unit = {}) {
        userDoc(uid)
            .collection("motivationMeta")
            .document("config")
            .set(mapOf("selectedQuoteIndex" to index))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeFavourite(uid: String, item: MotivationItem, onComplete: (Boolean) -> Unit = {}) {
        userDoc(uid)
            .collection("motivationFavourites")
            .whereEqualTo("quote", item.quote)
            .whereEqualTo("author", item.author)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

}
