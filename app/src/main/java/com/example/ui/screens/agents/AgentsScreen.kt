package com.example.ui.screens.agents

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.ConversationEntity
import com.example.data.db.entities.WorkflowEntity
import com.example.ui.components.CodeBlockWithSyntax
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgentsScreen(viewModel: AgentsViewModel) {
    val haptic = rememberHapticFeedback()
    val workflows by viewModel.workflows.collectAsState()
    val conversationsHistory by viewModel.conversationsHistory.collectAsState()
    val selectedConvId by viewModel.selectedConversationId.collectAsState()
    val selectedMessages by viewModel.selectedConversationMessages.collectAsState()

    val agentLog by viewModel.agentLog.collectAsState()
    val isRunning by viewModel.isAgentRunning.collectAsState()
    val isGenerating by viewModel.isGeneratingWorkflow.collectAsState()

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Macros, 1 = Agente IA, 2 = Histórico Room, 3 = Logs
    var goalInput by remember { mutableStateOf("") }
    var aiWorkflowPrompt by remember { mutableStateOf("") }
    var historySearchQuery by remember { mutableStateOf("") }

    var showCreateWorkflowModal by remember { mutableStateOf(false) }
    var wfName by remember { mutableStateOf("") }
    var wfDesc by remember { mutableStateOf("") }
    var wfCmds by remember { mutableStateOf("") }

    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Build & Compilação", "Gestão de Arquivos", "Git & Backup", "Termux & Shell")

    val filteredWorkflows = remember(workflows, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Todos") {
            workflows
        } else {
            workflows.filter { wf ->
                when (selectedCategoryFilter) {
                    "Build & Compilação" -> wf.name.contains("Build", true) || wf.name.contains("Compilação", true) || wf.commands.contains("gradle", true)
                    "Gestão de Arquivos" -> wf.name.contains("Arquivo", true) || wf.name.contains("Limpeza", true) || wf.commands.contains("rm", true) || wf.commands.contains("ls", true)
                    "Git & Backup" -> wf.name.contains("Git", true) || wf.commands.contains("git", true)
                    "Termux & Shell" -> wf.name.contains("Termux", true) || wf.commands.contains("termux", true)
                    else -> true
                }
            }
        }
    }

    val filteredHistory = remember(conversationsHistory, historySearchQuery) {
        if (historySearchQuery.isBlank()) {
            conversationsHistory
        } else {
            conversationsHistory.filter { conv ->
                conv.title.contains(historySearchQuery, ignoreCase = true) ||
                        conv.provider.contains(historySearchQuery, ignoreCase = true) ||
                        conv.model.contains(historySearchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        // Dashboard Header Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GlowCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoMode, "Automação", tint = GlowCyan, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Dashboard de Automações & Agente IA", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlowCyan)
                            Text("${conversationsHistory.size} consultas salvas no banco Room DB local", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Surface(
                        color = if (isRunning || isGenerating) Color(0xFF332B10) else Color(0xFF0D2818),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, if (isRunning || isGenerating) TerminalYellow else TerminalGreen)
                    ) {
                        Text(
                            text = if (isRunning) "AGENTE ATIVO" else if (isGenerating) "GERANDO..." else "PRONTO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning || isGenerating) TerminalYellow else TerminalGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Segmented Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF10141D),
                    contentColor = GlowCyan,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic(HapticType.LIGHT_CLICK)
                            selectedTab = 0
                        },
                        text = { Text("Macros", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Build, null, modifier = Modifier.size(14.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic(HapticType.LIGHT_CLICK)
                            selectedTab = 1
                        },
                        text = { Text("Agente IA", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.SmartToy, null, modifier = Modifier.size(14.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            haptic(HapticType.LIGHT_CLICK)
                            selectedTab = 2
                        },
                        text = { Text("Histórico Room (${conversationsHistory.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = {
                            haptic(HapticType.LIGHT_CLICK)
                            selectedTab = 3
                        },
                        text = { Text("Logs (${agentLog.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Terminal, null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }

        // Tab Content Switcher
        when (selectedTab) {
            0 -> {
                // TAB 0: MACROS & PIPELINES DASHBOARD
                Column(modifier = Modifier.fillMaxSize()) {
                    // Quick Action Category Filters
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategoryFilter).coerceAtLeast(0),
                        containerColor = Color.Transparent,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        categories.forEach { cat ->
                            val isSel = cat == selectedCategoryFilter
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    haptic(HapticType.LIGHT_CLICK)
                                    selectedCategoryFilter = cat
                                },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 6.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GlowCyan,
                                    selectedLabelColor = BlackHoleBackground,
                                    containerColor = DarkSurface,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MACROS E WORKFLOWS SALVOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Button(
                            onClick = {
                                haptic(HapticType.LIGHT_CLICK)
                                showCreateWorkflowModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowCyan),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = BlackHoleBackground, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Macro", fontSize = 11.sp, color = BlackHoleBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (filteredWorkflows.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FolderOpen, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Nenhum workflow encontrado para esta categoria.", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredWorkflows) { wf ->
                                MacroCardItem(
                                    workflow = wf,
                                    onRun = {
                                        haptic(HapticType.CONFIRM_SUCCESS)
                                        viewModel.executeWorkflow(wf)
                                        selectedTab = 3 // Switch to logs tab
                                    },
                                    onDelete = {
                                        haptic(HapticType.WARNING_ERROR)
                                        viewModel.deleteWorkflow(wf)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // AI Workflow Quick Generator Bar
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gerar Nova Macro com Inteligência Artificial", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberPurple)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = aiWorkflowPrompt,
                                    onValueChange = {
                                        haptic(HapticType.KEYPRESS)
                                        aiWorkflowPrompt = it
                                    },
                                    placeholder = { Text("Ex: Criar script de backup zip...", fontSize = 11.sp, color = TextMuted) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                                )
                                Button(
                                    onClick = {
                                        if (aiWorkflowPrompt.isNotBlank()) {
                                            haptic(HapticType.CONFIRM_SUCCESS)
                                            viewModel.generateAiWorkflow(aiWorkflowPrompt)
                                            aiWorkflowPrompt = ""
                                        }
                                    },
                                    enabled = !isGenerating,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: AUTONOMOUS AI AGENT EXECUTION
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Agente Autônomo de Código & DevOps", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlowCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "O Agente analisa o repositório, cria planos de execução, roda comandos no shell e persiste cada consulta e resposta no banco de dados local Room.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = goalInput,
                                onValueChange = { goalInput = it },
                                placeholder = { Text("Descreva o objetivo completo. Ex: Inspecione o diretório, crie um arquivo de log formatado em JSON e execute a verificação no terminal.", fontSize = 11.sp, color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (goalInput.isNotBlank()) {
                                        viewModel.runAgentTask(goalInput)
                                        goalInput = ""
                                        selectedTab = 3 // Switch to logs tab
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isRunning,
                                colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BlackHoleBackground, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Agente Executando Tarefa...", color = BlackHoleBackground, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.PlayArrow, null, tint = BlackHoleBackground, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Iniciar Agente & Persistir no Room DB", color = BlackHoleBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text("EXEMPLOS RÁPIDOS DE TAREFAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)

                    val presetGoals = listOf(
                        "Inspecionar todos os arquivos Kotlin do projeto e listar resumos de funções.",
                        "Executar limpeza de cache build do Gradle e gerar relatório de arquivos.",
                        "Testar permissões do Termux e enviar uma notificação de status de bateria."
                    )

                    presetGoals.forEach { preset ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.runAgentTask(preset)
                                    selectedTab = 3
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Bolt, null, tint = TerminalYellow, modifier = Modifier.size(18.dp))
                                Text(preset, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: ROOM DATABASE AI CHAT HISTORY SIDEBAR & DETAIL VIEW
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left History Sidebar List (Room Database Persistence)
                    Column(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .padding(end = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = historySearchQuery,
                            onValueChange = { historySearchQuery = it },
                            placeholder = { Text("Buscar histórico...", fontSize = 11.sp, color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )

                        if (filteredHistory.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Inbox, null, tint = TextMuted, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Nenhuma consulta salva.", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredHistory) { conv ->
                                    val isSelected = conv.id == selectedConvId

                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic(HapticType.LIGHT_CLICK)
                                                viewModel.selectHistoryConversation(conv.id)
                                            },
                                        backgroundColor = if (isSelected) Color(0xFF1E293B) else DarkSurface,
                                        borderColor = if (isSelected) GlowCyan else DarkCardBorder
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = conv.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) GlowCyan else TextPrimary,
                                                    maxLines = 1,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        haptic(HapticType.WARNING_ERROR)
                                                        viewModel.deleteHistoryConversation(conv.id)
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, "Excluir do Room", tint = TerminalRed, modifier = Modifier.size(12.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${conv.provider} • ${conv.model}",
                                                    fontSize = 10.sp,
                                                    color = TextMuted,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = dateFormat.format(Date(conv.updatedAt)),
                                                    fontSize = 9.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right History Details Inspector
                    Column(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                            .padding(start = 4.dp)
                    ) {
                        if (selectedConvId == null) {
                            GlassCard(
                                modifier = Modifier.fillMaxSize(),
                                backgroundColor = DarkSurface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.QuestionAnswer, null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Selecione uma consulta no histórico do Room DB para inspecionar os detalhes completos.", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        } else {
                            val activeConv = conversationsHistory.find { it.id == selectedConvId }

                            GlassCard(
                                modifier = Modifier.fillMaxSize(),
                                backgroundColor = DarkSurface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(activeConv?.title ?: "Detalhes da Consulta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlowCyan)
                                            Text("Persistido no SQLite via Room DAO", fontSize = 10.sp, color = TextMuted)
                                        }

                                        if (activeConv != null) {
                                            IconButton(
                                                onClick = {
                                                    haptic(HapticType.CONFIRM_SUCCESS)
                                                    viewModel.runAgentTask(activeConv.title.removePrefix("Agente: "))
                                                    selectedTab = 3
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Replay, "Re-executar Consulta", tint = TerminalGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(selectedMessages) { msg ->
                                            HistoryMessageItem(
                                                message = msg,
                                                onCopy = { txt ->
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("QueryResponse", txt))
                                                    Toast.makeText(context, "Conteúdo copiado com sucesso!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // TAB 3: EXECUTION LOGS CONSOLE
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONSOLE DE LOGS & EXECUÇÃO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    val logText = agentLog.joinToString("\n")
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("AgentLog", logText))
                                    Toast.makeText(context, "Logs copiados para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, "Copiar Logs", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.clearLog() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Limpar Logs", tint = TerminalRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        backgroundColor = DarkSurface
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(agentLog) { log ->
                                val textColor = when {
                                    log.contains("✅") || log.contains("✔") || log.contains("🎉") -> TerminalGreen
                                    log.contains("❌") || log.contains("Erro") -> TerminalRed
                                    log.contains("▶") || log.contains("🤖") || log.contains("🚀") -> GlowCyan
                                    log.contains("🔍") || log.contains("🧠") || log.contains("⚡") -> TerminalYellow
                                    else -> TextPrimary
                                }
                                Text(
                                    text = log,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = textColor,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Create Workflow Dialog
    if (showCreateWorkflowModal) {
        AlertDialog(
            onDismissRequest = { showCreateWorkflowModal = false },
            title = { Text("Criar Nova Macro de Automação", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wfName,
                        onValueChange = { wfName = it },
                        label = { Text("Nome da Macro", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = wfDesc,
                        onValueChange = { wfDesc = it },
                        label = { Text("Descrição Breve", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = wfCmds,
                        onValueChange = { wfCmds = it },
                        label = { Text("Comandos Shell (um por linha ou separados por ;)", color = TextMuted) },
                        placeholder = { Text("pwd\ngradle assembleDebug\ntermux-toast 'Build Finalizado'", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (wfName.isNotBlank() && wfCmds.isNotBlank()) {
                            viewModel.createWorkflow(wfName, wfDesc, wfCmds)
                            wfName = ""
                            wfDesc = ""
                            wfCmds = ""
                            showCreateWorkflowModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                ) {
                    Text("Salvar Macro", color = BlackHoleBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateWorkflowModal = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun HistoryMessageItem(
    message: ChatMessageEntity,
    onCopy: (String) -> Unit
) {
    val isUser = message.role == "user"
    val cardBg = if (isUser) Color(0xFF161F30) else DarkSurface
    val borderCol = if (isUser) ElectricBlue else DarkCardBorder

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBg,
        borderColor = borderCol,
        cornerRadius = 10.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = if (isUser) ElectricBlue else GlowCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isUser) "Pergunta / Prompt" else "Resposta da IA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) ElectricBlue else GlowCyan
                    )
                }

                IconButton(
                    onClick = { onCopy(message.content) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, "Copiar", tint = TextMuted, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val content = message.content
            val codeBlockRegex = Regex("```([a-zA-Z0-9_]*)\\n?([\\s\\S]*?)```")
            val matches = codeBlockRegex.findAll(content).toList()

            if (matches.isEmpty()) {
                Text(
                    text = content,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    lineHeight = 15.sp
                )
            } else {
                var lastIndex = 0
                matches.forEach { match ->
                    val textBefore = content.substring(lastIndex, match.range.first)
                    if (textBefore.isNotBlank()) {
                        Text(
                            text = textBefore.trim(),
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 15.sp
                        )
                    }

                    val lang = match.groupValues[1].ifBlank { "code" }
                    val codeText = match.groupValues[2].trim()

                    CodeBlockWithSyntax(
                        code = codeText,
                        language = lang,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    lastIndex = match.range.last + 1
                }

                if (lastIndex < content.length) {
                    val textAfter = content.substring(lastIndex)
                    if (textAfter.isNotBlank()) {
                        Text(
                            text = textAfter.trim(),
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroCardItem(
    workflow: WorkflowEntity,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF142030)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Terminal, null, tint = GlowCyan, modifier = Modifier.size(16.dp))
                    }

                    Column {
                        Text(workflow.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(workflow.description, fontSize = 10.sp, color = TextMuted)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRun,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Executar Macro", tint = TerminalGreen, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Excluir Macro", tint = TerminalRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Code Preview Box
            CodeBlockWithSyntax(
                code = workflow.commands,
                language = "sh",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
