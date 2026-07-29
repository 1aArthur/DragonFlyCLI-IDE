package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.network.AiApiClient
import com.example.data.repository.*
import com.example.data.security.SecurePreferences
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.agents.AgentsViewModel
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.editor.CodeEditorViewModel
import com.example.ui.screens.files.FileManagerViewModel
import com.example.ui.screens.git.GitViewModel
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.terminal.TerminalViewModel
import com.example.ui.theme.BlackHoleBackground
import com.example.ui.theme.DragonflyTheme
import com.example.utils.GitHelper

import androidx.lifecycle.lifecycleScope
import com.example.utils.performance.NativePerformanceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Deploy Native C / Python / Shell performance optimizer scripts & Rust toolchain & Wasmtime engine in background thread
        lifecycleScope.launch(Dispatchers.IO) {
            NativePerformanceEngine.deployNativeOptimizerTools(applicationContext)
            com.example.utils.rust.RustCargoNdkEngine.initializeRustEnvironment(applicationContext)
            com.example.utils.wasm.WasmtimeEngine.initializeWasmtimeEnvironment(applicationContext)
        }

        // Core singletons and database initialization
        val database = AppDatabase.getDatabase(this)
        val securePreferences = SecurePreferences(this)
        val aiApiClient = AiApiClient()

        val apiConfigRepository = ApiConfigRepository(database.apiConfigDao(), securePreferences)
        val chatRepository = ChatRepository(database.chatDao(), apiConfigRepository, aiApiClient)
        val terminalManager = TerminalManager(this, database.commandDao())
        val fileRepository = FileRepository(this, database.bookmarkDao())
        val workflowRepository = WorkflowRepository(database.workflowDao(), terminalManager)
        val gitHelper = GitHelper(terminalManager)

        // ViewModels
        val chatViewModel = ChatViewModel(chatRepository)
        val terminalViewModel = TerminalViewModel(terminalManager)
        val editorViewModel = CodeEditorViewModel(fileRepository)
        val fileManagerViewModel = FileManagerViewModel(fileRepository)
        val agentsViewModel = AgentsViewModel(workflowRepository, terminalManager, chatRepository)
        val gitViewModel = GitViewModel(gitHelper)
        val settingsViewModel = SettingsViewModel(apiConfigRepository)

        setContent {
            DragonflyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BlackHoleBackground
                ) {
                    AppNavigation(
                        chatViewModel = chatViewModel,
                        terminalViewModel = terminalViewModel,
                        editorViewModel = editorViewModel,
                        fileManagerViewModel = fileManagerViewModel,
                        agentsViewModel = agentsViewModel,
                        gitViewModel = gitViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
