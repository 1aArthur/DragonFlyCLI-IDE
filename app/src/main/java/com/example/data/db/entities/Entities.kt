package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    val model: String,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val systemPrompt: String = "You are DragonflyCLI, an expert AI assistant, developer, terminal operator, and software architect.",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val role: String, // "user", "assistant", "system", "tool"
    val content: String,
    val provider: String,
    val model: String,
    val codeSnippet: String? = null,
    val commandExecuted: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val workingDirectory: String,
    val exitCode: Int = 0,
    val output: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "file_bookmarks")
data class BookmarkEntity(
    @PrimaryKey val filePath: String,
    val alias: String,
    val isDirectory: Boolean,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val commands: String, // Pipe or newline separated commands
    val triggerType: String = "MANUAL", // "MANUAL", "ON_APP_START", "SCHEDULED"
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey val providerKey: String,
    val apiKey: String = "",
    val baseUrl: String = "",
    val defaultModel: String = "",
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val maxTokens: Int = 4096,
    val timeoutSec: Int = 30,
    val isCustomProxy: Boolean = false,
    val isEnabled: Boolean = true
)
