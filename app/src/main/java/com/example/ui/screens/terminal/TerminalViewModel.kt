package com.example.ui.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TerminalLine
import com.example.data.repository.TerminalManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel(val terminalManager: TerminalManager) : ViewModel() {
    val lines: StateFlow<List<TerminalLine>> = terminalManager.lines
    val currentDir: StateFlow<String> = terminalManager.currentDirState
    val isRunning: StateFlow<Boolean> = terminalManager.isRunning

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    fun runCommand(cmd: String) {
        viewModelScope.launch {
            terminalManager.executeCommand(cmd)
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

    fun killProcess() {
        terminalManager.killCurrentProcess()
    }
}
