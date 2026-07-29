package com.example.ui.theme

import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MonospacedFontOption(
    val id: String,
    val displayName: String,
    val description: String,
    val fontFamily: FontFamily
) {
    JETBRAINS_MONO(
        id = "jetbrains_mono",
        displayName = "JetBrains Mono",
        description = "Projetada para desenvolvedores com máxima legibilidade de código",
        fontFamily = FontFamily.Monospace
    ),
    FIRA_CODE(
        id = "fira_code",
        displayName = "Fira Code",
        description = "Fonte monoespaçada popular com suporte extenso a ligaduras",
        fontFamily = FontFamily.Monospace
    ),
    ROBOTO_MONO(
        id = "roboto_mono",
        displayName = "Roboto Mono",
        description = "Design geométrico limpo otimizado para a plataforma Android",
        fontFamily = FontFamily.Monospace
    ),
    SOURCE_CODE_PRO(
        id = "source_code_pro",
        displayName = "Source Code Pro",
        description = "Fonte profissional da Adobe para ambientes IDE",
        fontFamily = FontFamily.Monospace
    ),
    SYSTEM_MONOSPACE(
        id = "system_monospace",
        displayName = "Monospace Padrão",
        description = "Fonte monoespaçada nativa do sistema operacional",
        fontFamily = FontFamily.Monospace
    )
}

data class FontSettingsState(
    val selectedEditorFont: MonospacedFontOption = MonospacedFontOption.JETBRAINS_MONO,
    val selectedTerminalFont: MonospacedFontOption = MonospacedFontOption.FIRA_CODE,
    val editorFontSizeSp: Int = 12,
    val terminalFontSizeSp: Int = 11,
    val enableLigatures: Boolean = true,
    val lineSpacingMultiplier: Float = 1.25f
)

object FontController {
    private val _state = MutableStateFlow(FontSettingsState())
    val state: StateFlow<FontSettingsState> = _state.asStateFlow()

    fun setEditorFont(font: MonospacedFontOption) {
        _state.value = _state.value.copy(selectedEditorFont = font)
    }

    fun setTerminalFont(font: MonospacedFontOption) {
        _state.value = _state.value.copy(selectedTerminalFont = font)
    }

    fun setEditorFontSize(sizeSp: Int) {
        _state.value = _state.value.copy(editorFontSizeSp = sizeSp.coerceIn(10, 24))
    }

    fun setTerminalFontSize(sizeSp: Int) {
        _state.value = _state.value.copy(terminalFontSizeSp = sizeSp.coerceIn(9, 22))
    }

    fun toggleLigatures() {
        _state.value = _state.value.copy(enableLigatures = !_state.value.enableLigatures)
    }

    fun setLineSpacing(multiplier: Float) {
        _state.value = _state.value.copy(lineSpacingMultiplier = multiplier.coerceIn(1.0f, 2.0f))
    }

    fun resetDefaults() {
        _state.value = FontSettingsState()
    }
}
