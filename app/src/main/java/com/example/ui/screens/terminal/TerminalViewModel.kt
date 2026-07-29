package com.example.ui.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TerminalLine
import com.example.data.repository.TerminalManager
import com.example.data.repository.TerminalSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel(val terminalManager: TerminalManager) : ViewModel() {
    val sessions: StateFlow<List<TerminalSession>> = terminalManager.sessions
    val activeSessionId: StateFlow<String> = terminalManager.activeSessionId

    val lines: StateFlow<List<TerminalLine>> = terminalManager.lines
    val currentDir: StateFlow<String> = terminalManager.currentDirState
    val isRunning: StateFlow<Boolean> = terminalManager.isRunning
    val isTermuxMode: StateFlow<Boolean> = terminalManager.isTermuxMode

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    fun createNewSession(title: String? = null) {
        terminalManager.createNewSession(title)
    }

    fun selectSession(id: String) {
        terminalManager.selectSession(id)
    }

    fun closeSession(id: String) {
        terminalManager.closeSession(id)
    }

    fun renameSession(id: String, newName: String) {
        terminalManager.renameSession(id, newName)
    }

    fun runCommand(cmd: String, targetSessionId: String? = null) {
        viewModelScope.launch {
            terminalManager.executeCommand(cmd, targetSessionId)
            _suggestions.value = emptyList()
        }
    }

    fun onInputChanged(input: String) {
        if (input.isBlank()) {
            _suggestions.value = emptyList()
        } else {
            _suggestions.value = terminalManager.getAutocompleteSuggestions(input)
        }
    }

    fun killProcess(targetSessionId: String? = null) {
        terminalManager.killCurrentProcess(targetSessionId)
    }
}
