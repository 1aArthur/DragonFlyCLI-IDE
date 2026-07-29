package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.ChatMessageEntity
import com.example.domain.model.AiProvider
import com.example.ui.components.CodeBlockWithSyntax
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onRunInTerminal: (String) -> Unit,
    onOpenInEditor: (String) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val conversations by viewModel.conversations.collectAsState()
    val currentConv by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showProviderMenu by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }
    var showConvDrawer by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showConvDrawer = true }) {
                    Icon(Icons.Default.Menu, "Conversas", tint = GlowCyan)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = currentConv?.title ?: "Chat IA",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${selectedProvider.displayName} • $selectedModel",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Row {
                // Provider Dropdown Trigger
                IconButton(onClick = { showProviderMenu = true }) {
                    Icon(Icons.Default.Psychology, "Provedor", tint = ElectricBlue)
                }
                DropdownMenu(
                    expanded = showProviderMenu,
                    onDismissRequest = { showProviderMenu = false }
                ) {
                    AiProvider.values().forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.setProvider(provider)
                                showProviderMenu = false
                            }
                        )
                    }
                }

                // Model Selector
                IconButton(onClick = { showModelMenu = true }) {
                    Icon(Icons.Default.Tune, "Modelo", tint = CyberPurple)
                }

                IconButton(onClick = { viewModel.createNewConversation() }) {
                    Icon(Icons.Default.AddComment, "Nova Conversa", tint = GlowCyan)
                }
            }
        }

        // Model Picker Dialog Modal
        if (showModelMenu) {
            var customModelInput by remember { mutableStateOf(selectedModel) }

            AlertDialog(
                onDismissRequest = { showModelMenu = false },
                title = { Text("Escolher Modelo (${selectedProvider.displayName})", color = TextPrimary, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Selecione um modelo pré-definido ou digite um identificador personalizado:", fontSize = 11.sp, color = TextMuted)

                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .fillMaxWidth()
                        ) {
                            items(availableModels) { model ->
                                val isSelected = model.id == selectedModel
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            viewModel.setModel(model.id)
                                            showModelMenu = false
                                        },
                                    backgroundColor = if (isSelected) Color(0xFF1E293B) else DarkSurface,
                                    borderColor = if (isSelected) GlowCyan else DarkCardBorder
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(model.name, fontSize = 13.sp, color = if (isSelected) GlowCyan else TextPrimary)
                                        Text(model.id, fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customModelInput,
                            onValueChange = { customModelInput = it },
                            label = { Text("Nome do Modelo Personalizado", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customModelInput.isNotBlank()) {
                                viewModel.setModel(customModelInput.trim())
                            }
                            showModelMenu = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                    ) {
                        Text("Aplicar Modelo", color = BlackHoleBackground)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showModelMenu = false }) {
                        Text("Cancelar", color = TextMuted)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Drawer Modal Dialog for Conversations
        if (showConvDrawer) {
            AlertDialog(
                onDismissRequest = { showConvDrawer = false },
                title = { Text("Minhas Conversas", color = TextPrimary) },
                text = {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(conversations) { conv ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.selectConversation(conv)
                                        showConvDrawer = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(conv.title, color = TextPrimary, fontSize = 14.sp)
                                        Text("${conv.provider} • ${conv.model}", color = TextMuted, fontSize = 11.sp)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteCurrentConversation() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Deletar", tint = TerminalRed)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showConvDrawer = false }) {
                        Text("Fechar", color = GlowCyan)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(
                    message = msg,
                    onCopy = { clipboardManager.setText(AnnotatedString(it)) },
                    onRunInTerminal = onRunInTerminal,
                    onOpenInEditor = onOpenInEditor
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = GlowCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedProvider.displayName} pensando...",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlowCyan
                        )
                    }
                }
            }
        }

        // Input Field Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            cornerRadius = 24.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        haptic(HapticType.KEYPRESS)
                        inputText = it
                    },
                    placeholder = { Text("Digite sua mensagem para a IA...", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            haptic(HapticType.CONFIRM_SUCCESS)
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            haptic(HapticType.CONFIRM_SUCCESS)
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlowCyan)
                ) {
                    Icon(Icons.Default.Send, "Enviar", tint = BlackHoleBackground)
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onCopy: (String) -> Unit,
    onRunInTerminal: (String) -> Unit,
    onOpenInEditor: (String) -> Unit
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val cardBg = if (isUser) Color(0xFF161F30) else DarkSurface
    val borderCol = if (isUser) ElectricBlue else DarkCardBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                contentDescription = null,
                tint = if (isUser) ElectricBlue else GlowCyan,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "Você" else "${message.provider} (${message.model})",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        GlassCard(
            backgroundColor = cardBg,
            borderColor = borderCol,
            cornerRadius = 16.dp,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val content = message.content
                val codeBlockRegex = Regex("```([a-zA-Z0-9_]*)\\n?([\\s\\S]*?)```")

                var lastIndex = 0
                val matches = codeBlockRegex.findAll(content).toList()

                if (matches.isEmpty()) {
                    Text(
                        text = content,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                } else {
                    matches.forEach { match ->
                        val textBefore = content.substring(lastIndex, match.range.first)
                        if (textBefore.isNotBlank()) {
                            Text(
                                text = textBefore.trim(),
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        val lang = match.groupValues[1].ifBlank { "code" }
                        val codeText = match.groupValues[2].trim()

                        CodeBlockWithSyntax(
                            code = codeText,
                            language = lang,
                            modifier = Modifier.padding(vertical = 4.dp),
                            onOpenInEditor = onOpenInEditor,
                            onRunInTerminal = onRunInTerminal
                        )

                        lastIndex = match.range.last + 1
                    }

                    if (lastIndex < content.length) {
                        val textAfter = content.substring(lastIndex)
                        if (textAfter.isNotBlank()) {
                            Text(
                                text = textAfter.trim(),
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                // Action controls for AI assistant outputs
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { onCopy(message.content) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, "Copiar", tint = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onRunInTerminal(message.content) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Terminal, "Executar no Terminal", tint = GlowCyan)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onOpenInEditor(message.content) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Code, "Abrir no Editor", tint = ElectricBlue)
                        }
                    }
                }
            }
        }
    }
}
