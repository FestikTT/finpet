package ru.finpet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

/**
 * Модификатор горизонтальной тряски (Shake) для ошибок, недостатка монет и превышения лимитов.
 * Смещает элемент влево-вправо на [offsetDp] с затухающей синусоидой в течение 200 мс.
 */
fun Modifier.shake(
    trigger: Int,
    offsetDp: Float = 6f
): Modifier = composed {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            shakeOffset.snapTo(0f)
            // Быстрое затухающее колебание: влево-вправо-влево-вправо-ноль
            shakeOffset.animateTo(-offsetDp, tween(35, easing = LinearEasing))
            shakeOffset.animateTo(offsetDp, tween(40, easing = LinearEasing))
            shakeOffset.animateTo(-offsetDp * 0.6f, tween(40, easing = LinearEasing))
            shakeOffset.animateTo(offsetDp * 0.4f, tween(45, easing = LinearEasing))
            shakeOffset.animateTo(0f, tween(40, easing = LinearEasing))
        }
    }

    this.graphicsLayer {
        translationX = shakeOffset.value * density
    }
}

/**
 * Частица конфетти
 */
private class ConfettiParticle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var rotation: Float,
    val vRotation: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean
)

/**
 * Полноэкранный взрыв праздничного конфетти при 100% достижении цели или победе
 */
@Composable
fun FullscreenConfettiCelebration(
    title: String = "Цель достигнута! 🏆",
    subtitle: String = "Ты блестяще накопил всю сумму и доказал финансовую дисциплину!",
    onDismiss: () -> Unit
) {
    val currentTheme = LocalAppTheme.current

    // Тактильный победный отклик Duolingo double-bounce
    LaunchedEffect(Unit) {
        SoundHapticManager.performSuccessHaptic()
        delay(400)
        SoundHapticManager.performStreakHaptic()
    }

    // Покачивание кубка (wobble)
    val infiniteTransition = rememberInfiniteTransition(label = "trophyWobble")
    val trophyAngle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyAngle"
    )

    // Частицы конфетти
    val colors = listOf(
        FinCoinGold,
        BrandRoseWarm,
        BrandVioletPrimary,
        StatGreenEmerald,
        Color(0xFF38BDF8),
        Color(0xFFF43F5E),
        Color(0xFFA855F7),
        Color(0xFFFBBF24)
    )

    val particles = remember {
        List(48) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.4f - 0.3f,
                vx = (Random.nextFloat() - 0.5f) * 0.005f,
                vy = Random.nextFloat() * 0.007f + 0.003f,
                rotation = Random.nextFloat() * 360f,
                vRotation = (Random.nextFloat() - 0.5f) * 12f,
                size = Random.nextFloat() * 12f + 8f,
                color = colors[it % colors.size],
                isCircle = Random.nextBoolean()
            )
        }
    }

    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime ->
                frameTick = frameTime
                particles.forEach { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.rotation += p.vRotation
                    if (p.y > 1.1f) {
                        p.y = -0.1f
                        p.x = Random.nextFloat()
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Отрисовка падающего конфетти на Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Чтение frameTick обеспечивает перерисовку каждого кадра анимации
                if (frameTick >= 0) {
                    particles.forEach { p ->
                    val px = p.x * w
                    val py = p.y * h

                    rotate(p.rotation, pivot = Offset(px, py)) {
                        if (p.isCircle) {
                            drawCircle(
                                color = p.color,
                                radius = p.size / 2f,
                                center = Offset(px, py)
                            )
                        } else {
                            drawRoundRect(
                                color = p.color,
                                topLeft = Offset(px - p.size / 2f, py - p.size / 3f),
                                size = Size(p.size, p.size * 0.65f),
                                cornerRadius = CornerRadius(3f, 3f)
                            )
                        }
                    }
                }
            }
        }

        // Центральная праздничная карточка с кубком
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceLight,
                shadowElevation = 16.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, FinCoinGold),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Покачивающийся золотой кубок
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(FinCoinGold.copy(alpha = 0.2f))
                            .graphicsLayer { rotationZ = trophyAngle },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 48.sp)
                    }

                    Text(
                        text = title,
                        fontFamily = UnboundedFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    CartoonButton(
                        text = "Ура! Отлично 🎉",
                        onClick = onDismiss,
                        containerColor = currentTheme.primaryColor,
                        height = 46.dp,
                        fontSize = 13.5.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Анимированные золотые монетки, летящие дугой в копилку
 */
@Composable
fun FlyingCoinArcOverlay(
    isFlying: Boolean,
    onCoinLanded: () -> Unit,
    onAllLanded: () -> Unit
) {
    if (!isFlying) return

    val coinProgress = remember { Animatable(0f) }

    LaunchedEffect(isFlying) {
        if (isFlying) {
            coinProgress.snapTo(0f)
            // Запуск пролета монеток с дискретными тиками
            launch {
                repeat(4) { idx ->
                    delay((idx * 80).toLong())
                    onCoinLanded()
                }
            }
            coinProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(450, easing = FastOutSlowInEasing)
            )
            onAllLanded()
        }
    }

    val t = coinProgress.value
    val coinColor = FinCoinGold
    val coinGlowColor = coinColor.copy(alpha = 0.35f)

    if (t in 0.01f..0.99f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Точки траектории дуги: от кнопки внизу к прогресс-бару сверху
            val startX = w * 0.78f
            val startY = h * 0.58f
            val endX = w * 0.45f
            val endY = h * 0.42f

            repeat(4) { i ->
                val delayedT = (t - i * 0.12f).coerceIn(0f, 1f)
                if (delayedT > 0f && delayedT < 1f) {
                    val arcHeight = 80f * sin(delayedT * Math.PI).toFloat()
                    val curX = startX + (endX - startX) * delayedT
                    val curY = startY + (endY - startY) * delayedT - arcHeight

                    // Золотая монетка со свечением
                    drawCircle(
                        color = coinGlowColor,
                        radius = 14f,
                        center = Offset(curX, curY)
                    )
                    drawCircle(
                        color = coinColor,
                        radius = 9f,
                        center = Offset(curX, curY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 3.5f,
                        center = Offset(curX - 2.5f, curY - 2.5f)
                    )
                }
            }
        }
    }
}
