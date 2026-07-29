package com.example.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SyntaxHighlighter
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback

@Composable
fun CodeEditorScreen(
    viewModel: CodeEditorViewModel,
    onAskAiAboutCode: (String) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val fontState by FontController.state.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val activeIndex by viewModel.activeTabIndex.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val editorFontFamily = fontState.selectedEditorFont.fontFamily
    val editorFontSize = fontState.editorFontSizeSp.sp
    val editorLineHeight = (fontState.editorFontSizeSp * fontState.lineSpacingMultiplier).sp

    var showSearchField by remember { mutableStateOf(false) }

    val activeTab = tabs.getOrNull(activeIndex)

    val quickCodeKeys = listOf("  ", "{", "}", "(", ")", ";", "=", "\"", "fun", "val", "return")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        // Top Toolbar & Multi-Tab Header
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Bar
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(tabs) { idx, tab ->
                        val isSelected = idx == activeIndex
                        Surface(
                            color = if (isSelected) Color(0xFF182234) else DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GlowCyan else DarkCardBorder
                            ),
                            modifier = Modifier.clickable {
                                haptic(HapticType.LIGHT_CLICK)
                                viewModel.selectTab(idx)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (tab.isDirty) "${tab.name} *" else tab.name,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) GlowCyan else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar Aba",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            haptic(HapticType.LIGHT_CLICK)
                                            viewModel.closeTab(idx)
                                        }
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = {
                        haptic(HapticType.LIGHT_CLICK)
                        showSearchField = !showSearchField
                    }) {
                        Icon(Icons.Default.Search, "Pesquisar", tint = TextSecondary)
                    }
                    IconButton(onClick = {
                        haptic(HapticType.CONFIRM_SUCCESS)
                        viewModel.saveActiveFile()
                    }) {
                        Icon(Icons.Default.Save, "Salvar", tint = GlowCyan)
                    }
                }
            }
        }

        // Search Bar overlay if enabled
        if (showSearchField) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        haptic(HapticType.KEYPRESS)
                        viewModel.setSearchQuery(it)
                    },
                    placeholder = { Text("Pesquisar no arquivo...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GlowCyan,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    singleLine = true
                )
            }
        }

        // Code Editor Canvas
        if (activeTab != null) {
            // Mobile Quick Coding Accessory Keyboard Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(quickCodeKeys.size) { keyIdx ->
                    val keySymbol = quickCodeKeys[keyIdx]
                    Surface(
                        color = Color(0xFF161E2E),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkCardBorder),
                        modifier = Modifier.clickable {
                            haptic(HapticType.KEYPRESS)
                            viewModel.updateActiveContent(activeTab.content + keySymbol)
                        }
                    ) {
                        Text(
                            text = if (keySymbol == "  ") "TAB" else keySymbol,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                backgroundColor = DarkSurface,
                borderColor = DarkCardBorder
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(8.dp)
                ) {
                    // Line numbers column
                    val linesCount = activeTab.content.lines().size.coerceAtLeast(1)
                    Column(
                        modifier = Modifier
                            .padding(end = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..linesCount) {
                            Text(
                                text = i.toString(),
                                fontFamily = editorFontFamily,
                                fontSize = editorFontSize,
                                color = TextMuted,
                                lineHeight = editorLineHeight
                            )
                        }
                    }

                    // Editable Code Content
                    OutlinedTextField(
                        value = activeTab.content,
                        onValueChange = {
                            haptic(HapticType.KEYPRESS)
                            viewModel.updateActiveContent(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = editorFontFamily,
                            fontSize = editorFontSize,
                            lineHeight = editorLineHeight
                        ),
                        visualTransformation = {
                            androidx.compose.ui.text.input.TransformedText(
                                SyntaxHighlighter.highlight(it.text),
                                androidx.compose.ui.text.input.OffsetMapping.Identity
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // AI Assistance Trigger
            Button(
                onClick = {
                    haptic(HapticType.HEAVY_CLICK)
                    onAskAiAboutCode("Refatore e explique este código:\n\n${activeTab.content}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162032)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlowCyan)
            ) {
                Icon(Icons.Default.Code, null, tint = GlowCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analisar / Refatorar Código com IA", color = GlowCyan, fontSize = 12.sp)
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum arquivo aberto no Editor", color = TextMuted)
            }
        }
    }
}
