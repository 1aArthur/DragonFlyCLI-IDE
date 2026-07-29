package com.example.data.repository

import com.example.data.db.dao.ApiConfigDao
import com.example.data.db.entities.ApiConfigEntity
import com.example.data.security.SecurePreferences
import com.example.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

class ApiConfigRepository(
    private val apiConfigDao: ApiConfigDao,
    private val securePreferences: SecurePreferences
) {
    val allConfigs: Flow<List<ApiConfigEntity>> = apiConfigDao.getAllConfigs()

    suspend fun getApiConfig(provider: AiProvider): ApiConfigEntity {
        val stored = apiConfigDao.getConfigForProvider(provider.name)
        val secureKey = securePreferences.getEncryptedString("KEY_${provider.name}")

        return if (stored != null) {
            stored.copy(apiKey = if (secureKey.isNotBlank()) secureKey else stored.apiKey)
        } else {
            ApiConfigEntity(
                providerKey = provider.name,
                apiKey = secureKey,
                baseUrl = provider.defaultBaseUrl,
                defaultModel = provider.defaultModel
            )
        }
    }

    suspend fun saveApiConfig(
        provider: AiProvider,
        apiKey: String,
        baseUrl: String,
        defaultModel: String,
        temperature: Float,
        topP: Float,
        maxTokens: Int,
        timeoutSec: Int
    ) {
        securePreferences.saveEncryptedString("KEY_${provider.name}", apiKey)

        val entity = ApiConfigEntity(
            providerKey = provider.name,
            apiKey = "", // Don't persist key in cleartext SQLite table
            baseUrl = baseUrl.ifBlank { provider.defaultBaseUrl },
            defaultModel = defaultModel.ifBlank { provider.defaultModel },
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            timeoutSec = timeoutSec,
            isEnabled = true
        )
        apiConfigDao.insertConfig(entity)
    }
}
