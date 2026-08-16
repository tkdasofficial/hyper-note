package com.hyper.note.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hyper.note.data.Task
import com.hyper.note.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(viewModel: NoteViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val pinnedTasks = tasks.filter { it.isPinned }
    val regularTasks = tasks.filter { !it.isPinned }

    var selectedTaskIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedTaskIds.isNotEmpty()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedTaskIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedTaskIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        val areAllSelectedPinned = selectedTaskIds.all { id -> tasks.find { it.id == id }?.isPinned == true }
                        IconButton(onClick = {
                            viewModel.pinTasks(selectedTaskIds.toList(), !areAllSelectedPinned)
                            selectedTaskIds = emptySet()
                        }) {
                            Icon(Icons.Default.PushPin, contentDescription = if (areAllSelectedPinned) "Unpin selected" else "Pin selected")
                        }
                        IconButton(onClick = {
                            viewModel.deleteTasks(selectedTaskIds.toList())
                            selectedTaskIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Tasks", color = MaterialTheme.colorScheme.primary) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks yet. Tap '+' to add.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pinnedTasks.isNotEmpty()) {
                        item {
                            Text("PINNED", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        items(pinnedTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                isSelected = selectedTaskIds.contains(task.id),
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedTaskIds = if (selectedTaskIds.contains(task.id)) selectedTaskIds - task.id else selectedTaskIds + task.id
                                    } else {
                                        viewModel.toggleTask(task)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) selectedTaskIds = setOf(task.id)
                                }
                            )
                        }
                    }
                    if (regularTasks.isNotEmpty()) {
                        if (pinnedTasks.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                        item {
                            Text("TASKS", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        items(regularTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                isSelected = selectedTaskIds.contains(task.id),
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedTaskIds = if (selectedTaskIds.contains(task.id)) selectedTaskIds - task.id else selectedTaskIds + task.id
                                    } else {
                                        viewModel.toggleTask(task)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) selectedTaskIds = setOf(task.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            var newTaskText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text("Add Task") },
                text = {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("Task description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                viewModel.addTask(newTaskText)
                            }
                            showAddTaskDialog = false
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            Text(
                text = task.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.isCompleted && !isSelectionMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.isCompleted && !isSelectionMode) TextDecoration.LineThrough else null
            )
        }
    }
}
