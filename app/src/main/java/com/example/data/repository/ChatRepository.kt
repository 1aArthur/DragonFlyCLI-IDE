package com.example.data.repository

import com.example.data.db.dao.ChatDao
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.ConversationEntity
import com.example.data.network.AiApiClient
import com.example.data.network.AiRequestOptions
import com.example.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiConfigRepository: ApiConfigRepository,
    private val aiApiClient: AiApiClient
) {
    val conversations: Flow<List<ConversationEntity>> = chatDao.getAllConversations()

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun createConversation(
        title: String,
        provider: AiProvider = AiProvider.GEMINI,
        model: String = provider.defaultModel
    ): ConversationEntity {
        val newConv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            provider = provider.name,
            model = model,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertConversation(newConv)
        return newConv
    }

    suspend fun sendMessage(
        conversationId: String,
        userMessageText: String,
        provider: AiProvider,
        model: String
    ): ChatMessageEntity {
        // Save user message
        val userMsgEntity = ChatMessageEntity(
            conversationId = conversationId,
            role = "user",
            content = userMessageText,
            provider = provider.name,
            model = model
        )
        chatDao.insertMessage(userMsgEntity)

        // Get chat history
        val history = chatDao.getMessagesForConversation(conversationId).first()
        val historyPairs = history.map { Pair(it.role, it.content) }

        // Fetch provider config
        val apiConfig = apiConfigRepository.getApiConfig(provider)
        val options = AiRequestOptions(
            provider = provider,
            model = model.ifBlank { apiConfig.defaultModel.ifBlank { provider.defaultModel } },
            apiKey = apiConfig.apiKey,
            baseUrl = apiConfig.baseUrl,
            temperature = apiConfig.temperature,
            topP = apiConfig.topP,
            maxTokens = apiConfig.maxTokens
        )

        val aiResponseText = aiApiClient.sendMessage(historyPairs, options)

        val assistantMsgEntity = ChatMessageEntity(
            conversationId = conversationId,
            role = "assistant",
            content = aiResponseText,
            provider = provider.name,
            model = model
        )
        chatDao.insertMessage(assistantMsgEntity)

        // Update conversation timestamp
        chatDao.getConversationById(conversationId)?.let { conv ->
            chatDao.insertConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }

        return assistantMsgEntity
    }

    suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteMessagesForConversation(conversationId)
        chatDao.deleteConversationById(conversationId)
    }
}
