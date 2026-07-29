package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Chat IA", Icons.Default.Chat)
    object Terminal : Screen("terminal", "Terminal", Icons.Default.Terminal)
    object Editor : Screen("editor", "Editor", Icons.Default.Code)
    object Files : Screen("files", "Arquivos", Icons.Default.Folder)
    object Agents : Screen("agents", "Agentes", Icons.Default.SmartToy)
    object Git : Screen("git", "Git", Icons.Default.AccountTree)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)

    companion object {
        val allScreens: List<Screen>
            get() = listOf(Chat, Terminal, Editor, Files, Agents, Git, Settings)
    }
}
