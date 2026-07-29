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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BlackHoleAnimationController(
    modifier: Modifier = Modifier
) {
    val state by BlackHoleController.state.collectAsState()
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Live Background Preview Card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, "Preview", tint = GlowCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Preview do Background (Buraco Negro Shader)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowCyan
                        )
                    }

                    Surface(
                        color = if (state.isAnimated) Color(0xFF0D2818) else Color(0xFF2B1012),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, if (state.isAnimated) TerminalGreen else TerminalRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isAnimated) TerminalGreen else TerminalRed)
                            )
                            Text(
                                text = if (state.isAnimated) "EM EXECUÇÃO" else "PAUSADO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isAnimated) TerminalGreen else TerminalRed
                            )
                        }
                    }
                }

                // Interactive Live Viewport Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                ) {
                    BlackHoleShaderCanvas(
                        state = state,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay HUD Details
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Surface(
                            color = DarkSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Text(
                                text = "MODO: ${state.colorMode.label} | VELOCIDADE: ${String.format("%.1fx", state.rotationSpeed)}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Play / Pause Overlay Button
                    IconButton(
                        onClick = { BlackHoleController.toggleAnimated() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurface.copy(alpha = 0.88f))
                            .border(1.dp, GlowCyan, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.isAnimated) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = GlowCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Control Panel Header
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Tune, "Controlador", tint = ElectricBlue, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Controlador de Animação e Física Shader",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { BlackHoleController.reset() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Reset", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Color Mode Chips
                        Text("Paleta de Cores do Disco & Estrelas:", fontSize = 11.sp, color = TextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BlackHoleColorMode.values().forEach { mode ->
                                val isSelected = state.colorMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { BlackHoleController.setColorMode(mode) },
                                    label = { Text(mode.label, fontSize = 10.sp) },
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

                        Divider(color = DarkCardBorder, thickness = 0.5.dp)

                        // Rotation Speed Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Velocidade de Rotação:", fontSize = 11.sp, color = TextPrimary)
                                Text("${String.format("%.2f", state.rotationSpeed)}x", fontSize = 11.sp, color = GlowCyan)
                            }
                            Slider(
                                value = state.rotationSpeed,
                                onValueChange = { BlackHoleController.updateRotationSpeed(it) },
                                valueRange = 0.1f..3.0f,
                                colors = SliderDefaults.colors(thumbColor = GlowCyan, activeTrackColor = GlowCyan)
                            )
                        }

                        // Gravitational Pull Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Força de Atração Gravitacional:", fontSize = 11.sp, color = TextPrimary)
                                Text("${String.format("%.2f", state.gravitationalPull)}x", fontSize = 11.sp, color = CyberPurple)
                            }
                            Slider(
                                value = state.gravitationalPull,
                                onValueChange = { BlackHoleController.updateGravitationalPull(it) },
                                valueRange = 0.2f..2.5f,
                                colors = SliderDefaults.colors(thumbColor = CyberPurple, activeTrackColor = CyberPurple)
                            )
                        }

                        // Particle Count Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Densidade de Estrelas / Partículas:", fontSize = 11.sp, color = TextPrimary)
                                Text("${state.particleCount}", fontSize = 11.sp, color = ElectricBlue)
                            }
                            Slider(
                                value = state.particleCount.toFloat(),
                                onValueChange = { BlackHoleController.updateParticleCount(it.toInt()) },
                                valueRange = 40f..300f,
                                colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                            )
                        }

                        // Glow Intensity Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Intensidade do Brilho Photon Glow:", fontSize = 11.sp, color = TextPrimary)
                                Text("${String.format("%.2f", state.glowIntensity)}x", fontSize = 11.sp, color = TerminalGreen)
                            }
                            Slider(
                                value = state.glowIntensity,
                                onValueChange = { BlackHoleController.updateGlowIntensity(it) },
                                valueRange = 0.2f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = TerminalGreen, activeTrackColor = TerminalGreen)
                            )
                        }

                        // Parallax Depth Multiplier Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Parallax de Profundidade 3D (Arraste no preview):", fontSize = 11.sp, color = TextPrimary)
                                Text("${String.format("%.2f", state.parallaxMultiplier)}x", fontSize = 11.sp, color = GlowCyan)
                            }
                            Slider(
                                value = state.parallaxMultiplier,
                                onValueChange = { BlackHoleController.updateParallaxMultiplier(it) },
                                valueRange = 0.0f..2.5f,
                                colors = SliderDefaults.colors(thumbColor = GlowCyan, activeTrackColor = GlowCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}
