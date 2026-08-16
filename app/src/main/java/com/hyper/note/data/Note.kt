package com.hyper.note.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val lastModified: Long = System.currentTimeMillis(),
    val tags: String = "", // Comma-separated
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val folderId: Int? = null
) {
    fun getTagsList(): List<String> {
        if (tags.isBlank()) return emptyList()
        return tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
