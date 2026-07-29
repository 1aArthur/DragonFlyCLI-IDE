package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback

@Composable
fun CodeBlockWithSyntax(
    code: String,
    language: String = "kotlin",
    modifier: Modifier = Modifier,
    onOpenInEditor: ((String) -> Unit)? = null,
    onRunInTerminal: ((String) -> Unit)? = null
) {
    val haptic = rememberHapticFeedback()
    val context = LocalContext.current
    val highlightedText = remember(code, language) {
        SyntaxHighlighter.highlight(code, language)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
        color = Color(0xFF0C1017)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Code Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141A24))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(GlowCyan)
                    )
                    Text(
                        text = language.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GlowCyan
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onRunInTerminal != null) {
                        IconButton(
                            onClick = {
                                haptic(HapticType.CONFIRM_SUCCESS)
                                onRunInTerminal(code)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, "Executar no Terminal", tint = TerminalGreen, modifier = Modifier.size(14.dp))
                        }
                    }

                    if (onOpenInEditor != null) {
                        IconButton(
                            onClick = {
                                haptic(HapticType.LIGHT_CLICK)
                                onOpenInEditor(code)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Code, "Abrir no Editor", tint = ElectricBlue, modifier = Modifier.size(14.dp))
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic(HapticType.CONFIRM_SUCCESS)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code", code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, "Copiar Código", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Code Text Body with Line Numbers
            val horizontalScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                val lines = code.lines()
                Column(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.indices.forEach { idx ->
                        Text(
                            text = (idx + 1).toString(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Text(
                        text = highlightedText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
