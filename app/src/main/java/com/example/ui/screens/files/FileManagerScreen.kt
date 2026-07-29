package com.example.ui.screens.files

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.components.RoomDatabaseInspector
import com.example.ui.theme.*

@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    onOpenFileInEditor: (String) -> Unit,
    onRunFileInTerminal: (String) -> Unit,
    onAskAiAboutFile: (String) -> Unit
) {
    val currentPath by viewModel.currentPath.collectAsState()
    val files by viewModel.files.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    var renamingItem by remember { mutableStateOf<FileItem?>(null) }
    var renameInputName by remember { mutableStateOf("") }

    var fileDetailsItem by remember { mutableStateOf<FileItem?>(null) }

    var showDbInspector by remember { mutableStateOf(false) }
    var inspectingDbName by remember { mutableStateOf("dragonfly_room_database.db") }

    val filteredFiles = remember(files, searchFilter) {
        if (searchFilter.isBlank()) files
        else files.filter { it.name.contains(searchFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        // Path Header & Quick Actions
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.Default.ArrowUpward, "Subir Diretório", tint = GlowCyan)
                        }
                        Text(
                            text = currentPath,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            inspectingDbName = "dragonfly_room_database.db"
                            showDbInspector = true
                        }) {
                            Icon(Icons.Default.Storage, "Inspetor Room DB", tint = TerminalYellow)
                        }
                        IconButton(onClick = {
                            isCreatingFolder = false
                            showCreateDialog = true
                        }) {
                            Icon(Icons.Default.NoteAdd, "Novo Arquivo", tint = GlowCyan)
                        }
                        IconButton(onClick = {
                            isCreatingFolder = true
                            showCreateDialog = true
                        }) {
                            Icon(Icons.Default.CreateNewFolder, "Nova Pasta", tint = ElectricBlue)
                        }
                    }
                }

                // Quick Path Chips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rootDir = viewModel.fileRepository.getRootDirectory().absolutePath
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlowCyan),
                        modifier = Modifier.clickable { viewModel.loadDirectory(rootDir) }
                    ) {
                        Text("📁 Root do App", fontSize = 10.sp, color = GlowCyan, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }

                    val downloadDir = "/sdcard/Download"
                    if (java.io.File(downloadDir).exists()) {
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ElectricBlue),
                            modifier = Modifier.clickable { viewModel.loadDirectory(downloadDir) }
                        ) {
                            Text("📥 Downloads", fontSize = 10.sp, color = ElectricBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }
        }

        // Search Filter TextField
        OutlinedTextField(
            value = searchFilter,
            onValueChange = { viewModel.setSearchFilter(it) },
            placeholder = { Text("Filtrar arquivos...", fontSize = 11.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchFilter.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchFilter("") }) {
                        Icon(Icons.Default.Close, "Limpar", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = GlowCyan,
                unfocusedBorderColor = DarkCardBorder
            ),
            singleLine = true
        )

        // Bookmark row if available
        if (bookmarks.isNotEmpty()) {
            Text("Favoritos", fontSize = 11.sp, color = GlowCyan, modifier = Modifier.padding(bottom = 4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                bookmarks.forEach { bm ->
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.clickable { viewModel.loadDirectory(bm.filePath) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = TerminalYellow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(bm.alias, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // File List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredFiles) { file ->
                val isDb = file.name.endsWith(".db") || file.name.endsWith(".sqlite") || file.name.endsWith(".room")
                FileListItem(
                    file = file,
                    onItemClick = {
                        if (file.isDirectory) {
                            viewModel.loadDirectory(file.path)
                        } else if (isDb) {
                            inspectingDbName = file.name
                            showDbInspector = true
                        } else {
                            onOpenFileInEditor(file.path)
                        }
                    },
                    onInspectDb = {
                        inspectingDbName = file.name
                        showDbInspector = true
                    },
                    onRename = {
                        renamingItem = file
                        renameInputName = file.name
                    },
                    onShowDetails = {
                        fileDetailsItem = file
                    },
                    onDelete = { viewModel.deleteFile(file) },
                    onBookmark = { viewModel.toggleBookmark(file) },
                    onRunInTerminal = { onRunFileInTerminal("cat '${file.path}'") },
                    onAskAi = { onAskAiAboutFile("Analise o arquivo: ${file.path}") },
                    onZip = { viewModel.zipDirectory(file) },
                    onUnzip = { viewModel.unzipFile(file) }
                )
            }
        }
    }

    // Modal Create File / Folder Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (isCreatingFolder) "Nova Pasta" else "Novo Arquivo", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    placeholder = { Text(if (isCreatingFolder) "Nome da pasta" else "ex: script.py", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            if (isCreatingFolder) {
                                viewModel.createNewFolder(newItemName)
                            } else {
                                viewModel.createNewFile(newItemName)
                            }
                            newItemName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                ) {
                    Text("Criar", color = BlackHoleBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Modal Rename Dialog
    renamingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { renamingItem = null },
            title = { Text("Renomear ${if (item.isDirectory) "Pasta" else "Arquivo"}", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameInputName,
                    onValueChange = { renameInputName = it },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputName.isNotBlank() && renameInputName != item.name) {
                            viewModel.renameFile(item, renameInputName)
                        }
                        renamingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                ) {
                    Text("Salvar", color = BlackHoleBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingItem = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Modal Details Dialog
    fileDetailsItem?.let { item ->
        AlertDialog(
            onDismissRequest = { fileDetailsItem = null },
            title = { Text("Detalhes do Item", color = GlowCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nome: ${item.name}", fontSize = 12.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    Text("Caminho: ${item.path}", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    Text("Tipo: ${if (item.isDirectory) "Diretório" else "Arquivo"}", fontSize = 11.sp, color = TextMuted)
                    if (!item.isDirectory) {
                        Text("Tamanho: ${item.sizeBytes} bytes", fontSize = 11.sp, color = TextMuted)
                    }
                    Text("Última Modificação: ${java.util.Date(item.lastModified)}", fontSize = 10.sp, color = TextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = { fileDetailsItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                ) {
                    Text("Fechar", color = BlackHoleBackground)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showDbInspector) {
        RoomDatabaseInspector(
            dbName = inspectingDbName,
            onDismiss = { showDbInspector = false }
        )
    }
}

@Composable
fun FileListItem(
    file: FileItem,
    onItemClick: () -> Unit,
    onInspectDb: () -> Unit = {},
    onRename: () -> Unit = {},
    onShowDetails: () -> Unit = {},
    onDelete: () -> Unit,
    onBookmark: () -> Unit,
    onRunInTerminal: () -> Unit,
    onAskAi: () -> Unit,
    onZip: () -> Unit,
    onUnzip: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDb = file.name.endsWith(".db") || file.name.endsWith(".sqlite") || file.name.endsWith(".room")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when {
                        file.isDirectory -> Icons.Default.Folder
                        isDb -> Icons.Default.Storage
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = when {
                        file.isDirectory -> GlowCyan
                        isDb -> TerminalYellow
                        else -> ElectricBlue
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(file.name, fontSize = 13.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    if (!file.isDirectory) {
                        Text("${file.sizeBytes} bytes", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, "Opções", tint = TextMuted)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isDb) {
                        DropdownMenuItem(
                            text = { Text("Inspecionar Room DB", color = GlowCyan) },
                            onClick = {
                                onInspectDb()
                                showMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Renomear", color = TextPrimary) },
                        onClick = {
                            onRename()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Detalhes do Item", color = TextPrimary) },
                        onClick = {
                            onShowDetails()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Favoritar", color = TextPrimary) },
                        onClick = {
                            onBookmark()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Analisar com IA", color = TextPrimary) },
                        onClick = {
                            onAskAi()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Visualizar no Terminal", color = TextPrimary) },
                        onClick = {
                            onRunInTerminal()
                            showMenu = false
                        }
                    )
                    if (file.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("Compactar ZIP", color = TextPrimary) },
                            onClick = {
                                onZip()
                                showMenu = false
                            }
                        )
                    }
                    if (file.name.endsWith(".zip")) {
                        DropdownMenuItem(
                            text = { Text("Extrair ZIP", color = TextPrimary) },
                            onClick = {
                                onUnzip()
                                showMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Deletar", color = TerminalRed) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
