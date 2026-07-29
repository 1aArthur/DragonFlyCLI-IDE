package com.example.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

enum class HapticType {
    LIGHT_CLICK,
    KEYPRESS,
    CONFIRM_SUCCESS,
    WARNING_ERROR,
    HEAVY_CLICK
}

object HapticFeedbackHelper {

    fun performHaptic(context: Context, type: HapticType = HapticType.LIGHT_CLICK) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effectId = when (type) {
                        HapticType.LIGHT_CLICK -> VibrationEffect.EFFECT_TICK
                        HapticType.KEYPRESS -> VibrationEffect.EFFECT_CLICK
                        HapticType.CONFIRM_SUCCESS -> VibrationEffect.EFFECT_DOUBLE_CLICK
                        HapticType.WARNING_ERROR -> VibrationEffect.EFFECT_HEAVY_CLICK
                        HapticType.HEAVY_CLICK -> VibrationEffect.EFFECT_HEAVY_CLICK
                    }
                    vibrator.vibrate(VibrationEffect.createPredefined(effectId))
                } else {
                    @Suppress("DEPRECATION")
                    val durationMs = when (type) {
                        HapticType.LIGHT_CLICK -> 10L
                        HapticType.KEYPRESS -> 15L
                        HapticType.CONFIRM_SUCCESS -> 30L
                        HapticType.WARNING_ERROR -> 50L
                        HapticType.HEAVY_CLICK -> 25L
                    }
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration permissions or hardware missing
        }
    }
}

@Composable
fun rememberHapticFeedback(): (HapticType) -> Unit {
    val view = LocalView.current
    val context = view.context
    return { type ->
        try {
            val feedbackConstant = when (type) {
                HapticType.LIGHT_CLICK -> HapticFeedbackConstants.VIRTUAL_KEY
                HapticType.KEYPRESS -> HapticFeedbackConstants.KEYBOARD_TAP
                HapticType.CONFIRM_SUCCESS -> HapticFeedbackConstants.CONFIRM
                HapticType.WARNING_ERROR -> HapticFeedbackConstants.REJECT
                HapticType.HEAVY_CLICK -> HapticFeedbackConstants.LONG_PRESS
            }
            if (!view.performHapticFeedback(feedbackConstant)) {
                HapticFeedbackHelper.performHaptic(context, type)
            }
        } catch (e: Exception) {
            HapticFeedbackHelper.performHaptic(context, type)
        }
    }
}
