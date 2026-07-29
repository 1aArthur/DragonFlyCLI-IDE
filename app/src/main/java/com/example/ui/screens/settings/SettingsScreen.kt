package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AiProvider
import com.example.ui.components.BlackHoleAnimationController
import com.example.ui.components.FontManagementController
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val modelName by viewModel.modelName.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val topP by viewModel.topP.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    var showApiKey by remember { mutableStateOf(false) }
    var showProviderPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Configurações de APIs & Modelos", style = MaterialTheme.typography.titleLarge, color = GlowCyan)
        Text("Gerenciamento seguro de chaves com criptografia Android Keystore.", fontSize = 12.sp, color = TextSecondary)

        // Shader & Animation Background Controller with Live Preview
        BlackHoleAnimationController()

        // Monospaced Font Management Controller for Editor & Terminal
        FontManagementController()

        // Provider Selector Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Provedor Selecionado", fontSize = 11.sp, color = TextMuted)
                    Text(selectedProvider.displayName, fontSize = 15.sp, color = GlowCyan, style = MaterialTheme.typography.titleLarge)
                }

                Box {
                    Button(
                        onClick = { showProviderPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlowCyan)
                    ) {
                        Text("Trocar Provedor", color = GlowCyan, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = showProviderPicker,
                        onDismissRequest = { showProviderPicker = false }
                    ) {
                        AiProvider.values().forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.displayName, color = TextPrimary) },
                                onClick = {
                                    viewModel.selectProvider(provider)
                                    showProviderPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Credentials & URL Form Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, "Keystore", tint = TerminalGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Criptografia AES-256 via Android Keystore", fontSize = 11.sp, color = TerminalGreen)
                }

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.setApiKey(it) },
                    label = { Text("Chave de API (${selectedProvider.name})", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(Icons.Default.Key, "Mostrar", tint = GlowCyan)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { viewModel.setBaseUrl(it) },
                    label = { Text("URL Base / Endpoint Proxy", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { viewModel.setModelName(it) },
                    label = { Text("Nome do Modelo Padrão", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }
        }

        // Parameters Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Parâmetros do Modelo", fontSize = 13.sp, color = ElectricBlue)

                Text("Temperatura: String.format(\"%.2f\", $temperature)", fontSize = 12.sp, color = TextPrimary)
                Slider(
                    value = temperature,
                    onValueChange = { viewModel.setTemperature(it) },
                    valueRange = 0.0f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = GlowCyan, activeTrackColor = GlowCyan)
                )

                Text("Top P: String.format(\"%.2f\", $topP)", fontSize = 12.sp, color = TextPrimary)
                Slider(
                    value = topP,
                    onValueChange = { viewModel.setTopP(it) },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                )
            }
        }

        if (saveStatus.isNotBlank()) {
            Text(saveStatus, fontSize = 12.sp, color = TerminalGreen)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalYellow)
            ) {
                Text("Testar Conexão", color = TerminalYellow, fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.saveConfig() },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = GlowCyan)
            ) {
                Icon(Icons.Default.Save, null, tint = BlackHoleBackground, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salvar No Keystore", color = BlackHoleBackground, fontSize = 12.sp)
            }
        }
    }
}
