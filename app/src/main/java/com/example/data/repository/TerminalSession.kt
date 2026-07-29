package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    initialTitle: String,
    initialDir: File
) {
    private val _title = MutableStateFlow(initialTitle)
    val title: StateFlow<String> = _title.asStateFlow()

    private val _lines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("$initialTitle inicializado.", TerminalLine.LineType.SYSTEM),
            TerminalLine("Diretório de trabalho: ${initialDir.absolutePath}", TerminalLine.LineType.SYSTEM),
            TerminalLine("Digite 'help' para comandos do DragonflyCLI.", TerminalLine.LineType.SYSTEM)
        )
    )
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _currentDir = MutableStateFlow(initialDir.absolutePath)
    val currentDir: StateFlow<String> = _currentDir.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isTermuxMode = MutableStateFlow(false)
    val isTermuxMode: StateFlow<Boolean> = _isTermuxMode.asStateFlow()

    var workingDirFile: File = initialDir
        private set

    var currentProcess: Process? = null

    fun setTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun setTermuxMode(enabled: Boolean) {
        _isTermuxMode.value = enabled
        if (enabled) {
            appendLine(TerminalLine("Modo Termux Bridge ativado.", TerminalLine.LineType.SYSTEM))
        } else {
            appendLine(TerminalLine("Modo Shell Nativo Android ativado.", TerminalLine.LineType.SYSTEM))
        }
    }

    fun setWorkingDirectory(dir: File) {
        workingDirFile = dir
        _currentDir.value = dir.absolutePath
    }

    fun appendLine(line: TerminalLine) {
        _lines.value = _lines.value + line
    }

    fun clearLines() {
        _lines.value = emptyList()
    }

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }
}
