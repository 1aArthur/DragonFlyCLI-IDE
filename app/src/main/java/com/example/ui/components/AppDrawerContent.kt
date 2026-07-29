package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.TermuxStatus
import com.example.utils.rememberHapticFeedback

data class NavDrawerItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String,
    val description: String
)

val drawerItems = listOf(
    NavDrawerItem(Screen.Chat, Icons.Default.Chat, "Chat IA", "Assistente e Arquiteto"),
    NavDrawerItem(Screen.Terminal, Icons.Default.Terminal, "Terminal Linux", "Shell Nativo e Termux:API"),
    NavDrawerItem(Screen.Editor, Icons.Default.Code, "Editor de Código", "Syntax Highlighting & Tabs"),
    NavDrawerItem(Screen.Files, Icons.Default.Folder, "Gerenciador de Arquivos", "Explorador e Bookmarks"),
    NavDrawerItem(Screen.Agents, Icons.Default.SmartToy, "Agentes Autônomos", "Workflows e Pipelines"),
    NavDrawerItem(Screen.Git, Icons.Default.AccountTree, "Controle Git", "Commits e Branching"),
    NavDrawerItem(Screen.Settings, Icons.Default.Settings, "Configurações", "Chaves API & Shader Physics")
)

@Composable
fun AppDrawerContent(
    currentRoute: String,
    termuxStatus: TermuxStatus,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()

    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = BlackHoleBackground,
        drawerContentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(GlowCyan, CyberPurple, Color.Black)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, "Logo", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Column {
                            Text(
                                text = "DragonflyCLI IDE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowCyan
                            )
                            Text(
                                text = "v2.5 Deep Space Edition",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Divider(color = DarkCardBorder, thickness = 0.5.dp)

                    // Termux Badge Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (termuxStatus.isTermuxInstalled) TerminalGreen else TerminalYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Bridge Termux:API:",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            color = if (termuxStatus.isTermuxInstalled) Color(0xFF0D2818) else Color(0xFF2B2010),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, if (termuxStatus.isTermuxInstalled) TerminalGreen else TerminalYellow)
                        ) {
                            Text(
                                text = if (termuxStatus.isTermuxInstalled) "INSTALADO" else "AUSENTE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (termuxStatus.isTermuxInstalled) TerminalGreen else TerminalYellow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "NAVEGAÇÃO PRINCIPAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // Navigation Items List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                drawerItems.forEach { item ->
                    val isSelected = currentRoute == item.screen.route
                    Surface(
                        color = if (isSelected) Color(0xFF182234) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) BorderStroke(1.dp, GlowCyan) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic(HapticType.LIGHT_CLICK)
                                onNavigate(item.screen)
                                onCloseDrawer()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) GlowCyan else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GlowCyan else TextPrimary
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(GlowCyan)
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = DarkCardBorder, thickness = 0.5.dp)

            // Quick Info Footer Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Storage, null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Banco de Dados Room & Keystore configurados com sucesso.",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
