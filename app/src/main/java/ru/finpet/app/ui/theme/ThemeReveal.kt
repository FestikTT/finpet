package ru.finpet.app.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot

val LocalThemeSwitcher = staticCompositionLocalOf<(AppTheme, Offset) -> Unit> {
    { _, _ -> }
}

/**
 * Оверлей плавного перехода тем (Circular Reveal Transition):
 * В момент смены темы снимок старого экрана рисуется поверх нового.
 * Из точки клика вырезается круг с растущим радиусом от 0 до диагонали экрана,
 * эффектно раскрывая новую тему в стиле флагманских интерфейсов.
 */
@Composable
fun ThemeRevealOverlay(
    revealBitmap: ImageBitmap?,
    tapCenter: Offset,
    progress: Float
) {
    if (revealBitmap == null || progress >= 1f) return

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {} // Блокировка касаний во время анимации перехода
    ) {
        val maxRadius = hypot(size.width, size.height)
        val currentRadius = maxRadius * progress

        // Путь с EvenOdd: экранный прямоугольник МИНУС растущий круг в центре нажатия
        val clipPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addOval(
                Rect(
                    center = tapCenter,
                    radius = currentRadius
                )
            )
        }

        clipPath(clipPath) {
            drawImage(
                image = revealBitmap,
                srcSize = IntSize(revealBitmap.width, revealBitmap.height),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }
    }
}