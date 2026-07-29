package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.BlackHoleAnimationState
import com.example.ui.theme.BlackHoleController

@Composable
fun BlackHoleParticleSystem(
    modifier: Modifier = Modifier,
    particleCount: Int = 140,
    content: (@Composable () -> Unit)? = null
) {
    val configState by BlackHoleController.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        BlackHoleShaderCanvas(
            state = configState.copy(particleCount = particleCount.coerceAtLeast(configState.particleCount)),
            modifier = Modifier.fillMaxSize()
        )

        content?.invoke()
    }
}
