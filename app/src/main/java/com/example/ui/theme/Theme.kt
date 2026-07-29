package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackHoleColorScheme = darkColorScheme(
    primary = GlowCyan,
    onPrimary = BlackHoleBackground,
    primaryContainer = DarkSurface,
    onPrimaryContainer = TextPrimary,
    inversePrimary = BlackHoleBackground,
    secondary = ElectricBlue,
    onSecondary = BlackHoleBackground,
    secondaryContainer = Color(0xFF162032),
    onSecondaryContainer = TextPrimary,
    tertiary = CyberPurple,
    onTertiary = BlackHoleBackground,
    tertiaryContainer = Color(0xFF261836),
    onTertiaryContainer = TextPrimary,
    background = BlackHoleBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardBorder,
    onSurfaceVariant = TextSecondary,
    surfaceTint = GlowCyan,
    inverseSurface = TextPrimary,
    inverseOnSurface = BlackHoleBackground,
    error = TerminalRed,
    onError = BlackHoleBackground,
    errorContainer = Color(0xFF321215),
    onErrorContainer = TextPrimary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1A1A22),
    scrim = BlackHoleBackground
)

@Composable
fun DragonflyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackHoleColorScheme,
        typography = Typography,
        content = content
    )
}
