package com.hyper.note.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val THEME_MODE = intPreferencesKey("theme_mode") // 0 = System, 1 = Light, 2 = Dark
        val FONT_SIZE = intPreferencesKey("font_size") // 0 = Small, 1 = Normal, 2 = Large
    }

    val themeModeFlow: Flow<Int> = context.dataStore.data.map { it[THEME_MODE] ?: 0 }
    val fontSizeFlow: Flow<Int> = context.dataStore.data.map { it[FONT_SIZE] ?: 1 }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { it[FONT_SIZE] = size }
    }
}
