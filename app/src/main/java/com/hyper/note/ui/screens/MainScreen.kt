package com.hyper.note.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hyper.note.viewmodel.NoteViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: NoteViewModel,
    onNavigateToEditor: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFolders: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToEditor = onNavigateToEditor,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFolders = onNavigateToFolders
                )
                1 -> TasksScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
