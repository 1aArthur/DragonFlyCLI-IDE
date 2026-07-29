package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.entities.ApiConfigEntity
import com.example.data.repository.ApiConfigRepository
import com.example.domain.model.AiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val apiConfigRepository: ApiConfigRepository) : ViewModel() {
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

    fun setApiKey(value: String) { _apiKey.value = value }
    fun setBaseUrl(value: String) { _baseUrl.value = value }
    fun setModelName(value: String) { _modelName.value = value }
    fun setTemperature(value: Float) { _temperature.value = value }
    fun setTopP(value: Float) { _topP.value = value }
    fun setMaxTokens(value: Int) { _maxTokens.value = value }

    fun saveConfig() {
        viewModelScope.launch {
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
            _saveStatus.value = "Configurações para ${provider.displayName} salvas com criptografia Keystore!"
        }
    }
}
