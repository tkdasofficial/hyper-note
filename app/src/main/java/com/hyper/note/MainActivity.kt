package com.hyper.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hyper.note.ui.screens.MainScreen
import com.hyper.note.ui.screens.FoldersScreen
import com.hyper.note.ui.screens.EditorScreen
import com.hyper.note.ui.screens.SettingsScreen
import com.hyper.note.ui.theme.AppTheme
import com.hyper.note.viewmodel.NoteViewModel
import com.hyper.note.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val fontSize by settingsViewModel.fontSize.collectAsState()
            
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            
            val fontScale = when (fontSize) {
                0 -> 0.85f
                2 -> 1.15f
                else -> 1.0f
            }

            AppTheme(darkTheme = isDark, fontScale = fontScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HyperNoteApp(settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun HyperNoteApp(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val noteViewModel: NoteViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = noteViewModel,
                onNavigateToEditor = { id ->
                    if (id != null) {
                        navController.navigate("editor/$id")
                    } else {
                        navController.navigate("editor/0") // 0 means new note
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToFolders = {
                    navController.navigate("folders")
                }
            )
        }
        composable(
            "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId")
            EditorScreen(
                noteId = if (noteId == 0) null else noteId,
                viewModel = noteViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("folders") {
            FoldersScreen(
                viewModel = noteViewModel,
                onNavigateBack = { navController.popBackStack() },
                onFolderSelected = { folderId ->
                    noteViewModel.setSelectedFolder(folderId)
                    navController.popBackStack() // Go back to MainScreen which will show the selected folder
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }
    }
}
