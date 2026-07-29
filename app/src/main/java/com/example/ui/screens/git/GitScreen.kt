package com.example.ui.screens.git

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun GitScreen(
    viewModel: GitViewModel,
    onOpenTerminal: () -> Unit
) {
    var commitMessage by remember { mutableStateOf("") }
    var cloneUrl by remember { mutableStateOf("") }
    var showCloneModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Gerenciador Git", style = MaterialTheme.typography.titleLarge, color = GlowCyan)
        Text("Controle de versão diretamente no aplicativo.", fontSize = 12.sp, color = TextSecondary)

        // Git Status Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Ações Rápidas", fontSize = 13.sp, color = GlowCyan)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.status()
                            onOpenTerminal()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text("Git Status", color = TextPrimary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.diff()
                            onOpenTerminal()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text("Git Diff", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.log()
                            onOpenTerminal()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text("Git Log", color = TextPrimary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showCloneModal = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Text("Git Clone", color = TextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Commit Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Commit & Push", fontSize = 13.sp, color = ElectricBlue)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("Mensagem do commit...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.addAll()
                            if (commitMessage.isNotBlank()) {
                                viewModel.commit(commitMessage)
                                commitMessage = ""
                            }
                            onOpenTerminal()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                    ) {
                        Text("Stage & Commit", color = BlackHoleBackground, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.push()
                            onOpenTerminal()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Git Push", color = TextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Pull & Sync
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sincronizar Repositório Remoto", fontSize = 12.sp, color = TextPrimary)
                Button(
                    onClick = {
                        viewModel.pull()
                        onOpenTerminal()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Text("Git Pull", color = TerminalGreen, fontSize = 11.sp)
                }
            }
        }
    }

    // Modal Clone Dialog
    if (showCloneModal) {
        AlertDialog(
            onDismissRequest = { showCloneModal = false },
            title = { Text("Clonar Repositório Git", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = cloneUrl,
                    onValueChange = { cloneUrl = it },
                    placeholder = { Text("https://github.com/usuario/repo.git", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cloneUrl.isNotBlank()) {
                            viewModel.clone(cloneUrl)
                            cloneUrl = ""
                            showCloneModal = false
                            onOpenTerminal()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                ) {
                    Text("Clonar", color = BlackHoleBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneModal = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
