package np.ict.mad.studybuddy.core.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class Note(
    val id: Int,
    var title: String,
    var content: String
)

data class UserNotes(
    val username: String,
    val notes: MutableList<Note>
)

data class NotesData(
    val users: MutableList<UserNotes>
)

class NotesStorage(private val context: Context) {

    private val fileName = "notes.json"

    // Load from internal storage (persistent)
    private fun loadFromStorage(): NotesData {
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            // Copy from assets on first run
            val assetJson = context.assets.open(fileName).bufferedReader().use { it.readText() }
            file.writeText(assetJson)
        }

        val json = file.readText()
        val type = object : TypeToken<NotesData>() {}.type

        return Gson().fromJson(json, type)
    }

    // Save entire JSON back to storage
    private fun saveToStorage(data: NotesData) {
        val file = File(context.filesDir, fileName)
        file.writeText(Gson().toJson(data))
    }

    // ===== PUBLIC API =====

    // Get notes of a user
    fun getUserNotes(username: String): MutableList<Note> {
        val data = loadFromStorage()
        return data.users.find { it.username == username }?.notes ?: mutableListOf()
    }

    // Add a new note
    fun addNote(username: String, title: String, content: String) {
        val data = loadFromStorage()
        val user = data.users.find { it.username == username }

        if (user != null) {
            val newId = (user.notes.maxOfOrNull { it.id } ?: 0) + 1
            user.notes.add(Note(newId, title, content))
        } else {
            data.users.add(
                UserNotes(
                    username,
                    mutableListOf(Note(1, title, content))
                )
            )
        }

        saveToStorage(data)
    }

    // Edit a note
    fun updateNote(username: String, noteId: Int, newTitle: String, newContent: String) {
        val data = loadFromStorage()
        val user = data.users.find { it.username == username }

        user?.notes?.find { it.id == noteId }?.apply {
            title = newTitle
            content = newContent
        }

        saveToStorage(data)
    }

    fun deleteNote(username: String, noteId: Int) {
        val data = loadFromStorage()
        val user = data.users.find { it.username == username }

        user?.notes?.removeIf { it.id == noteId }

        saveToStorage(data)
    }

}


