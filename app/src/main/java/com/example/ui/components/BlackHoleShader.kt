package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.BlackHoleAnimationState
import com.example.ui.theme.BlackHoleBackground
import com.example.ui.theme.BlackHoleColorMode
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.TerminalGreen
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance primitive float array buffer for particle rendering.
 * Eliminates object allocations inside the Canvas draw loop.
 */
private class OptimizedParticleBuffer(val count: Int, primaryColorsArgb: IntArray) {
    val initialAngles = FloatArray(count)
    val distanceRatios = FloatArray(count)
    val baseSpeeds = FloatArray(count)
    val radii = FloatArray(count)
    val colorsArgb = IntArray(count)
    val baseAlphas = FloatArray(count)
    val depthLayers = FloatArray(count) // Parallax depth layer (0.2f = deep space, 1.5f = foreground flare)

    init {
        val rand = Random(42)
        for (i in 0 until count) {
            initialAngles[i] = rand.nextFloat() * 2f * Math.PI.toFloat()
            distanceRatios[i] = rand.nextFloat() * 0.92f + 0.08f
            baseSpeeds[i] = (rand.nextFloat() * 0.012f + 0.003f) * if (rand.nextBoolean()) 1f else -1f
            radii[i] = rand.nextFloat() * 2.8f + 0.8f
            colorsArgb[i] = primaryColorsArgb[rand.nextInt(primaryColorsArgb.size)]
            baseAlphas[i] = rand.nextFloat() * 0.7f + 0.3f
            
            // Assign depth layers based on distance ratio
            val dist = distanceRatios[i]
            depthLayers[i] = when {
                dist > 0.70f -> 0.35f + rand.nextFloat() * 0.25f // Background distant stars
                dist > 0.30f -> 0.75f + rand.nextFloat() * 0.35f // Midground accretion disk
                else -> 1.20f + rand.nextFloat() * 0.40f         // Foreground plasma flares
            }
        }
    }
}

