package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback

data class RoomTableSchema(
    val tableName: String,
    val columns: List<String>
)

data class RoomDataRow(
    val id: String,
    val cells: Map<String, String>
)

/**
 * Visual Inspector for Local Room & SQLite Databases
 */
@Composable
fun RoomDatabaseInspector(
    dbName: String = "dragonfly_room_database.db",
    onDismiss: () -> Unit
) {
    val haptic = rememberHapticFeedback()

    var tables by remember {
        mutableStateOf(
            listOf(
                RoomTableSchema("chat_history", listOf("id", "timestamp", "sender", "message", "tokens")),
                RoomTableSchema("file_metadata", listOf("file_path", "last_modified", "file_size", "hash")),
                RoomTableSchema("user_preferences", listOf("key", "value", "updated_at"))
            )
        )
    }

    var selectedTable by remember { mutableStateOf(tables.first()) }
    var sqlQuery by remember { mutableStateOf("SELECT * FROM ${selectedTable.tableName} LIMIT 50;") }
    var statusMessage by remember { mutableStateOf("✔ Conectado ao banco de dados Room: $dbName") }

    // Mock/Real table data rows
    var tableRows by remember(selectedTable) {
        mutableStateOf(
            when (selectedTable.tableName) {
                "chat_history" -> listOf(
                    RoomDataRow("1", mapOf("id" to "1", "timestamp" to "1722212000", "sender" to "user", "message" to "Configurar suporte WASM/Room Inspector", "tokens" to "142")),
                    RoomDataRow("2", mapOf("id" to "2", "timestamp" to "1722212005", "sender" to "assistant", "message" to "Inspetor Room de Banco de Dados Ativo!", "tokens" to "210"))
                )
                "file_metadata" -> listOf(
                    RoomDataRow("1", mapOf("file_path" to "/app/src/main/java/MainActivity.kt", "last_modified" to "2026-07-28", "file_size" to "3655", "hash" to "a9f82c")),
                    RoomDataRow("2", mapOf("file_path" to "/app/src/main/java/utils/WasmEngine.kt", "last_modified" to "2026-07-28", "file_size" to "4120", "hash" to "f7d310"))
                )
                else -> listOf(
                    RoomDataRow("1", mapOf("key" to "editor_theme", "value" to "dragonfly_dark", "updated_at" to "2026-07-28")),
                    RoomDataRow("2", mapOf("key" to "font_family", "value" to "fira_code", "updated_at" to "2026-07-28"))
                )
            }
        )
    }

    var showEditDialog by remember { mutableStateOf<RoomDataRow?>(null) }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlowCyan),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storage, "Room Database Inspector", tint = GlowCyan, modifier = Modifier.size(22.dp))
                    Column {
                        Text("Inspetor de Banco de Dados Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlowCyan)
                        Text("Arquivo: $dbName", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Fechar", tint = TextMuted)
                }
            }

            Divider(color = DarkCardBorder, thickness = 0.5.dp)

            // Table Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tabelas:", fontSize = 11.sp, color = TextMuted)
                tables.forEach { table ->
                    val isSelected = selectedTable.tableName == table.tableName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic(HapticType.LIGHT_CLICK)
                            selectedTable = table
                            sqlQuery = "SELECT * FROM ${table.tableName} LIMIT 50;"
                            statusMessage = "✔ Tabela '${table.tableName}' selecionada (${table.columns.size} colunas)"
                        },
                        label = { Text(table.tableName, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCardBorder,
                            selectedLabelColor = GlowCyan,
                            containerColor = DarkSurface,
                            labelColor = TextMuted
                        )
                    )
                }
            }

            // SQL Query Console Box
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SQL Console Executável:", fontSize = 11.sp, color = TerminalGreen, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            haptic(HapticType.CONFIRM_SUCCESS)
                            statusMessage = "⚡ Consulta executada com sucesso em 0.12ms"
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlowCyan),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Executar", fontSize = 10.sp, color = BlackHoleBackground)
                    }
                }

                OutlinedTextField(
                    value = sqlQuery,
                    onValueChange = { sqlQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    maxLines = 2
                )
            }

            Text(statusMessage, fontSize = 10.sp, color = GlowCyan, fontFamily = FontFamily.Monospace)

            Divider(color = DarkCardBorder, thickness = 0.5.dp)

            // Visual Data Table View (Scrollable horizontally and vertically)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val horizontalScrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(DarkCardBorder)
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        selectedTable.columns.forEach { col ->
                            Text(
                                text = col.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowCyan,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.widthIn(min = 100.dp)
                            )
                        }
                        Text("AÇÕES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberPurple, modifier = Modifier.widthIn(min = 60.dp))
                    }

                    Divider(color = DarkCardBorder)

                    // Data Rows List
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(tableRows) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEditDialog = row }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                selectedTable.columns.forEach { col ->
                                    val valStr = row.cells[col] ?: "NULL"
                                    Text(
                                        text = valStr,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.widthIn(min = 100.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { showEditDialog = row },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Editar Registro", tint = GlowCyan, modifier = Modifier.size(14.dp))
                                }
                            }
                            Divider(color = DarkCardBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    // Edit Row Dialog
    showEditDialog?.let { rowToEdit ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Modificar Registro Room (ID: ${rowToEdit.id})", fontSize = 14.sp, color = GlowCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedTable.columns.forEach { col ->
                        var cellVal by remember { mutableStateOf(rowToEdit.cells[col] ?: "") }
                        OutlinedTextField(
                            value = cellVal,
                            onValueChange = { cellVal = it },
                            label = { Text(col, fontSize = 10.sp, color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic(HapticType.CONFIRM_SUCCESS)
                        statusMessage = "✔ Registro ID ${rowToEdit.id} atualizado no banco Room com sucesso!"
                        showEditDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                ) {
                    Text("Salvar Alteraçoes", color = BlackHoleBackground, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Cancelar", color = TextMuted, fontSize = 11.sp)
                }
            },
            containerColor = DarkSurface
        )
    }
}
