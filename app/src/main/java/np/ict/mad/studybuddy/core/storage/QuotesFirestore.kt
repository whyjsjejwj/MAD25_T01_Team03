package np.ict.mad.studybuddy.core.storage

import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.studybuddy.feature.motivation.MotivationItem

class QuotesFirestore {

    private val db = FirebaseFirestore.getInstance()

    fun getQuotes(onResult: (List<MotivationItem>) -> Unit) {
        db.collection("quotes")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val quote = doc.getString("quote")
                    val author = doc.getString("author") ?: ""
                    if (quote != null) MotivationItem(quote, author) else null
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
