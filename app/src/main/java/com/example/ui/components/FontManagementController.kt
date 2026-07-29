package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback

@Composable
fun FontManagementController(
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val fontState by FontController.state.collectAsState()
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic(HapticType.LIGHT_CLICK)
                            isExpanded = !isExpanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.FontDownload, "Fontes", tint = GlowCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Gerenciador de Fontes Monoespaçadas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowCyan
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic(HapticType.LIGHT_CLICK)
                                FontController.resetDefaults()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Restaurar", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Interactive Live Typography Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("EDITOR PREVIEW (${fontState.selectedEditorFont.displayName})", fontSize = 10.sp, color = GlowCyan, fontWeight = FontWeight.Bold)
                            Text("${fontState.editorFontSizeSp} sp", fontSize = 10.sp, color = TextMuted)
                        }

                        Text(
                            text = "fun main() {\n    val dragonfly = \"DragonflyCLI v2.5\"\n    println(dragonfly)\n}",
                            fontFamily = fontState.selectedEditorFont.fontFamily,
                            fontSize = fontState.editorFontSizeSp.sp,
                            color = TerminalGreen,
                            lineHeight = (fontState.editorFontSizeSp * fontState.lineSpacingMultiplier).sp
                        )

                        Divider(color = DarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TERMINAL PREVIEW (${fontState.selectedTerminalFont.displayName})", fontSize = 10.sp, color = CyberPurple, fontWeight = FontWeight.Bold)
                            Text("${fontState.terminalFontSizeSp} sp", fontSize = 10.sp, color = TextMuted)
                        }

                        Text(
                            text = "user@dragonfly:~$ cargo build --release\n[Cargo-NDK Engine] Build completed in 0.28s",
                            fontFamily = fontState.selectedTerminalFont.fontFamily,
                            fontSize = fontState.terminalFontSizeSp.sp,
                            color = GlowCyan,
                            lineHeight = (fontState.terminalFontSizeSp * fontState.lineSpacingMultiplier).sp
                        )
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // 1. Editor Font Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Code, null, tint = GlowCyan, modifier = Modifier.size(14.dp))
                                Text("Fonte do Editor de Código:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                MonospacedFontOption.values().forEach { font ->
                                    val isSelected = fontState.selectedEditorFont == font
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic(HapticType.LIGHT_CLICK)
                                            FontController.setEditorFont(font)
                                        },
                                        label = { Text(font.displayName, fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DarkCardBorder,
                                            selectedLabelColor = GlowCyan,
                                            containerColor = DarkSurface,
                                            labelColor = TextMuted
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = DarkCardBorder,
                                            selectedBorderColor = GlowCyan
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tamanho do Texto (Editor):", fontSize = 11.sp, color = TextMuted)
                                Text("${fontState.editorFontSizeSp} sp", fontSize = 11.sp, color = GlowCyan, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = fontState.editorFontSizeSp.toFloat(),
                                onValueChange = {
                                    haptic(HapticType.KEYPRESS)
                                    FontController.setEditorFontSize(it.toInt())
                                },
                                valueRange = 10f..22f,
                                colors = SliderDefaults.colors(thumbColor = GlowCyan, activeTrackColor = GlowCyan)
                            )
                        }

                        Divider(color = DarkCardBorder, thickness = 0.5.dp)

                        // 2. Terminal Font Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Terminal, null, tint = CyberPurple, modifier = Modifier.size(14.dp))
                                Text("Fonte do Terminal CLI:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                MonospacedFontOption.values().forEach { font ->
                                    val isSelected = fontState.selectedTerminalFont == font
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic(HapticType.LIGHT_CLICK)
                                            FontController.setTerminalFont(font)
                                        },
                                        label = { Text(font.displayName, fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DarkCardBorder,
                                            selectedLabelColor = CyberPurple,
                                            containerColor = DarkSurface,
                                            labelColor = TextMuted
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = DarkCardBorder,
                                            selectedBorderColor = CyberPurple
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tamanho do Texto (Terminal):", fontSize = 11.sp, color = TextMuted)
                                Text("${fontState.terminalFontSizeSp} sp", fontSize = 11.sp, color = CyberPurple, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = fontState.terminalFontSizeSp.toFloat(),
                                onValueChange = {
                                    haptic(HapticType.KEYPRESS)
                                    FontController.setTerminalFontSize(it.toInt())
                                },
                                valueRange = 9f..20f,
                                colors = SliderDefaults.colors(thumbColor = CyberPurple, activeTrackColor = CyberPurple)
                            )
                        }

                        Divider(color = DarkCardBorder, thickness = 0.5.dp)

                        // 3. Line Spacing Multiplier Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Espaçamento de Linhas (Line Height):", fontSize = 11.sp, color = TextPrimary)
                                Text("${String.format("%.2f", fontState.lineSpacingMultiplier)}x", fontSize = 11.sp, color = TerminalGreen)
                            }
                            Slider(
                                value = fontState.lineSpacingMultiplier,
                                onValueChange = { FontController.setLineSpacing(it) },
                                valueRange = 1.0f..1.8f,
                                colors = SliderDefaults.colors(thumbColor = TerminalGreen, activeTrackColor = TerminalGreen)
                            )
                        }
                    }
                }
            }
        }
    }
}
