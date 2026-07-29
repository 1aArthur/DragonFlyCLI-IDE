package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BlackHoleStarsBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BlackHoleParticleSystem(
        modifier = modifier,
        particleCount = 130,
        content = content
    )
}

