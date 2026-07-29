package com.example.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

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
        }
    }

    fun saveActiveFile() {
        val index = _activeTabIndex.value
        val currentTabs = _tabs.value.toMutableList()
        if (index in currentTabs.indices) {
            val current = currentTabs[index]
            viewModelScope.launch {
                fileRepository.writeFileText(current.path, current.content)
                currentTabs[index] = current.copy(isDirty = false)
                _tabs.value = currentTabs
            }
        }
    }

    fun closeTab(index: Int) {
        val currentTabs = _tabs.value.toMutableList()
        if (index in currentTabs.indices) {
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
