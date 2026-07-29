package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.ConversationEntity
import com.example.data.repository.ChatRepository
import com.example.domain.model.AiModel
import com.example.domain.model.AiProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {
    val conversations: StateFlow<List<ConversationEntity>> = chatRepository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _selectedProvider = MutableStateFlow(AiProvider.GEMINI)
    val selectedProvider: StateFlow<AiProvider> = _selectedProvider.asStateFlow()

    private val _selectedModel = MutableStateFlow(AiProvider.GEMINI.defaultModel)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val availableModels: StateFlow<List<AiModel>> = _selectedProvider.map { provider ->
        AiModel.getDefaultModels().filter { it.provider == provider }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiModel.getDefaultModels())

    val messages: StateFlow<List<ChatMessageEntity>> = _currentConversation
        .flatMapLatest { conv ->
            if (conv != null) chatRepository.getMessagesForConversation(conv.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            conversations.collect { list ->
                if (_currentConversation.value == null && list.isNotEmpty()) {
                    selectConversation(list.first())
                } else if (list.isEmpty()) {
                    createNewConversation("Nova Conversa IA")
                }
            }
        }
    }

    fun selectConversation(conv: ConversationEntity) {
        _currentConversation.value = conv
        val prov = try { AiProvider.valueOf(conv.provider) } catch (e: Exception) { AiProvider.GEMINI }
        _selectedProvider.value = prov
        _selectedModel.value = conv.model
    }

    fun setProvider(provider: AiProvider) {
        _selectedProvider.value = provider
        viewModelScope.launch {
            val config = chatRepository.getApiConfig(provider)
            val modelToUse = if (config.defaultModel.isNotBlank()) config.defaultModel else provider.defaultModel
            _selectedModel.value = modelToUse

            _currentConversation.value?.let { conv ->
                val updated = conv.copy(
                    provider = provider.name,
                    model = modelToUse,
                    updatedAt = System.currentTimeMillis()
                )
                chatRepository.updateConversation(updated)
                _currentConversation.value = updated
            }
        }
    }

    fun setModel(model: String) {
        _selectedModel.value = model
        viewModelScope.launch {
            _currentConversation.value?.let { conv ->
                val updated = conv.copy(
                    model = model,
                    updatedAt = System.currentTimeMillis()
                )
                chatRepository.updateConversation(updated)
                _currentConversation.value = updated
            }
        }
    }

    fun createNewConversation(title: String = "Nova Conversa Dragonfly") {
        viewModelScope.launch {
            val config = chatRepository.getApiConfig(_selectedProvider.value)
            val initialModel = if (_selectedModel.value.isNotBlank()) _selectedModel.value else config.defaultModel
            val newConv = chatRepository.createConversation(
                title = title,
                provider = _selectedProvider.value,
                model = initialModel
            )
            _currentConversation.value = newConv
        }
    }

    fun sendMessage(promptText: String) {
        val conv = _currentConversation.value ?: return
        if (promptText.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                chatRepository.sendMessage(
                    conversationId = conv.id,
                    userMessageText = promptText,
                    provider = _selectedProvider.value,
                    model = _selectedModel.value
                )
            } catch (e: Exception) {
                // Handled in repository
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCurrentConversation() {
        val conv = _currentConversation.value ?: return
        viewModelScope.launch {
            chatRepository.deleteConversation(conv.id)
            _currentConversation.value = null
        }
    }
}
