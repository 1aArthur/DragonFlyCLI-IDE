package com.example.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EditorTab(
    val path: String,
    val name: String,
    val content: String,
    val isDirty: Boolean = false
)

class CodeEditorViewModel(private val fileRepository: FileRepository) : ViewModel() {
    private val _tabs = MutableStateFlow<List<EditorTab>>(emptyList())
    val tabs: StateFlow<List<EditorTab>> = _tabs.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var autoSaveJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        // Open default starter file
        viewModelScope.launch {
            val root = fileRepository.getRootDirectory()
            val sampleFile = File(root, "main.kt")
            if (!sampleFile.exists()) {
                val sampleCode = "// DragonflyCLI-IDE Sample Code\n" +
                        "fun main() {\n" +
                        "    val appName = \"DragonflyCLI-IDE\"\n" +
                        "    println(\"Welcome to \$appName - Mobile AI & Terminal Studio\")\n" +
                        "}"
                fileRepository.writeFileText(sampleFile.absolutePath, sampleCode)
            }
            openFile(sampleFile.absolutePath)
        }
    }

    fun createNewFileTab(fileName: String) {
        viewModelScope.launch {
            val root = fileRepository.getRootDirectory()
            val cleanName = if (fileName.contains(".")) fileName else "$fileName.kt"
            val newFile = File(root, cleanName)
            if (!newFile.exists()) {
                fileRepository.writeFileText(newFile.absolutePath, "// Novo Arquivo: $cleanName\n")
            }
            openFile(newFile.absolutePath)
            _statusMessage.value = "Arquivo '$cleanName' criado no editor."
        }
    }

    fun formatActiveCode() {
        val index = _activeTabIndex.value
        val currentTabs = _tabs.value.toMutableList()
        if (index in currentTabs.indices) {
            val current = currentTabs[index]
            val lines = current.content.lines()
            var indentLevel = 0
            val formattedLines = lines.map { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("}") || trimmed.startsWith(")")) {
                    indentLevel = (indentLevel - 1).coerceAtLeast(0)
                }
                val indentStr = "    ".repeat(indentLevel)
                val formattedLine = if (trimmed.isEmpty()) "" else "$indentStr$trimmed"
                if (trimmed.endsWith("{") || trimmed.endsWith("(")) {
                    indentLevel++
                }
                formattedLine
            }
            val formattedCode = formattedLines.joinToString("\n")
            currentTabs[index] = current.copy(content = formattedCode, isDirty = true)
            _tabs.value = currentTabs
            _statusMessage.value = "Código formatado."
            scheduleAutoSave(index)
        }
    }

    fun openFile(filePath: String) {
        viewModelScope.launch {
            val existingIndex = _tabs.value.indexOfFirst { it.path == filePath }
            if (existingIndex >= 0) {
                _activeTabIndex.value = existingIndex
                return@launch
            }

            val text = fileRepository.readFileText(filePath)
            val name = File(filePath).name
            val newTab = EditorTab(path = filePath, name = name, content = text)
            _tabs.value = _tabs.value + newTab
            _activeTabIndex.value = _tabs.value.size - 1
        }
    }

    fun updateActiveContent(newContent: String) {
        val index = _activeTabIndex.value
        val currentTabs = _tabs.value.toMutableList()
        if (index in currentTabs.indices) {
            val current = currentTabs[index]
            currentTabs[index] = current.copy(content = newContent, isDirty = true)
            _tabs.value = currentTabs
            _statusMessage.value = "Digitando... (Auto-salvamento em breve)"
            scheduleAutoSave(index)
        }
    }

    private fun scheduleAutoSave(tabIndex: Int, delayMs: Long = 1000L) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(delayMs)
            saveTab(tabIndex, isAutoSave = true)
        }
    }

    private fun saveTab(index: Int, isAutoSave: Boolean = false) {
        val currentTabs = _tabs.value.toMutableList()
        if (index in currentTabs.indices) {
            val current = currentTabs[index]
            if (current.isDirty) {
                viewModelScope.launch {
                    fileRepository.writeFileText(current.path, current.content)
                    val updatedTabs = _tabs.value.toMutableList()
                    if (index in updatedTabs.indices) {
                        updatedTabs[index] = updatedTabs[index].copy(isDirty = false)
                        _tabs.value = updatedTabs
                        val timeStr = timeFormat.format(Date())
                        _statusMessage.value = if (isAutoSave) "✔ Auto-salvo às $timeStr (${current.name})" else "✔ Salvo com sucesso (${current.name})"
                    }
                }
            }
        }
    }

    fun saveActiveFile() {
        autoSaveJob?.cancel()
        saveTab(_activeTabIndex.value, isAutoSave = false)
    }

    fun closeTab(index: Int) {
        autoSaveJob?.cancel()
        val currentTabs = _tabs.value.toMutableList()
        if (index in currentTabs.indices) {
            val current = currentTabs[index]
            if (current.isDirty) {
                viewModelScope.launch {
                    fileRepository.writeFileText(current.path, current.content)
                }
            }
            currentTabs.removeAt(index)
            _tabs.value = currentTabs
            if (_activeTabIndex.value >= currentTabs.size) {
                _activeTabIndex.value = (currentTabs.size - 1).coerceAtLeast(0)
            }
        }
    }

    fun selectTab(index: Int) {
        if (index in _tabs.value.indices) {
            _activeTabIndex.value = index
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
