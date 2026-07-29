package com.example.ui.screens.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.ConversationEntity
import com.example.data.db.entities.WorkflowEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.TerminalManager
import com.example.data.repository.WorkflowRepository
import com.example.domain.model.AiProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AgentsViewModel(
    private val workflowRepository: WorkflowRepository,
    private val terminalManager: TerminalManager,
    private val chatRepository: ChatRepository
) : ViewModel() {
    val workflows: StateFlow<List<WorkflowEntity>> = workflowRepository.workflows
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversationsHistory: StateFlow<List<ConversationEntity>> = chatRepository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    val selectedConversationMessages: StateFlow<List<ChatMessageEntity>> = _selectedConversationId
        .flatMapLatest { id ->
            if (id != null) chatRepository.getMessagesForConversation(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _agentLog = MutableStateFlow<List<String>>(
        listOf("Dragonfly Autonomous Automation Engine v2.5 Ready.")
    )
    val agentLog: StateFlow<List<String>> = _agentLog.asStateFlow()

    private val _isAgentRunning = MutableStateFlow(false)
    val isAgentRunning: StateFlow<Boolean> = _isAgentRunning.asStateFlow()

    private val _isGeneratingWorkflow = MutableStateFlow(false)
    val isGeneratingWorkflow: StateFlow<Boolean> = _isGeneratingWorkflow.asStateFlow()

    init {
        viewModelScope.launch {
            workflowRepository.seedDefaultMacrosIfNeeded()
        }
    }

    fun selectHistoryConversation(conversationId: String) {
        _selectedConversationId.value = conversationId
    }

    fun deleteHistoryConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
            if (_selectedConversationId.value == conversationId) {
                _selectedConversationId.value = null
            }
            appendLog("🗑️ Registro de consulta de IA excluído do banco de dados Room.")
        }
    }

    fun runAgentTask(goalPrompt: String) {
        if (goalPrompt.isBlank() || _isAgentRunning.value) return

        viewModelScope.launch {
            _isAgentRunning.value = true
            appendLog("🤖 Agente de Automação iniciado: $goalPrompt")

            try {
                // Step 1: Create agent workspace conversation in Room Database
                val conv = chatRepository.createConversation("Agente: $goalPrompt", AiProvider.GEMINI)
                _selectedConversationId.value = conv.id

                appendLog("🔍 [Passo 1/3] Inspecionando estado do projeto e workspace...")
                terminalManager.executeCommand("pwd; ls -la")

                appendLog("🧠 [Passo 2/3] Processando raciocínio com Gemini AI e salvando histórico...")
                val responseMsg = chatRepository.sendMessage(
                    conversationId = conv.id,
                    userMessageText = "Atue como um Engenheiro DevOps & Desenvolvedor Autônomo. O objetivo é: $goalPrompt. Liste o plano de execução e os comandos shell para solucionar esta tarefa.",
                    provider = AiProvider.GEMINI,
                    model = AiProvider.GEMINI.defaultModel
                )

                appendLog("⚡ [Passo 3/3] Executando comandos do plano da IA...")
                appendLog("📄 Resposta da IA (Salva no Room DB):\n" + responseMsg.content.take(300) + "...")

                // Extract shell commands from markdown code blocks in response if present
                val cmdRegex = Regex("```(?:bash|sh|cmd)?\\n?([\\s\\S]*?)```")
                val matches = cmdRegex.findAll(responseMsg.content)
                matches.forEach { match ->
                    val block = match.groupValues[1].trim()
                    block.lines().forEach { line ->
                        val trimmedLine = line.trim()
                        if (trimmedLine.isNotBlank() && !trimmedLine.startsWith("#")) {
                            appendLog("  ▶ Execution Shell: $trimmedLine")
                            terminalManager.executeCommand(trimmedLine)
                        }
                    }
                }

                appendLog("✅ Tarefa do Agente concluída com sucesso e persistida no banco Room.")
            } catch (e: Exception) {
                appendLog("❌ Erro ao executar Agente: ${e.localizedMessage}")
            } finally {
                _isAgentRunning.value = false
            }
        }
    }

    fun generateAiWorkflow(prompt: String) {
        if (prompt.isBlank() || _isGeneratingWorkflow.value) return

        viewModelScope.launch {
            _isGeneratingWorkflow.value = true
            appendLog("✨ Gerando Workflow Inteligente via Gemini para: '$prompt'...")
            try {
                val conv = chatRepository.createConversation("Gerador Macro: $prompt", AiProvider.GEMINI)
                _selectedConversationId.value = conv.id

                val response = chatRepository.sendMessage(
                    conversationId = conv.id,
                    userMessageText = "Crie uma macro de automação shell para: '$prompt'. Responda APENAS em JSON com a estrutura: {\"name\": \"Nome Curto\", \"description\": \"Descrição\", \"commands\": \"comando1\\ncomando2\"}",
                    provider = AiProvider.GEMINI,
                    model = AiProvider.GEMINI.defaultModel
                )

                val name = "Macro IA: " + prompt.take(20)
                val desc = "Gerado automaticamente por IA para: $prompt"
                val cmds = response.content.replace("```json", "").replace("```", "").trim()

                createWorkflow(name, desc, cmds)
                appendLog("🎉 Workflow '$name' gerado e salvo no banco de dados com sucesso!")
            } catch (e: Exception) {
                appendLog("❌ Falha ao gerar workflow por IA: ${e.localizedMessage}")
            } finally {
                _isGeneratingWorkflow.value = false
            }
        }
    }

    fun createWorkflow(name: String, description: String, commands: String) {
        viewModelScope.launch {
            workflowRepository.createWorkflow(name, description, commands)
            appendLog("➕ Novo Workflow/Macro registrado: '$name'")
        }
    }

    fun executeWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            workflowRepository.executeWorkflow(workflow) { logLine ->
                appendLog(logLine)
            }
        }
    }

    fun deleteWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            workflowRepository.deleteWorkflow(workflow)
            appendLog("🗑️ Workflow removido: '${workflow.name}'")
        }
    }

    fun clearLog() {
        _agentLog.value = listOf("Dragonfly Autonomous Automation Engine v2.5 Ready.")
    }

    private fun appendLog(msg: String) {
        _agentLog.value = _agentLog.value + msg
    }
}