@Composable
fun BlackHoleShaderCanvas(
    state: BlackHoleAnimationState,
    modifier: Modifier = Modifier
) {
    var accumulatedTimeSec by remember { mutableFloatStateOf(0f) }

    val currentIsAnimated by rememberUpdatedState(state.isAnimated)
    val currentRotationSpeed by rememberUpdatedState(state.rotationSpeed)

    // Interactive Drag Parallax State
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        var lastNano = 0L
        while (true) {
            withInfiniteAnimationFrameNanos { nano ->
                if (currentIsAnimated) {
                    if (lastNano != 0L) {
                        val deltaSec = (nano - lastNano) / 1_000_000_000f
                        accumulatedTimeSec += deltaSec * currentRotationSpeed
                    }
                }
                lastNano = nano
            }
        }
    }

    val primaryColors = remember(state.colorMode) {
        when (state.colorMode) {
            BlackHoleColorMode.CYAN_PURPLE -> listOf(GlowCyan, CyberPurple, ElectricBlue, Color(0xFFFF9E00))
            BlackHoleColorMode.MONOCHROME_BW -> listOf(Color.White, Color(0xFFE0F7FA), Color(0xFF90A4AE), Color(0xFF546E7A))
            BlackHoleColorMode.FIRE_GOLD -> listOf(Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFFFF1744), Color(0xFFFFAB00))
            BlackHoleColorMode.NEON_GREEN -> listOf(TerminalGreen, Color(0xFF00FFCC), Color(0xFF00E676), Color(0xFF1B5E20))
        }
    }

    val primaryColorsArgb = remember(primaryColors) {
        primaryColors.map { it.toArgb() }.toIntArray()
    }

    // Preallocated primitive particle buffer
    val particleBuffer = remember(state.particleCount, state.colorMode) {
        OptimizedParticleBuffer(state.particleCount, primaryColorsArgb)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val target = Offset(
                                (dragOffset.value.x + dragAmount.x).coerceIn(-120f, 120f),
                                (dragOffset.value.y + dragAmount.y).coerceIn(-120f, 120f)
                            )
                            dragOffset.snapTo(target)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            dragOffset.animateTo(
                                targetValue = Offset.Zero,
                                animationSpec = spring(stiffness = 180f, dampingRatio = 0.65f)
                            )
                        }
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val baseCenter = Offset(width / 2f, height * 0.38f)
        val maxRadius = width.coerceAtLeast(height) * 0.75f * state.gravitationalPull
        val blackHoleRadius = (maxRadius * 0.20f).coerceAtLeast(24f)

        val animTime = accumulatedTimeSec
        val pMultiplier = state.parallaxMultiplier

        // Continuous Orbital Parallax Drift
        val orbitalParallaxX = sin(animTime * 0.8f) * 16f * pMultiplier
        val orbitalParallaxY = cos(animTime * 0.8f) * 12f * pMultiplier

        val userDragX = dragOffset.value.x * pMultiplier
        val userDragY = dragOffset.value.y * pMultiplier

        // 1. Singularity Core Base Center with Depth Layer 1.0f
        val singularityCenter = Offset(
            baseCenter.x + userDragX * 1.0f + orbitalParallaxX * 1.0f,
            baseCenter.y + userDragY * 1.0f + orbitalParallaxY * 1.0f
        )

        // Deep Space Background Center with Depth Layer 0.25f
        val backgroundCenter = Offset(
            baseCenter.x + userDragX * 0.25f + orbitalParallaxX * 0.25f,
            baseCenter.y + userDragY * 0.25f + orbitalParallaxY * 0.25f
        )

        // 1. Deep Space Void Background
        drawRect(color = BlackHoleBackground)

        // 2. Gravitational Lens Outer Glow (Nebula Halo - Depth Layer 0.25f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColors[0].copy(alpha = (0.40f * state.glowIntensity).coerceIn(0f, 1f)),
                    primaryColors[1].copy(alpha = (0.22f * state.glowIntensity).coerceIn(0f, 1f)),
                    Color(0xFF0D0B1A).copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = backgroundCenter,
                radius = maxRadius * 0.88f
            ),
            radius = maxRadius * 0.88f,
            center = backgroundCenter
        )

        // 3. Swirling Accretion Disk (Depth Layer 0.85f)
        val diskCenter = Offset(
            baseCenter.x + userDragX * 0.85f + orbitalParallaxX * 0.85f,
            baseCenter.y + userDragY * 0.85f + orbitalParallaxY * 0.85f
        )
        val diskRotation = -18f + (animTime * 15f) % 360f

        rotate(degrees = diskRotation, pivot = diskCenter) {
            val diskWidth = blackHoleRadius * 5.8f
            val diskHeight = blackHoleRadius * 2.5f

            // Outer Accretion Sweep Ring
            drawOval(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColors[0],
                        primaryColors[1],
                        primaryColors[2],
                        primaryColors[0]
                    ),
                    center = diskCenter
                ),
                topLeft = Offset(diskCenter.x - diskWidth / 2f, diskCenter.y - diskHeight / 2f),
                size = Size(diskWidth, diskHeight),
                style = Stroke(width = blackHoleRadius * 0.45f),
                alpha = (0.75f * state.glowIntensity).coerceIn(0f, 1f)
            )

            // Inner Plasma Fire Ring
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColors[0],
                        primaryColors[1],
                        Color.Transparent
                    ),
                    center = diskCenter,
                    radius = blackHoleRadius * 2.2f
                ),
                topLeft = Offset(diskCenter.x - blackHoleRadius * 2.2f, diskCenter.y - blackHoleRadius * 0.85f),
                size = Size(blackHoleRadius * 4.4f, blackHoleRadius * 1.7f),
                style = Stroke(width = blackHoleRadius * 0.25f),
                alpha = (0.90f * state.glowIntensity).coerceIn(0f, 1f)
            )
        }

        // 4. Orbiting Gravitational Particles & Stars (Zero-Allocation Loop using Primitive Buffer)
        val pCount = particleBuffer.count
        for (i in 0 until pCount) {
            val depth = particleBuffer.depthLayers[i]
            val particleCenter = Offset(
                baseCenter.x + userDragX * depth + orbitalParallaxX * depth,
                baseCenter.y + userDragY * depth + orbitalParallaxY * depth
            )

            val currentAngle = particleBuffer.initialAngles[i] + (particleBuffer.baseSpeeds[i] * animTime * 30f)
            val currentDistance = particleBuffer.distanceRatios[i] * maxRadius
            val cosVal = cos(currentAngle.toDouble()).toFloat()
            val sinVal = sin(currentAngle.toDouble()).toFloat()

            val px = particleCenter.x + cosVal * currentDistance
            val py = particleCenter.y + sinVal * currentDistance * 0.80f

            val proximity = (1f - (currentDistance / maxRadius)).coerceIn(0.2f, 1f)
            val twinkle = 0.8f + 0.2f * sin(animTime * 4f + particleBuffer.initialAngles[i])
            val alpha = (particleBuffer.baseAlphas[i] * proximity * twinkle * state.glowIntensity).coerceIn(0.15f, 1.0f)

            val colorArgb = particleBuffer.colorsArgb[i]

            drawCircle(
                color = Color(colorArgb).copy(alpha = alpha),
                radius = particleBuffer.radii[i] * (0.8f + proximity * 0.5f),
                center = Offset(px, py)
            )
        }

        // 5. Photon Ring (Event Horizon Edge Lens Glow - Depth Layer 1.0f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = (0.95f * state.glowIntensity).coerceIn(0f, 1f)),
                    primaryColors[0].copy(alpha = (0.90f * state.glowIntensity).coerceIn(0f, 1f)),
                    primaryColors[1].copy(alpha = (0.50f * state.glowIntensity).coerceIn(0f, 1f)),
                    Color.Transparent
                ),
                center = singularityCenter,
                radius = blackHoleRadius * 1.30f
            ),
            radius = blackHoleRadius * 1.30f,
            center = singularityCenter
        )

        // 6. Singularity Event Horizon Core (Absolute Void - Depth Layer 1.0f)
        drawCircle(
            color = Color(0xFF000000),
            radius = blackHoleRadius,
            center = singularityCenter
        )
    }
}
