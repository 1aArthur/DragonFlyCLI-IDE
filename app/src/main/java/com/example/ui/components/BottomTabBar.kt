package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.utils.HapticType
import com.example.utils.rememberHapticFeedback
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun BottomTabBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit
) {
    val haptic = rememberHapticFeedback()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    DarkCardBorder,
                    CyberPurple.copy(alpha = 0.4f),
                    GlowCyan.copy(alpha = 0.4f),
                    DarkCardBorder
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.allScreens.forEach { screen ->
                val isSelected = currentRoute == screen.route

                val activeColor by animateColorAsState(
                    targetValue = if (isSelected) GlowCyan else TextMuted,
                    animationSpec = tween(durationMillis = 250),
                    label = "activeColor"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.18f else 1.0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "iconScale"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF141A29) else Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label = "pillBgColor"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(pillBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic(HapticType.LIGHT_CLICK)
                            onNavigate(screen)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = activeColor,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(iconScale)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextMuted
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Glowing bottom indicator dot/line
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 14.dp else 0.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) GlowCyan else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}

