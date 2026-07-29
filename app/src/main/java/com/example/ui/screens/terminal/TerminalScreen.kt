package com.example.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TerminalLine
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback

@Composable
fun TerminalScreen(viewModel: TerminalViewModel) {
    val haptic = rememberHapticFeedback()
    val lines by viewModel.lines.collectAsState()
    val currentDir by viewModel.currentDir.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val isTermuxMode by viewModel.terminalManager.isTermuxMode.collectAsState()
    val termuxStatus by viewModel.terminalManager.termuxStatus.collectAsState()

    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickCommands = if (isTermuxMode) {
        listOf("pkg update", "termux-toast Hello", "termux-battery-status", "uname -a", "clear", "help")
    } else {
        listOf("ls -la", "pwd", "git status", "python3 --version", "node -v", "clear", "help")
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        // Header Status Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Terminal, "Terminal", tint = GlowCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentDir,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Termux Mode Toggle Switch
                    FilterChip(
                        selected = isTermuxMode,
                        onClick = {
                            haptic(HapticType.HEAVY_CLICK)
                            viewModel.terminalManager.setTermuxMode(!isTermuxMode)
                        },
                        label = { Text("Termux Mode", fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0D2818),
                            selectedLabelColor = TerminalGreen,
                            containerColor = DarkSurface,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isTermuxMode,
                            selectedBorderColor = TerminalGreen,
                            borderColor = DarkCardBorder
                        )
                    )

                    if (isRunning) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TerminalYellow, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.killProcess() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Cancel, "Cancelar", tint = TerminalRed)
                            }
                        }
                    }
                }
            }
        }

        // Terminal Output Window
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            backgroundColor = DarkSurface,
            borderColor = DarkCardBorder
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(lines) { line ->
                    val color = when (line.type) {
                        TerminalLine.LineType.COMMAND -> GlowCyan
                        TerminalLine.LineType.OUTPUT -> TextPrimary
                        TerminalLine.LineType.ERROR -> TerminalRed
                        TerminalLine.LineType.SYSTEM -> ElectricBlue
                        TerminalLine.LineType.SUCCESS -> TerminalGreen
                    }

                    Text(
                        text = line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = color,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Auto-complete suggestion row
        if (suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(suggestions) { sugg ->
                    Surface(
                        color = Color(0xFF1E2638),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            haptic(HapticType.LIGHT_CLICK)
                            inputCommand = sugg
                        }
                    ) {
                        Text(
                            text = sugg,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = GlowCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick Command Shortcuts
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickCommands) { cmd ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.clickable {
                        haptic(HapticType.LIGHT_CLICK)
                        viewModel.runCommand(cmd)
                    }
                ) {
                    Text(
                        text = cmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Terminal Prompt Command Input
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$ ", fontFamily = FontFamily.Monospace, color = GlowCyan, fontSize = 14.sp)

                OutlinedTextField(
                    value = inputCommand,
                    onValueChange = {
                        haptic(HapticType.KEYPRESS)
                        inputCommand = it
                        viewModel.onInputChanged(it)
                    },
                    placeholder = { Text("Digite o comando...", fontFamily = FontFamily.Monospace, color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        if (inputCommand.isNotBlank()) {
                            haptic(HapticType.CONFIRM_SUCCESS)
                            viewModel.runCommand(inputCommand)
                            inputCommand = ""
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            haptic(HapticType.CONFIRM_SUCCESS)
                            viewModel.runCommand(inputCommand)
                            inputCommand = ""
                        }
                    },
                    enabled = !isRunning
                ) {
                    Icon(Icons.Default.PlayArrow, "Executar", tint = if (isRunning) TextMuted else GlowCyan)
                }
            }
        }
    }
}
