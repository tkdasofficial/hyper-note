package com.hyper.note.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hyper.note.data.Note
import com.hyper.note.ui.components.MarkdownText
import com.hyper.note.viewmodel.NoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: Int?,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf(noteId ?: 0) }
    var tags by remember { mutableStateOf("") }
    
    var showPreview by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(noteId) {
        if (noteId != null && noteId != 0) {
            viewModel.getNoteById(noteId).collect { note ->
                if (note != null) {
                    title = note.title
                    content = note.content
                    isPinned = note.isPinned
                    tags = note.tags
                    currentNoteId = note.id
                }
            }
        }
    }

    // Auto-save debounce
    LaunchedEffect(title, content, isPinned, tags) {
        if (title.isNotBlank() || content.isNotBlank()) {
            delay(300) // 300ms debounce
            val noteToSave = Note(
                id = currentNoteId,
                title = title,
                content = content,
                isPinned = isPinned,
                tags = tags,
                lastModified = System.currentTimeMillis()
            )
            viewModel.saveNote(noteToSave) { savedId ->
                if (currentNoteId == 0) {
                    currentNoteId = savedId
                }
            }
        }
    }

    val handleBack = {
        if (title.isNotBlank() || content.isNotBlank()) {
            val finalNote = Note(
                id = currentNoteId,
                title = title,
                content = content,
                isPinned = isPinned,
                tags = tags,
                lastModified = System.currentTimeMillis()
            )
            viewModel.saveNote(finalNote)
        }
        onNavigateBack()
    }

    BackHandler {
        handleBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin"
                        )
                    }
                    TextButton(onClick = { showPreview = !showPreview }) {
                        Text(if (showPreview) "EDIT" else "PREVIEW", color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                if (currentNoteId != 0) {
                                    showDeleteConfirmDialog = true
                                } else {
                                    onNavigateBack() // Just go back if note hasn't been saved
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { content += "**bold** " }, contentPadding = PaddingValues(4.dp)) { Text("B") }
                        TextButton(onClick = { content += "*italic* " }, contentPadding = PaddingValues(4.dp)) { Text("I") }
                        TextButton(onClick = { content += "## " }, contentPadding = PaddingValues(4.dp)) { Text("H") }
                        TextButton(onClick = { content += "- " }, contentPadding = PaddingValues(4.dp)) { Text("List") }
                    }
                    
                    val charCount = content.length
                    val wordCount = if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
                    Text("$wordCount words", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tags Input
            BasicTextField(
                value = tags,
                onValueChange = { tags = it },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (tags.isEmpty()) {
                        Text("Add tags (comma separated)...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text("Title", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            // Body
            if (showPreview) {
                MarkdownText(
                    markdown = content,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text("Start typing...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                )
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (currentNoteId != 0) {
                            viewModel.deleteNote(currentNoteId)
                        }
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
