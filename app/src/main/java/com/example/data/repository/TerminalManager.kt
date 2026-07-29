package com.example.data.repository

import android.content.Context
import com.example.data.db.dao.CommandDao
import com.example.data.db.entities.CommandHistoryEntity
import com.example.utils.TermuxBridge
import com.example.utils.TermuxStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class LineType {
        COMMAND, OUTPUT, ERROR, SYSTEM, SUCCESS
    }
}

class TerminalManager(
    private val context: Context,
    private val commandDao: CommandDao
) {
    private var currentDirectory: File = context.filesDir

    private val _isTermuxMode = MutableStateFlow(false)
    val isTermuxMode: StateFlow<Boolean> = _isTermuxMode.asStateFlow()

    private val _termuxStatus = MutableStateFlow(TermuxBridge.checkTermuxStatus(context))
    val termuxStatus: StateFlow<TermuxStatus> = _termuxStatus.asStateFlow()

    private val _lines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("DragonflyCLI Terminal v2.5 initialized.", TerminalLine.LineType.SYSTEM),
            TerminalLine("Working directory: ${currentDirectory.absolutePath}", TerminalLine.LineType.SYSTEM),
            TerminalLine("Type 'help' for available CLI commands.", TerminalLine.LineType.SYSTEM)
        )
    )
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _currentDirState = MutableStateFlow(currentDirectory.absolutePath)
    val currentDirState: StateFlow<String> = _currentDirState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var currentProcess: Process? = null

    fun getWorkingDirectory(): File = currentDirectory

    fun refreshTermuxStatus() {
        _termuxStatus.value = TermuxBridge.checkTermuxStatus(context)
    }

    fun setTermuxMode(enabled: Boolean) {
        _isTermuxMode.value = enabled
        if (enabled) {
            appendLine(TerminalLine("Modo Termux Bridge ativado. Comandos serão enviados via Intent com.termux.RUN_COMMAND.", TerminalLine.LineType.SYSTEM))
        } else {
            appendLine(TerminalLine("Modo de Shell Nativo Android reativado.", TerminalLine.LineType.SYSTEM))
        }
    }

    suspend fun executeCommand(commandStr: String) = withContext(Dispatchers.IO) {
        val trimmed = commandStr.trim()
        if (trimmed.isEmpty()) return@withContext

        appendLine(TerminalLine("$ ${trimmed}", TerminalLine.LineType.COMMAND))

        // Check if command explicitly uses termux: prefix or Termux mode is enabled
        if (trimmed.startsWith("termux:") || _isTermuxMode.value) {
            val actualCmd = if (trimmed.startsWith("termux:")) trimmed.removePrefix("termux:").trim() else trimmed
            executeViaTermuxBridge(actualCmd)
            return@withContext
        }

        // Handle shell built-ins
        when {
            trimmed == "clear" -> {
                _lines.value = emptyList()
                return@withContext
            }
            trimmed == "pwd" -> {
                appendLine(TerminalLine(currentDirectory.absolutePath, TerminalLine.LineType.OUTPUT))
                saveHistory(trimmed, 0)
                return@withContext
            }
            trimmed == "help" -> {
                val helpMsg = """
                    Available commands & shortcuts:
                      cd <path>      : Change directory
                      ls [-la]       : List files in current directory
                      pwd            : Display current working path
                      cat <file>     : View file content
                      echo <text>    : Print text
                      mkdir <dir>    : Create new folder
                      rm [-r] <file> : Remove file or directory
                      touch <file>   : Create empty file
                      termux:<cmd>   : Dispatch command directly to Termux:API
                      python / node  : Script execution interface
                      git <subcmd>   : Git repository operator
                      clear          : Clear terminal screen
                      help           : Display this help prompt
                """.trimIndent()
                appendLine(TerminalLine(helpMsg, TerminalLine.LineType.SYSTEM))
                saveHistory(trimmed, 0)
                return@withContext
            }
            trimmed.startsWith("cd ") || trimmed == "cd" -> {
                handleCd(trimmed)
                saveHistory(trimmed, 0)
                return@withContext
            }
        }

        // Execute external process in local shell
        try {
            _isRunning.value = true
            val processBuilder = ProcessBuilder("sh", "-c", trimmed)
            processBuilder.directory(currentDirectory)
            processBuilder.environment()["PATH"] = System.getenv("PATH") ?: "/system/bin:/system/xbin"
            processBuilder.environment()["HOME"] = context.filesDir.absolutePath

            val process = processBuilder.start()
            currentProcess = process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                appendLine(TerminalLine(line ?: "", TerminalLine.LineType.OUTPUT))
            }

            var errLine: String?
            while (errReader.readLine().also { errLine = it } != null) {
                appendLine(TerminalLine(errLine ?: "", TerminalLine.LineType.ERROR))
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                appendLine(TerminalLine("Process finished with code 0", TerminalLine.LineType.SUCCESS))
            } else {
                appendLine(TerminalLine("Process exited with code $exitCode", TerminalLine.LineType.ERROR))
            }
            saveHistory(trimmed, exitCode)
        } catch (e: Exception) {
            appendLine(TerminalLine("Error executing command: ${e.localizedMessage}", TerminalLine.LineType.ERROR))
            saveHistory(trimmed, -1)
        } finally {
            currentProcess = null
            _isRunning.value = false
        }
    }

    private suspend fun executeViaTermuxBridge(commandStr: String) {
        val status = TermuxBridge.checkTermuxStatus(context)
        if (!status.isTermuxInstalled) {
            appendLine(TerminalLine("Erro Termux: O aplicativo Termux não está instalado neste dispositivo Android.", TerminalLine.LineType.ERROR))
            appendLine(TerminalLine("Instale o Termux e o Termux:API via F-Droid ou GitHub para habilitação do ambiente Linux completo.", TerminalLine.LineType.SYSTEM))
            saveHistory(commandStr, -1)
            return
        }

        appendLine(TerminalLine("[Termux Intent Bridge] Enviando comando com.termux.RUN_COMMAND: $commandStr", TerminalLine.LineType.SYSTEM))
        val success = TermuxBridge.executeShellScript(
            context = context,
            script = commandStr,
            workDir = "/data/data/com.termux/files/home",
            background = true
        )

        if (success) {
            appendLine(TerminalLine("[Termux Intent Bridge] Intent despachado com sucesso para o Termux Service.", TerminalLine.LineType.SUCCESS))
            saveHistory("termux:$commandStr", 0)
        } else {
            appendLine(TerminalLine("[Termux Intent Bridge] Falha ao despachar o Intent com.termux.RUN_COMMAND.", TerminalLine.LineType.ERROR))
            saveHistory("termux:$commandStr", -1)
        }
    }

    private fun handleCd(command: String) {
        val pathArg = command.removePrefix("cd").trim()
        if (pathArg.isEmpty() || pathArg == "~") {
            currentDirectory = context.filesDir
        } else {
            val target = if (pathArg.startsWith("/")) {
                File(pathArg)
            } else {
                File(currentDirectory, pathArg)
            }
            if (target.exists() && target.isDirectory) {
                currentDirectory = target.canonicalFile
            } else {
                appendLine(TerminalLine("cd: no such file or directory: $pathArg", TerminalLine.LineType.ERROR))
                return
            }
        }
        _currentDirState.value = currentDirectory.absolutePath
        appendLine(TerminalLine("Switched to ${currentDirectory.absolutePath}", TerminalLine.LineType.SYSTEM))
    }

    fun killCurrentProcess() {
        currentProcess?.destroy()
        _isRunning.value = false
        appendLine(TerminalLine("Process terminated by user.", TerminalLine.LineType.ERROR))
    }

    private fun appendLine(line: TerminalLine) {
        _lines.value = _lines.value + line
    }

    private suspend fun saveHistory(cmd: String, exitCode: Int) {
        try {
            commandDao.insertCommand(
                CommandHistoryEntity(
                    command = cmd,
                    workingDirectory = currentDirectory.absolutePath,
                    exitCode = exitCode,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Ignore DB log errors
        }
    }

    fun getAutocompleteSuggestions(prefix: String): List<String> {
        val trimmed = prefix.trim()
        val files = currentDirectory.listFiles()?.map { it.name } ?: emptyList()
        val builtIns = listOf("ls", "cd", "pwd", "cat", "mkdir", "rm", "git", "python", "node", "clear", "help")
        
        return (builtIns + files).filter { it.startsWith(trimmed, ignoreCase = true) }.distinct()
    }
}
