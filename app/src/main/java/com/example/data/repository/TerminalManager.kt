package com.example.data.repository

import android.content.Context
import com.example.data.db.dao.CommandDao
import com.example.data.db.entities.CommandHistoryEntity
import com.example.utils.TermuxBridge
import com.example.utils.TermuxStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
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

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalManager(
    private val context: Context,
    private val commandDao: CommandDao
) {
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _sessions = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessions: StateFlow<List<TerminalSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String>("")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val _termuxStatus = MutableStateFlow(TermuxBridge.checkTermuxStatus(context))
    val termuxStatus: StateFlow<TermuxStatus> = _termuxStatus.asStateFlow()

    val activeSession: TerminalSession?
        get() = _sessions.value.find { it.id == _activeSessionId.value } ?: _sessions.value.firstOrNull()

    private val activeSessionFlow: Flow<TerminalSession?> = combine(_sessions, _activeSessionId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }

    // Backward compatible StateFlows for single-session callers
    val lines: StateFlow<List<TerminalLine>> = activeSessionFlow
        .flatMapLatest { session -> session?.lines ?: flowOf(emptyList()) }
        .stateIn(managerScope, SharingStarted.Eagerly, emptyList())

    val currentDirState: StateFlow<String> = activeSessionFlow
        .flatMapLatest { session -> session?.currentDir ?: flowOf(context.filesDir.absolutePath) }
        .stateIn(managerScope, SharingStarted.Eagerly, context.filesDir.absolutePath)

    val isRunning: StateFlow<Boolean> = activeSessionFlow
        .flatMapLatest { session -> session?.isRunning ?: flowOf(false) }
        .stateIn(managerScope, SharingStarted.Eagerly, false)

    val isTermuxMode: StateFlow<Boolean> = activeSessionFlow
        .flatMapLatest { session -> session?.isTermuxMode ?: flowOf(false) }
        .stateIn(managerScope, SharingStarted.Eagerly, false)

    init {
        createNewSession("Terminal 1")
    }

    fun createNewSession(title: String? = null): TerminalSession {
        val baseDir = activeSession?.workingDirFile ?: context.filesDir
        val count = _sessions.value.size + 1
        val sessionTitle = title ?: "Terminal $count"
        val session = TerminalSession(
            initialTitle = sessionTitle,
            initialDir = baseDir
        )
        _sessions.value = _sessions.value + session
        _activeSessionId.value = session.id
        return session
    }

    fun closeSession(id: String) {
        val currentList = _sessions.value
        if (currentList.size <= 1) {
            val session = currentList.firstOrNull() ?: return
            session.clearLines()
            session.appendLine(TerminalLine("Sessão reiniciada.", TerminalLine.LineType.SYSTEM))
            return
        }

        val sessionToClose = currentList.find { it.id == id }
        sessionToClose?.currentProcess?.destroy()

        val newList = currentList.filter { it.id != id }
        _sessions.value = newList

        if (_activeSessionId.value == id) {
            _activeSessionId.value = newList.last().id
        }
    }

    fun selectSession(id: String) {
        if (_sessions.value.any { it.id == id }) {
            _activeSessionId.value = id
        }
    }

    fun renameSession(id: String, newName: String) {
        val session = _sessions.value.find { it.id == id }
        if (session != null && newName.isNotBlank()) {
            session.setTitle(newName)
        }
    }

    fun getWorkingDirectory(targetSessionId: String? = null): File {
        val target = if (targetSessionId != null) _sessions.value.find { it.id == targetSessionId } else activeSession
        return target?.workingDirFile ?: context.filesDir
    }

    fun refreshTermuxStatus() {
        _termuxStatus.value = TermuxBridge.checkTermuxStatus(context)
    }

    fun setTermuxMode(enabled: Boolean, targetSessionId: String? = null) {
        val target = if (targetSessionId != null) _sessions.value.find { it.id == targetSessionId } else activeSession
        target?.setTermuxMode(enabled)
    }

    suspend fun executeCommand(commandStr: String, targetSessionId: String? = null) = withContext(Dispatchers.IO) {
        val trimmed = commandStr.trim()
        if (trimmed.isEmpty()) return@withContext

        val session = (if (targetSessionId != null) _sessions.value.find { it.id == targetSessionId } else activeSession)
            ?: createNewSession()

        // Auto rename default session title to concise command name
        if (session.title.value.startsWith("Terminal ")) {
            val conciseName = trimmed.split(" ").firstOrNull() ?: trimmed
            session.setTitle(conciseName.take(15))
        }

        session.appendLine(TerminalLine("$ ${trimmed}", TerminalLine.LineType.COMMAND))

        // Check if command explicitly uses termux: prefix or Termux mode is enabled
        if (trimmed.startsWith("termux:") || session.isTermuxMode.value) {
            val actualCmd = if (trimmed.startsWith("termux:")) trimmed.removePrefix("termux:").trim() else trimmed
            executeViaTermuxBridge(session, actualCmd)
            return@withContext
        }

        // Handle shell built-ins
        when {
            trimmed == "clear" -> {
                session.clearLines()
                return@withContext
            }
            trimmed == "pwd" -> {
                session.appendLine(TerminalLine(session.workingDirFile.absolutePath, TerminalLine.LineType.OUTPUT))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed == "native-opt" || trimmed == "opt-status" -> {
                val stats = com.example.utils.performance.NativePerformanceEngine.getMemoryStats()
                val binFiles = File(context.filesDir, "bin").listFiles()?.joinToString { it.name } ?: "Nenhum"
                val msg = """
                    [Native Multi-Language Performance Engine v2.5]
                    Status: ATIVO
                    $stats
                    Ferramentas Nativas em /bin: $binFiles
                    Última renderização LRU: ${com.example.utils.performance.NativePerformanceEngine.lastExecutionTimeMs} ms
                """.trimIndent()
                session.appendLine(TerminalLine(msg, TerminalLine.LineType.SUCCESS))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed == "benchmark" -> {
                session.appendLine(TerminalLine("⚡ Executando Benchmark do Motor Nativo C/Python/Shell...", TerminalLine.LineType.SYSTEM))
                val start = System.nanoTime()
                val scanResults = com.example.utils.performance.NativePerformanceEngine.performFastWorkspaceScan(session.workingDirFile, "")
                val elapsed = (System.nanoTime() - start) / 1_000_000.0
                val stats = com.example.utils.performance.NativePerformanceEngine.getMemoryStats()
                val resMsg = """
                    ✔ Benchmark concluído em ${String.format("%.2f", elapsed)} ms
                    Arquivos varridos em alta velocidade: ${scanResults.size}
                    $stats
                """.trimIndent()
                session.appendLine(TerminalLine(resMsg, TerminalLine.LineType.SUCCESS))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed.startsWith("cargo ") || trimmed == "cargo" || trimmed.startsWith("cargo-ndk") -> {
                val output = com.example.utils.rust.RustCargoNdkEngine.handleCargoCommand(trimmed, session.workingDirFile)
                session.appendLine(TerminalLine(output, TerminalLine.LineType.SUCCESS))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed.startsWith("wasm ") || trimmed == "wasm" -> {
                val parts = trimmed.split("\\s+".toRegex())
                val subCmd = parts.getOrNull(1) ?: "help"
                val fileArg = parts.getOrNull(2) ?: "module.wasm"
                val targetFile = File(session.workingDirFile, fileArg)

                val outMsg = when (subCmd) {
                    "inspect" -> com.example.utils.wasm.WasmRuntimeEngine.inspectWasmFile(targetFile)
                    "run" -> com.example.utils.wasm.WasmRuntimeEngine.executeWasmModule(targetFile, parts.drop(3))
                    "gen", "init" -> com.example.utils.wasm.WasmRuntimeEngine.generateSampleWasmModule(session.workingDirFile, fileArg)
                    else -> """
                        🔮 [WASM Runtime Engine] Options:
                          wasm inspect <file.wasm> : Analyze WASM headers and imports/exports
                          wasm run <file.wasm>     : Execute WebAssembly module in WASI environment
                          wasm gen [module.wat]    : Generate WebAssembly text format sample module
                        """.trimIndent()
                }
                session.appendLine(TerminalLine(outMsg, TerminalLine.LineType.SUCCESS))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed.startsWith("lsp ") || trimmed == "lsp" -> {
                val parts = trimmed.split("\\s+".toRegex())
                val subCmd = parts.getOrNull(1) ?: "status"
                val outMsg = when (subCmd) {
                    "status" -> com.example.utils.lsp.LspServerBridge.getStatusSummary()
                    "diag", "diagnostics" -> {
                        val ktFiles = session.workingDirFile.walkTopDown().filter { it.extension in listOf("kt", "rs", "py", "c") }.take(5).toList()
                        val diagLines = ktFiles.flatMap { f ->
                            com.example.utils.lsp.LspServerBridge.getDiagnostics(f, f.readText()).map { "${f.name}:${it.line}:${it.character} [${it.severity}] ${it.message}" }
                        }
                        if (diagLines.isEmpty()) "✔ LSP Diagnostics: 0 errors, 0 warnings found." else diagLines.joinToString("\n")
                    }
                    else -> "LSP Commands: lsp status | lsp diag"
                }
                session.appendLine(TerminalLine(outMsg, TerminalLine.LineType.SUCCESS))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed.startsWith("profile") || trimmed.startsWith("profiler") -> {
                val parts = trimmed.split("\\s+".toRegex())
                val subCmd = parts.getOrNull(1) ?: "report"
                val outMsg = when (subCmd) {
                    "start" -> com.example.utils.profiling.PerformanceProfiler.startProfiling()
                    "stop" -> com.example.utils.profiling.PerformanceProfiler.stopProfiling().toFormattedString()
                    else -> com.example.utils.profiling.PerformanceProfiler.getQuickReportSummary()
                }
                session.appendLine(TerminalLine(outMsg, TerminalLine.LineType.SUCCESS))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed == "help" -> {
                val helpMsg = """
                    Available commands & shortcuts:
                      cargo [subcmd] : Rust & Cargo-NDK cross-compilation engine (build, check, ndk, init)
                      wasm [subcmd]  : WebAssembly runtime engine & disassembler (inspect, run, gen)
                      lsp [subcmd]   : Language Server Protocol diagnostics & status (status, diag)
                      profile        : Performance Profiling engine (start, stop, report)
                      cd <path>      : Change directory
                      ls [-la]       : List files in current directory
                      pwd            : Display current working path
                      cat <file>     : View file content
                      mkdir <dir>    : Create new folder
                      rm [-r] <file> : Remove file or directory
                      touch <file>   : Create empty file
                      native-opt     : Visualizar estatísticas do motor nativo C/Python
                      benchmark      : Executar teste de desempenho
                      termux:<cmd>   : Dispatch command directly to Termux:API
                      python / node  : Script execution interface
                      git <subcmd>   : Git repository operator
                      clear          : Clear terminal screen
                      help           : Display this help prompt
                """.trimIndent()
                session.appendLine(TerminalLine(helpMsg, TerminalLine.LineType.SYSTEM))
                saveHistory(session, trimmed, 0)
                return@withContext
            }
            trimmed.startsWith("cd ") || trimmed == "cd" -> {
                handleCd(session, trimmed)
                saveHistory(session, trimmed, 0)
                return@withContext
            }
        }

        // Execute external process in local shell for this session
        try {
            session.setRunning(true)
            val processBuilder = ProcessBuilder("sh", "-c", trimmed)
            processBuilder.directory(session.workingDirFile)
            processBuilder.environment()["PATH"] = System.getenv("PATH") ?: "/system/bin:/system/xbin"
            processBuilder.environment()["HOME"] = context.filesDir.absolutePath

            val process = processBuilder.start()
            session.currentProcess = process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                session.appendLine(TerminalLine(line ?: "", TerminalLine.LineType.OUTPUT))
            }

            var errLine: String?
            while (errReader.readLine().also { errLine = it } != null) {
                session.appendLine(TerminalLine(errLine ?: "", TerminalLine.LineType.ERROR))
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                session.appendLine(TerminalLine("Process finished with code 0", TerminalLine.LineType.SUCCESS))
            } else {
                session.appendLine(TerminalLine("Process exited with code $exitCode", TerminalLine.LineType.ERROR))
            }
            saveHistory(session, trimmed, exitCode)
        } catch (e: Exception) {
            session.appendLine(TerminalLine("Error executing command: ${e.localizedMessage}", TerminalLine.LineType.ERROR))
            saveHistory(session, trimmed, -1)
        } finally {
            session.currentProcess = null
            session.setRunning(false)
        }
    }

    private suspend fun executeViaTermuxBridge(session: TerminalSession, commandStr: String) {
        val status = TermuxBridge.checkTermuxStatus(context)
        if (!status.isTermuxInstalled) {
            session.appendLine(TerminalLine("Erro Termux: O aplicativo Termux não está instalado neste dispositivo Android.", TerminalLine.LineType.ERROR))
            session.appendLine(TerminalLine("Instale o Termux e o Termux:API via F-Droid ou GitHub para habilitação do ambiente Linux completo.", TerminalLine.LineType.SYSTEM))
            saveHistory(session, commandStr, -1)
            return
        }

        session.appendLine(TerminalLine("[Termux Intent Bridge] Enviando comando com.termux.RUN_COMMAND: $commandStr", TerminalLine.LineType.SYSTEM))
        val success = TermuxBridge.executeShellScript(
            context = context,
            script = commandStr,
            workDir = "/data/data/com.termux/files/home",
            background = true
        )

        if (success) {
            session.appendLine(TerminalLine("[Termux Intent Bridge] Intent despachado com sucesso para o Termux Service.", TerminalLine.LineType.SUCCESS))
            saveHistory(session, "termux:$commandStr", 0)
        } else {
            session.appendLine(TerminalLine("[Termux Intent Bridge] Falha ao despachar o Intent com.termux.RUN_COMMAND.", TerminalLine.LineType.ERROR))
            saveHistory(session, "termux:$commandStr", -1)
        }
    }

    private fun handleCd(session: TerminalSession, command: String) {
        val pathArg = command.removePrefix("cd").trim()
        if (pathArg.isEmpty() || pathArg == "~") {
            session.setWorkingDirectory(context.filesDir)
        } else {
            val target = if (pathArg.startsWith("/")) {
                File(pathArg)
            } else {
                File(session.workingDirFile, pathArg)
            }
            if (target.exists() && target.isDirectory) {
                session.setWorkingDirectory(target.canonicalFile)
            } else {
                session.appendLine(TerminalLine("cd: no such file or directory: $pathArg", TerminalLine.LineType.ERROR))
                return
            }
        }
        session.appendLine(TerminalLine("Switched to ${session.workingDirFile.absolutePath}", TerminalLine.LineType.SYSTEM))
    }

    fun killCurrentProcess(targetSessionId: String? = null) {
        val session = if (targetSessionId != null) _sessions.value.find { it.id == targetSessionId } else activeSession
        if (session != null) {
            session.currentProcess?.destroy()
            session.setRunning(false)
            session.appendLine(TerminalLine("Process terminated by user.", TerminalLine.LineType.ERROR))
        }
    }

    private suspend fun saveHistory(session: TerminalSession, cmd: String, exitCode: Int) {
        try {
            commandDao.insertCommand(
                CommandHistoryEntity(
                    command = cmd,
                    workingDirectory = session.workingDirFile.absolutePath,
                    exitCode = exitCode,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Ignore DB log errors
        }
    }

    fun getAutocompleteSuggestions(prefix: String, targetSessionId: String? = null): List<String> {
        val trimmed = prefix.trim()
        val workDir = getWorkingDirectory(targetSessionId)
        val files = workDir.listFiles()?.map { it.name } ?: emptyList()
        val builtIns = listOf("ls", "cd", "pwd", "cat", "mkdir", "rm", "git", "python", "node", "clear", "help")
        
        return (builtIns + files).filter { it.startsWith(trimmed, ignoreCase = true) }.distinct()
    }
}
