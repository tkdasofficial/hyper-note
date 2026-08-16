package com.hyper.note.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, lastModified DESC")
    fun getActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY id ASC")
    suspend fun getAllNotesSync(): List<Note>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND folderId = :folderId ORDER BY isPinned DESC, lastModified DESC")
    fun getNotesByFolder(folderId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY lastModified DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastModified DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<Int>)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id IN (:ids)")
    suspend fun updatePinnedStatus(ids: List<Int>, isPinned: Boolean)

    @Query("UPDATE notes SET folderId = :folderId WHERE id IN (:ids)")
    suspend fun updateFolder(ids: List<Int>, folderId: Int?)

    @Query("UPDATE notes SET folderId = NULL WHERE folderId = :deletedFolderId")
    suspend fun clearDeletedFolder(deletedFolderId: Int)
}
