package com.example.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BlackHoleColorMode(val label: String) {
    CYAN_PURPLE("Ciano & Púrpura"),
    MONOCHROME_BW("Buraco Negro PB"),
    FIRE_GOLD("Plasma Dourado"),
    NEON_GREEN("Neon Matrix")
}

data class BlackHoleAnimationState(
    val rotationSpeed: Float = 1.0f,
    val gravitationalPull: Float = 1.0f,
    val particleCount: Int = 140,
    val glowIntensity: Float = 1.0f,
    val parallaxMultiplier: Float = 1.0f,
    val isAnimated: Boolean = true,
    val colorMode: BlackHoleColorMode = BlackHoleColorMode.CYAN_PURPLE
)

object BlackHoleController {
    private val _state = MutableStateFlow(BlackHoleAnimationState())
    val state: StateFlow<BlackHoleAnimationState> = _state.asStateFlow()

    fun updateRotationSpeed(speed: Float) {
        _state.value = _state.value.copy(rotationSpeed = speed)
    }

    fun updateGravitationalPull(pull: Float) {
        _state.value = _state.value.copy(gravitationalPull = pull)
    }

    fun updateParticleCount(count: Int) {
        _state.value = _state.value.copy(particleCount = count)
    }

    fun updateGlowIntensity(intensity: Float) {
        _state.value = _state.value.copy(glowIntensity = intensity)
    }

    fun updateParallaxMultiplier(multiplier: Float) {
        _state.value = _state.value.copy(parallaxMultiplier = multiplier)
    }

    fun toggleAnimated() {
        _state.value = _state.value.copy(isAnimated = !_state.value.isAnimated)
    }

    fun setColorMode(mode: BlackHoleColorMode) {
        _state.value = _state.value.copy(colorMode = mode)
    }

    fun reset() {
        _state.value = BlackHoleAnimationState()
    }
}
