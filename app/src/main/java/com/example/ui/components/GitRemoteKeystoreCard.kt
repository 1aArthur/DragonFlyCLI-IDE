package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.git.GitKeystoreManager
import com.example.utils.rememberHapticFeedback

@Composable
fun GitRemoteKeystoreCard(
    onPush: (remote: String, branch: String) -> Unit,
    onPull: (remote: String, branch: String) -> Unit,
    onFetch: (remote: String) -> Unit,
    onAddRemote: (name: String, url: String) -> Unit,
    onSyncCredentials: (username: String, token: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val keystoreManager = remember { GitKeystoreManager(context) }
    val credState by keystoreManager.state.collectAsState()

    var usernameInput by remember(credState.username) { mutableStateOf(credState.username) }
    var tokenInput by remember(credState.tokenOrSshKey) { mutableStateOf(credState.tokenOrSshKey) }
    var remoteUrlInput by remember(credState.remoteUrl) { mutableStateOf(credState.remoteUrl.ifBlank { "https://github.com/usuario/repositorio.git" }) }

    var remoteNameInput by remember { mutableStateOf("origin") }
    var branchNameInput by remember { mutableStateOf("main") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row with Android Keystore Security Shield Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (credState.isSecuredInKeystore) Icons.Default.Security else Icons.Default.LockOpen,
                        contentDescription = "Keystore Status",
                        tint = if (credState.isSecuredInKeystore) TerminalGreen else TerminalYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Repositórios Remotos & Keystore",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowCyan
                        )
                        Text(
                            text = if (credState.isSecuredInKeystore) "Protegido por Criptografia AES-250 GCM (Hardware)" else "Nenhuma credencial salva no Keystore",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic(HapticType.LIGHT_CLICK)
                        isExpanded = !isExpanded
                    }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir/Recolher",
                        tint = TextMuted
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Divider(color = DarkCardBorder, thickness = 0.5.dp)

                    // 1. Remote Branch Operations (Push, Pull, Fetch)
                    Text("1. Operações de Repositório Remoto", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = remoteNameInput,
                            onValueChange = { remoteNameInput = it },
                            label = { Text("Remote", fontSize = 9.sp, color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan, unfocusedBorderColor = DarkCardBorder)
                        )

                        OutlinedTextField(
                            value = branchNameInput,
                            onValueChange = { branchNameInput = it },
                            label = { Text("Branch", fontSize = 9.sp, color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GlowCyan, unfocusedBorderColor = DarkCardBorder)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic(HapticType.CONFIRM_SUCCESS)
                                onPush(remoteNameInput, branchNameInput)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
                        ) {
                            Icon(Icons.Default.Upload, null, tint = BlackHoleBackground, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Git Push", fontSize = 10.sp, color = BlackHoleBackground)
                        }

                        Button(
                            onClick = {
                                haptic(HapticType.CONFIRM_SUCCESS)
                                onPull(remoteNameInput, branchNameInput)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlowCyan)
                        ) {
                            Icon(Icons.Default.Download, null, tint = GlowCyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Git Pull", fontSize = 10.sp, color = GlowCyan)
                        }

                        Button(
                            onClick = {
                                haptic(HapticType.LIGHT_CLICK)
                                onFetch(remoteNameInput)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple)
                        ) {
                            Icon(Icons.Default.Sync, null, tint = CyberPurple, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Git Fetch", fontSize = 10.sp, color = CyberPurple)
                        }
                    }

                    Divider(color = DarkCardBorder, thickness = 0.5.dp)

                    // 2. Android Keystore Secure Credentials Form
                    Text("2. Criptografia de Credenciais (Android Keystore)", fontSize = 11.sp, color = TerminalGreen, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Usuário Git / GitHub", fontSize = 10.sp, color = TextMuted) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = TextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalGreen, unfocusedBorderColor = DarkCardBorder)
                    )

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Personal Access Token (PAT) / SSH Key", fontSize = 10.sp, color = TextMuted) },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Alternar Visibilidade",
                                    tint = TextMuted
                                )
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalGreen, unfocusedBorderColor = DarkCardBorder)
                    )

                    OutlinedTextField(
                        value = remoteUrlInput,
                        onValueChange = { remoteUrlInput = it },
                        label = { Text("URL do Repositório Remoto (HTTPS / SSH)", fontSize = 10.sp, color = TextMuted) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TerminalGreen, unfocusedBorderColor = DarkCardBorder)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic(HapticType.CONFIRM_SUCCESS)
                                val saved = keystoreManager.saveCredentials(usernameInput, tokenInput, remoteUrlInput)
                                if (saved) {
                                    onSyncCredentials(usernameInput, tokenInput)
                                    onAddRemote(remoteNameInput, remoteUrlInput)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                        ) {
                            Icon(Icons.Default.Lock, null, tint = BlackHoleBackground, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Salvar no Keystore", fontSize = 10.sp, color = BlackHoleBackground, fontWeight = FontWeight.Bold)
                        }

                        if (credState.isSecuredInKeystore) {
                            Button(
                                onClick = {
                                    haptic(HapticType.LIGHT_CLICK)
                                    keystoreManager.clearCredentials()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalRed)
                            ) {
                                Text("Limpar", fontSize = 10.sp, color = TerminalRed)
                            }
                        }
                    }

                    Text(
                        text = credState.lastSyncStatus,
                        fontSize = 10.sp,
                        color = GlowCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
