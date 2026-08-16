package com.hyper.note.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyper.note.data.AppDatabase
import com.hyper.note.data.Note
import com.hyper.note.data.SettingsManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val noteDao = AppDatabase.getDatabase(application).noteDao()
    
    val themeMode = settingsManager.themeModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )
    
    val fontSize = settingsManager.fontSizeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        1
    )
    
    fun setThemeMode(mode: Int) {
        viewModelScope.launch { settingsManager.setThemeMode(mode) }
    }
    
    fun setFontSize(size: Int) {
        viewModelScope.launch { settingsManager.setFontSize(size) }
    }

    fun exportNotes(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notes = noteDao.getAllNotesSync()
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, Note::class.java)
                val adapter = moshi.adapter<List<Note>>(listType)
                val json = adapter.toJson(notes)
                
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error exporting notes", e)
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }
    
    fun importNotes(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                reader.close()
                
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, Note::class.java)
                val adapter = moshi.adapter<List<Note>>(listType)
                val notes = adapter.fromJson(json)
                
                if (notes != null) {
                    notes.forEach { note ->
                        noteDao.insert(note.copy(id = 0)) // Insert as new notes
                    }
                    withContext(Dispatchers.Main) { onResult(true) }
                } else {
                    withContext(Dispatchers.Main) { onResult(false) }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error importing notes", e)
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }
}
