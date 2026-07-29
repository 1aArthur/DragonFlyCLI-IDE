package com.example.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.BlackHoleStarsBackground
import com.example.ui.components.BottomTabBar
import com.example.ui.components.GlassCard
import com.example.ui.screens.agents.AgentsScreen
import com.example.ui.screens.agents.AgentsViewModel
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.editor.CodeEditorScreen
import com.example.ui.screens.editor.CodeEditorViewModel
import com.example.ui.screens.files.FileManagerScreen
import com.example.ui.screens.files.FileManagerViewModel
import com.example.ui.screens.git.GitScreen
import com.example.ui.screens.git.GitViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.terminal.TerminalScreen
import com.example.ui.screens.terminal.TerminalViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    chatViewModel: ChatViewModel,
    terminalViewModel: TerminalViewModel,
    editorViewModel: CodeEditorViewModel,
    fileManagerViewModel: FileManagerViewModel,
    agentsViewModel: AgentsViewModel,
    gitViewModel: GitViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Chat.route

    val termuxStatus by terminalViewModel.terminalManager.termuxStatus.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val screenTitle = when (currentRoute) {
        Screen.Chat.route -> "Chat Assistant AI"
        Screen.Terminal.route -> "Linux Terminal CLI"
        Screen.Editor.route -> "Code Editor"
        Screen.Files.route -> "File Explorer"
        Screen.Agents.route -> "Autonomous Agents"
        Screen.Git.route -> "Git Control"
        Screen.Settings.route -> "System Settings"
        else -> "DragonflyCLI"
    }

    BlackHoleStarsBackground {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    currentRoute = currentRoute,
                    termuxStatus = termuxStatus,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        backgroundColor = Color(0xCC0D0F18),
                        borderColor = DarkCardBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir Menu Lateral",
                                        tint = GlowCyan
                                    )
                                }

                                Column {
                                    Text(
                                        text = screenTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "DragonflyCLI IDE",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (termuxStatus.isTermuxInstalled) {
                                Surface(
                                    color = Color(0xFF0D2818),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, TerminalGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Terminal, null, tint = TerminalGreen, modifier = Modifier.size(10.dp))
                                        Text("Termux Ready", fontSize = 9.sp, color = TerminalGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    BottomTabBar(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route
                    ) {
                        composable(Screen.Chat.route) {
                            ChatScreen(
                                viewModel = chatViewModel,
                                onRunInTerminal = { cmd ->
                                    terminalViewModel.runCommand(cmd)
                                    navController.navigate(Screen.Terminal.route)
                                },
                                onOpenInEditor = { code ->
                                    val root = fileManagerViewModel.fileRepository.getRootDirectory()
                                    val tempFile = java.io.File(root, "ai_snippet.kt")
                                    tempFile.writeText(code)
                                    editorViewModel.openFile(tempFile.absolutePath)
                                    navController.navigate(Screen.Editor.route)
                                }
                            )
                        }

                        composable(Screen.Terminal.route) {
                            TerminalScreen(viewModel = terminalViewModel)
                        }

                        composable(Screen.Editor.route) {
                            CodeEditorScreen(
                                viewModel = editorViewModel,
                                onAskAiAboutCode = { prompt ->
                                    chatViewModel.sendMessage(prompt)
                                    navController.navigate(Screen.Chat.route)
                                }
                            )
                        }

                        composable(Screen.Files.route) {
                            FileManagerScreen(
                                viewModel = fileManagerViewModel,
                                onOpenFileInEditor = { path ->
                                    editorViewModel.openFile(path)
                                    navController.navigate(Screen.Editor.route)
                                },
                                onRunFileInTerminal = { cmd ->
                                    terminalViewModel.runCommand(cmd)
                                    navController.navigate(Screen.Terminal.route)
                                },
                                onAskAiAboutFile = { prompt ->
                                    chatViewModel.sendMessage(prompt)
                                    navController.navigate(Screen.Chat.route)
                                }
                            )
                        }

                        composable(Screen.Agents.route) {
                            AgentsScreen(viewModel = agentsViewModel)
                        }

                        composable(Screen.Git.route) {
                            GitScreen(
                                viewModel = gitViewModel,
                                onOpenTerminal = { navController.navigate(Screen.Terminal.route) }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}

