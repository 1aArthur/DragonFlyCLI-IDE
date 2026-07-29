package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.network.AiApiClient
import com.example.data.network.AiRequestOptions
import com.example.data.repository.ApiConfigRepository
import com.example.domain.model.AiModel
import com.example.domain.model.AiProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val apiConfigRepository: ApiConfigRepository,
    private val aiApiClient: AiApiClient = AiApiClient()
) : ViewModel() {
    private val _selectedProvider = MutableStateFlow(AiProvider.GEMINI)
    val selectedProvider: StateFlow<AiProvider> = _selectedProvider.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _baseUrl = MutableStateFlow("")
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _modelName = MutableStateFlow("")
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _topP = MutableStateFlow(0.9f)
    val topP: StateFlow<Float> = _topP.asStateFlow()

    private val _maxTokens = MutableStateFlow(4096)
    val maxTokens: StateFlow<Int> = _maxTokens.asStateFlow()

    private val _saveStatus = MutableStateFlow("")
    val saveStatus: StateFlow<String> = _saveStatus.asStateFlow()

    val availableModels: StateFlow<List<AiModel>> = _selectedProvider.map { provider ->
        AiModel.getDefaultModels().filter { it.provider == provider }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var autoSaveJob: Job? = null

    init {
        loadConfigForProvider(AiProvider.GEMINI)
    }

    fun selectProvider(provider: AiProvider) {
        _selectedProvider.value = provider
        loadConfigForProvider(provider)
    }

    private fun loadConfigForProvider(provider: AiProvider) {
        viewModelScope.launch {
            val config = apiConfigRepository.getApiConfig(provider)
            _apiKey.value = config.apiKey
            _baseUrl.value = config.baseUrl.ifBlank { provider.defaultBaseUrl }
            _modelName.value = config.defaultModel.ifBlank { provider.defaultModel }
            _temperature.value = config.temperature
            _topP.value = config.topP
            _maxTokens.value = config.maxTokens
            _saveStatus.value = ""
        }
    }

    fun setApiKey(value: String) {
        _apiKey.value = value
        scheduleAutoSave()
    }

    fun setBaseUrl(value: String) {
        _baseUrl.value = value
        scheduleAutoSave()
    }

    fun setModelName(value: String) {
        _modelName.value = value
        scheduleAutoSave()
    }

    fun setTemperature(value: Float) {
        _temperature.value = value
        scheduleAutoSave()
    }

    fun setTopP(value: Float) {
        _topP.value = value
        scheduleAutoSave()
    }

    fun setMaxTokens(value: Int) {
        _maxTokens.value = value
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(600)
            persistConfig(isAuto = true)
        }
    }

    fun saveConfig() {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            persistConfig(isAuto = false)
        }
    }

    private suspend fun persistConfig(isAuto: Boolean) {
        val provider = _selectedProvider.value
        apiConfigRepository.saveApiConfig(
            provider = provider,
            apiKey = _apiKey.value,
            baseUrl = _baseUrl.value,
            defaultModel = _modelName.value,
            temperature = _temperature.value,
            topP = _topP.value,
            maxTokens = _maxTokens.value,
            timeoutSec = 30
        )
        _saveStatus.value = if (isAuto) {
            "✔ Alterações salvas automaticamente no Keystore (${provider.displayName})"
        } else {
            "✔ Configurações salvas no Keystore para ${provider.displayName}!"
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _saveStatus.value = "Testando conexão com ${_selectedProvider.value.displayName}..."
            val response = aiApiClient.sendMessage(
                messages = listOf("user" to "Hello, respond with OK if connection is working!"),
                options = AiRequestOptions(
                    provider = _selectedProvider.value,
                    model = _modelName.value,
                    apiKey = _apiKey.value,
                    baseUrl = _baseUrl.value,
                    maxTokens = 100
                )
            )
            _saveStatus.value = "Resultado do teste: $response"
        }
    }
}
