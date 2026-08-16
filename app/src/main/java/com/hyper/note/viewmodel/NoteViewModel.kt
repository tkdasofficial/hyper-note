package com.hyper.note.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyper.note.data.AppDatabase
import com.hyper.note.data.Folder
import com.hyper.note.data.Note
import com.hyper.note.data.NoteRepository
import com.hyper.note.data.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao(), database.taskDao(), database.folderDao())
    }

    val activeNotes: StateFlow<List<Note>> = repository.activeNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFolders: StateFlow<List<Folder>> = repository.allFolders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedFolderId = MutableStateFlow<Int?>(null)
    val selectedFolderId = _selectedFolderId.asStateFlow()

    fun setSelectedFolder(folderId: Int?) {
        _selectedFolderId.value = folderId
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.activeNotes else repository.searchNotes(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getNoteById(id: Int): StateFlow<Note?> {
        return repository.getNoteById(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun saveNote(note: Note, onSaved: (Int) -> Unit = {}) {
        viewModelScope.launch {
            if (note.id == 0) {
                val newId = repository.insert(note)
                onSaved(newId)
            } else {
                repository.update(note)
                onSaved(note.id)
            }
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(isPinned = !note.isPinned, lastModified = System.currentTimeMillis()))
        }
    }
    
    // Multi-select operations
    fun deleteNotes(ids: List<Int>) {
        viewModelScope.launch {
            repository.deleteNotes(ids)
        }
    }

    fun pinNotes(ids: List<Int>, pin: Boolean) {
        viewModelScope.launch {
            repository.updatePinnedStatus(ids, pin)
        }
    }

    fun moveNotesToFolder(ids: List<Int>, folderId: Int?) {
        viewModelScope.launch {
            repository.updateFolder(ids, folderId)
        }
    }
    
    // Tasks
    fun addTask(text: String) {
        viewModelScope.launch {
            repository.insertTask(Task(text = text))
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted, lastModified = System.currentTimeMillis()))
        }
    }

    fun deleteTasks(ids: List<Int>) {
        viewModelScope.launch {
            repository.deleteTasks(ids)
        }
    }

    fun pinTasks(ids: List<Int>, pin: Boolean) {
        viewModelScope.launch {
            repository.updateTaskPinnedStatus(ids, pin)
        }
    }

    // Folders
    fun addFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(Folder(name = name))
        }
    }

    fun deleteFolder(id: Int) {
        viewModelScope.launch {
            repository.deleteFolder(id)
        }
    }
}
