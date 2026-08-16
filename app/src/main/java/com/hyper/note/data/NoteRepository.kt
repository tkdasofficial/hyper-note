package com.hyper.note.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val folderDao: FolderDao
) {
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allFolders: Flow<List<Folder>> = folderDao.getAllFolders()

    fun getNotesByFolder(folderId: Int): Flow<List<Note>> = noteDao.getNotesByFolder(folderId)

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }

    fun getNoteById(id: Int): Flow<Note?> {
        return noteDao.getNoteById(id)
    }

    suspend fun insert(note: Note): Int {
        return noteDao.insert(note).toInt()
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun deleteById(id: Int) {
        noteDao.deleteById(id)
    }

    suspend fun deleteNotes(ids: List<Int>) {
        noteDao.deleteNotes(ids)
    }

    suspend fun updatePinnedStatus(ids: List<Int>, isPinned: Boolean) {
        noteDao.updatePinnedStatus(ids, isPinned)
    }

    suspend fun updateFolder(ids: List<Int>, folderId: Int?) {
        noteDao.updateFolder(ids, folderId)
    }
    
    // Tasks
    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTasks(ids: List<Int>) = taskDao.deleteTasks(ids)
    suspend fun updateTaskPinnedStatus(ids: List<Int>, isPinned: Boolean) = taskDao.updatePinnedStatus(ids, isPinned)
    
    // Folders
    suspend fun insertFolder(folder: Folder) = folderDao.insert(folder)
    suspend fun deleteFolder(id: Int) {
        folderDao.deleteById(id)
        noteDao.clearDeletedFolder(id)
    }
}
