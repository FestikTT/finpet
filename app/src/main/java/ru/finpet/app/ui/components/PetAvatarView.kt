package ru.finpet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.app.model.*
import ru.finpet.app.ui.theme.*

/**
 * Высокодетализированная живая модель питомцев.
 * Включает:
 * - Уникальную анатомию: пушистый анимированный хвост у лисенка, усики и кошачий хвост у котика,
 *   черные очки-овалы и темные лапки у панды.
 * - Плавную физику парения, дыхания и моргания (Spring/Sine wave).
 * - Анимированное виляние хвостом.
 * - Эволюционные визуальные знаки отличия (корона, росток, медальон).
 */
@Composable
fun PetAvatarView(
    pet: Pet,
    modifier: Modifier = Modifier,
    showStageBadge: Boolean = true,
    onPetClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var isTapped by remember { mutableStateOf(false) }
    var isClickLocked by remember { mutableStateOf(false) }

    // Пружинный радостный отклик при нажатии на питомца (Squash & Stretch с подпрыгиванием)
    val tapScaleX by animateFloatAsState(
        targetValue = if (isTapped) 1.14f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "petBounceTapX"
    )
    val tapScaleY by animateFloatAsState(
        targetValue = if (isTapped) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "petBounceTapY"
    )
    val tapJumpY by animateFloatAsState(
        targetValue = if (isTapped) -10f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "petBounceTapJump"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "petLifeCycle")

    // Плавное парение в пространстве (idle дыхание)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatHover"
    )

    // Дыхание тела
    val breathScaleY by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathBody"
    )


    // Естественное редкое моргание глаз
    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                1.0f at 0
                1.0f at 3800
                0.05f at 3950
                1.0f at 4050
                1.0f at 4200
            }
        ),
        label = "blinkEyes"
    )

    // Покачивание ушек
    val earWiggle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "earWiggle"
    )

    // Анимация всплывающих сонных пузырьков Zzz при сне
    val zzzOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sleepZzzOffset"
    )
    val zzzAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sleepZzzAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = tapScaleX * 0.77f
                scaleY = tapScaleY * 0.77f
                translationY = tapJumpY * density
            }
            .clickable(
                enabled = !isClickLocked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isClickLocked) return@clickable
                isClickLocked = true
                ru.finpet.app.audio.SoundHapticManager.performPetHaptic()
                coroutineScope.launch {
                    isTapped = true
                    delay(160)
                    isTapped = false
                    // Блокировка повторных тапов до полного завершения пружинной анимации
                    delay(360)
                    isClickLocked = false
                }
                onPetClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        val targetSize = if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified && maxWidth > 0.dp) {
            minOf(maxWidth, maxHeight.takeIf { it != androidx.compose.ui.unit.Dp.Unspecified && it > 0.dp } ?: maxWidth)
        } else {
            154.dp
        }
        val canvasSize = if (targetSize > 100.dp) targetSize - 16.dp else targetSize
        val scaleFactor = (targetSize.value / 154f).coerceIn(0.3f, 2f)

        Canvas(
            modifier = Modifier
                .size(canvasSize)
                .offset(y = (floatOffset * scaleFactor).dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Определение цветовых палитр в зависимости от типа и расцветки
            val primaryBase = when (pet.type) {
                PetType.PANDA -> Color(0xFFF9FAFB) // Тело панды белое
                else -> Color(pet.color.primaryColorHex)
            }
            val secondaryBase = Color(pet.color.secondaryColorHex)

            val lightHighlight = primaryBase.copy(
                red = (primaryBase.red * 1.25f).coerceAtMost(1f),
                green = (primaryBase.green * 1.25f).coerceAtMost(1f),
                blue = (primaryBase.blue * 1.25f).coerceAtMost(1f)
            )
            val deepShadow = when (pet.type) {
                PetType.PANDA -> Color(0xFFD1D5DB)
                else -> primaryBase.copy(
                    red = primaryBase.red * 0.70f,
                    green = primaryBase.green * 0.70f,
                    blue = primaryBase.blue * 0.70f
                )
            }

            // 1. Мягкая тень на полу
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3512141A), Color(0x1012141A), Color.Transparent),
                    center = Offset(centerX, centerY + height * 0.44f),
                    radius = width * 0.40f
                ),
                topLeft = Offset(centerX - width * 0.38f, centerY + height * 0.38f - floatOffset * 0.4f),
                size = Size(width * 0.76f, height * 0.16f)
            )

            // 3. Ушки
            when (pet.type) {
                PetType.FOX -> drawFoxEars(centerX, centerY, width, height, primaryBase, lightHighlight, deepShadow, secondaryBase, earWiggle)
                PetType.CAT -> drawCatEars(centerX, centerY, width, height, primaryBase, lightHighlight, deepShadow, earWiggle)
                PetType.PANDA -> drawPandaEars(centerX, centerY, width, height, earWiggle)
            }

            // 4. Тело персонажа (сферическое с мягким 3D градиентом)
            val bodyCenter = Offset(centerX, centerY + height * 0.05f)
            val bodyRadiusX = width * 0.36f
            val bodyRadiusY = height * 0.34f * breathScaleY

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(lightHighlight, primaryBase, deepShadow),
                    center = Offset(centerX - width * 0.12f, centerY - height * 0.08f),
                    radius = width * 0.44f
                ),
                topLeft = Offset(bodyCenter.x - bodyRadiusX, bodyCenter.y - bodyRadiusY),
                size = Size(bodyRadiusX * 2f, bodyRadiusY * 2f)
            )

            // Мультяшный аниме-контур персонажа
            drawOval(
                color = Color(0x381E293B),
                topLeft = Offset(bodyCenter.x - bodyRadiusX, bodyCenter.y - bodyRadiusY),
                size = Size(bodyRadiusX * 2f, bodyRadiusY * 2f),
                style = Stroke(width = 2.5f)
            )

            // Глянцевый блик на лбу
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(centerX - width * 0.14f, centerY - height * 0.14f),
                    radius = width * 0.16f
                ),
                topLeft = Offset(centerX - width * 0.22f, centerY - height * 0.20f),
                size = Size(width * 0.22f, height * 0.13f)
            )

            // 5. Характерные черты лица и шерсти
            when (pet.type) {
                PetType.FOX -> {
                    // Пушистые белые щёчки лисенка
                    drawFoxCheekFur(centerX, centerY, width, height, secondaryBase)
                }
                PetType.CAT -> {
                    // Белая грудка котика
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, secondaryBase),
                            center = Offset(centerX, centerY + height * 0.12f),
                            radius = width * 0.22f
                        ),
                        topLeft = Offset(centerX - width * 0.18f, centerY + height * 0.06f),
                        size = Size(width * 0.36f, height * 0.22f)
                    )
                    // Полосочки на лбу котика
                    drawCatForeheadStripes(centerX, centerY, width, height, deepShadow)
                }
                PetType.PANDA -> {
                    // Чёрные плечики и лапки панды
                    drawPandaShoulders(centerX, centerY, width, height)
                }
            }

            // Мордочка вокруг носика
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, secondaryBase),
                    center = Offset(centerX, centerY - height * 0.01f),
                    radius = width * 0.20f
                ),
                topLeft = Offset(centerX - width * 0.20f, centerY - height * 0.05f),
                size = Size(width * 0.40f, height * 0.18f)
            )

            // 6. Пятна вокруг глаз для панды
            if (pet.type == PetType.PANDA) {
                drawPandaEyePatches(centerX, centerY, width, height)
            }

            // Одежда: стильный полосатый свитер и сумочка через плечо (как на референсе)
            drawStripedSweaterAndBag(centerX, centerY, width, height, breathScaleY)

            // 7. Глазки (живые, выразительные 3D-глаза с двойными бликами в стиле Pixar)
            if (pet.currentMood == PetMood.SLEEPING) {
                // Закрытые спящие веки дугой
                val stroke = Stroke(width = 5f, cap = StrokeCap.Round)
                drawArc(
                    color = Color(0xFF1F222A),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(centerX - width * 0.22f, centerY - height * 0.10f),
                    size = Size(width * 0.13f, height * 0.08f),
                    style = stroke
                )
                drawArc(
                    color = Color(0xFF1F222A),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(centerX + width * 0.09f, centerY - height * 0.10f),
                    size = Size(width * 0.13f, height * 0.08f),
                    style = stroke
                )
            } else {
                val eyeRadius = width * 0.078f
                val leftEyeCenter = Offset(centerX - width * 0.15f, centerY - height * 0.07f)
                val rightEyeCenter = Offset(centerX + width * 0.15f, centerY - height * 0.07f)
                val blinkHeightScale = eyeBlink

                // Милые бровки дугой над глазами
                drawArc(
                    color = Color(0xFF1E293B).copy(alpha = 0.55f),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(leftEyeCenter.x - eyeRadius * 0.8f, leftEyeCenter.y - eyeRadius * 1.5f),
                    size = Size(eyeRadius * 1.6f, eyeRadius * 0.6f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF1E293B).copy(alpha = 0.55f),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(rightEyeCenter.x - eyeRadius * 0.8f, rightEyeCenter.y - eyeRadius * 1.5f),
                    size = Size(eyeRadius * 1.6f, eyeRadius * 0.6f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )

                // Глазные яблоки (глубокий живой 3D-градиент с тенью от верхнего века)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2E333F), Color(0xFF0F1115)),
                        center = leftEyeCenter,
                        radius = eyeRadius * 1.1f
                    ),
                    topLeft = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius * blinkHeightScale),
                    size = Size(eyeRadius * 2f, eyeRadius * 2f * blinkHeightScale)
                )
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2E333F), Color(0xFF0F1115)),
                        center = rightEyeCenter,
                        radius = eyeRadius * 1.1f
                    ),
                    topLeft = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y - eyeRadius * blinkHeightScale),
                    size = Size(eyeRadius * 2f, eyeRadius * 2f * blinkHeightScale)
                )

                if (eyeBlink > 0.35f) {
                    // Главный крупный глянцевый блик (сверху слева)
                    drawCircle(
                        color = Color.White,
                        radius = eyeRadius * 0.42f,
                        center = Offset(leftEyeCenter.x - eyeRadius * 0.30f, leftEyeCenter.y - eyeRadius * 0.30f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = eyeRadius * 0.42f,
                        center = Offset(rightEyeCenter.x - eyeRadius * 0.30f, rightEyeCenter.y - eyeRadius * 0.30f)
                    )

                    // Вторичный нижний блик объема (рефлекс)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.70f),
                        radius = eyeRadius * 0.22f,
                        center = Offset(leftEyeCenter.x + eyeRadius * 0.34f, leftEyeCenter.y + eyeRadius * 0.32f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.70f),
                        radius = eyeRadius * 0.22f,
                        center = Offset(rightEyeCenter.x + eyeRadius * 0.34f, rightEyeCenter.y + eyeRadius * 0.32f)
                    )

                    // Третий микро-блик (сияние)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.90f),
                        radius = eyeRadius * 0.12f,
                        center = Offset(leftEyeCenter.x - eyeRadius * 0.05f, leftEyeCenter.y + eyeRadius * 0.35f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.90f),
                        radius = eyeRadius * 0.12f,
                        center = Offset(rightEyeCenter.x - eyeRadius * 0.05f, rightEyeCenter.y + eyeRadius * 0.35f)
                    )
                }
            }

            // 8. Милые румяные щёчки
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFB7185).copy(alpha = 0.40f), Color.Transparent),
                    center = Offset(centerX - width * 0.24f, centerY + height * 0.03f),
                    radius = width * 0.07f
                ),
                topLeft = Offset(centerX - width * 0.30f, centerY - height * 0.01f),
                size = Size(width * 0.14f, height * 0.08f)
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFB7185).copy(alpha = 0.40f), Color.Transparent),
                    center = Offset(centerX + width * 0.24f, centerY + height * 0.03f),
                    radius = width * 0.07f
                ),
                topLeft = Offset(centerX + width * 0.16f, centerY - height * 0.01f),
                size = Size(width * 0.14f, height * 0.08f)
            )

            // 9. Носик и усики
            val noseCenter = Offset(centerX, centerY + height * 0.015f)
            val noseColor = if (pet.type == PetType.CAT) Color(0xFFF472B6) else Color(0xFF22242B)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(noseColor.copy(alpha = 0.9f), noseColor),
                    center = noseCenter,
                    radius = width * 0.04f
                ),
                topLeft = Offset(noseCenter.x - width * 0.035f, noseCenter.y - height * 0.02f),
                size = Size(width * 0.07f, height * 0.045f)
            )
            // Блик на носике
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = width * 0.011f,
                center = Offset(noseCenter.x - width * 0.01f, noseCenter.y - height * 0.01f)
            )

            // Кошачьи усики
            if (pet.type == PetType.CAT) {
                drawCatWhiskers(centerX, centerY, width, height)
            }

            // 10. Улыбочка
            drawArc(
                color = Color(0xFF232630),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(centerX - width * 0.07f, centerY + height * 0.025f),
                size = Size(width * 0.14f, height * 0.06f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // 11. Лапки
            drawPetPaws(centerX, centerY, width, height, pet.type, primaryBase, lightHighlight, deepShadow)

            // 13. Аксессуары
            drawAccessories(centerX, centerY, width, height, pet.accessory)

            // 14. Эволюционные знаки отличия
            when (pet.evolutionStage) {
                EvolutionStage.MASTER -> drawMasterCrown(centerX, centerY, width, height)
                EvolutionStage.TEEN -> drawTeenMedal(centerX, centerY, width, height)
                EvolutionStage.BABY -> drawBabySprout(centerX, centerY, width, height)
            }
        }

        // Стадия развития (премиальный бейдж) - только если включено и размер карточки достаточно большой
        if (showStageBadge && targetSize >= 140.dp) {
            val stageBadgeTitle = when (pet.evolutionStage) {
                EvolutionStage.BABY -> "🌱 Малыш"
                EvolutionStage.TEEN -> "⭐ Финансист"
                EvolutionStage.MASTER -> "👑 Эксперт"
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceLight,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
            ) {
                Text(
                    text = stageBadgeTitle,
                    color = BrandVioletPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

// =========================================================================
// Прорисовка уникальных элементов персонажей (Хвосты, Уши, Лапки, Усики)
// =========================================================================

private fun DrawScope.drawFoxTail(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    swayAngle: Float,
    primaryColor: Color,
    lightColor: Color,
    shadowColor: Color,
    tipColor: Color
) {
    // Вращение хвоста вокруг точки крепления (справа внизу)
    val pivot = Offset(centerX + width * 0.24f, centerY + height * 0.22f)
    rotate(swayAngle, pivot = pivot) {
        val tailPath = Path().apply {
            moveTo(pivot.x, pivot.y)
            cubicTo(
                pivot.x + width * 0.26f, pivot.y + height * 0.05f,
                pivot.x + width * 0.38f, pivot.y - height * 0.18f,
                pivot.x + width * 0.28f, pivot.y - height * 0.36f
            )
            cubicTo(
                pivot.x + width * 0.16f, pivot.y - height * 0.26f,
                pivot.x + width * 0.12f, pivot.y - height * 0.08f,
                pivot.x, pivot.y
            )
            close()
        }
        drawPath(
            path = tailPath,
            brush = Brush.linearGradient(
                colors = listOf(lightColor, primaryColor, shadowColor),
                start = Offset(pivot.x + width * 0.28f, pivot.y - height * 0.36f),
                end = pivot
            )
        )
        // Белый пушистый кончик хвоста лисенка
        val whiteTip = Path().apply {
            moveTo(pivot.x + width * 0.22f, pivot.y - height * 0.26f)
            lineTo(pivot.x + width * 0.28f, pivot.y - height * 0.36f)
            lineTo(pivot.x + width * 0.32f, pivot.y - height * 0.28f)
            close()
        }
        drawPath(whiteTip, tipColor)
    }
}

private fun DrawScope.drawCatTail(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    swayAngle: Float,
    primaryColor: Color,
    lightColor: Color,
    shadowColor: Color
) {
    val pivot = Offset(centerX + width * 0.22f, centerY + height * 0.24f)
    rotate(swayAngle * 0.8f, pivot = pivot) {
        val tailPath = Path().apply {
            moveTo(pivot.x, pivot.y)
            cubicTo(
                pivot.x + width * 0.20f, pivot.y + height * 0.02f,
                pivot.x + width * 0.28f, pivot.y - height * 0.14f,
                pivot.x + width * 0.20f, pivot.y - height * 0.26f
            )
            cubicTo(
                pivot.x + width * 0.14f, pivot.y - height * 0.22f,
                pivot.x + width * 0.15f, pivot.y - height * 0.06f,
                pivot.x, pivot.y
            )
            close()
        }
        drawPath(
            path = tailPath,
            brush = Brush.linearGradient(
                colors = listOf(lightColor, primaryColor, shadowColor),
                start = Offset(pivot.x + width * 0.20f, pivot.y - height * 0.26f),
                end = pivot
            )
        )
    }
}

private fun DrawScope.drawPandaTail(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float
) {
    // Маленький круглый черный хвостик панды
    drawCircle(
        color = Color(0xFF1E2026),
        radius = width * 0.05f,
        center = Offset(centerX + width * 0.28f, centerY + height * 0.20f)
    )
}

private fun DrawScope.drawFoxEars(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    primaryColor: Color,
    lightColor: Color,
    shadowColor: Color,
    innerColor: Color,
    earWiggle: Float
) {
    // Левое ухо (плотно врастает в контур черепа)
    rotate(earWiggle, pivot = Offset(centerX - width * 0.22f, centerY - height * 0.16f)) {
        val leftEar = Path().apply {
            moveTo(centerX - width * 0.32f, centerY - height * 0.12f)
            lineTo(centerX - width * 0.28f, centerY - height * 0.42f)
            lineTo(centerX - width * 0.10f, centerY - height * 0.24f)
            lineTo(centerX - width * 0.20f, centerY - height * 0.15f)
            close()
        }
        drawPath(
            path = leftEar,
            brush = Brush.linearGradient(
                colors = listOf(lightColor, primaryColor, shadowColor),
                start = Offset(centerX - width * 0.28f, centerY - height * 0.42f),
                end = Offset(centerX - width * 0.20f, centerY - height * 0.14f)
            )
        )
        // Внутреннее светлое ушко
        val leftInner = Path().apply {
            moveTo(centerX - width * 0.28f, centerY - height * 0.16f)
            lineTo(centerX - width * 0.26f, centerY - height * 0.36f)
            lineTo(centerX - width * 0.14f, centerY - height * 0.23f)
            close()
        }
        drawPath(leftInner, innerColor)

        // Черный кончик ушка лисенка
        val leftTip = Path().apply {
            moveTo(centerX - width * 0.30f, centerY - height * 0.36f)
            lineTo(centerX - width * 0.28f, centerY - height * 0.42f)
            lineTo(centerX - width * 0.22f, centerY - height * 0.37f)
            close()
        }
        drawPath(leftTip, Color(0xFF202229))
    }

    // Правое ухо (симметрично прилегает к черепу)
    rotate(-earWiggle, pivot = Offset(centerX + width * 0.22f, centerY - height * 0.16f)) {
        val rightEar = Path().apply {
            moveTo(centerX + width * 0.32f, centerY - height * 0.12f)
            lineTo(centerX + width * 0.28f, centerY - height * 0.42f)
            lineTo(centerX + width * 0.10f, centerY - height * 0.24f)
            lineTo(centerX + width * 0.20f, centerY - height * 0.15f)
            close()
        }
        drawPath(
            path = rightEar,
            brush = Brush.linearGradient(
                colors = listOf(lightColor, primaryColor, shadowColor),
                start = Offset(centerX + width * 0.28f, centerY - height * 0.42f),
                end = Offset(centerX + width * 0.20f, centerY - height * 0.14f)
            )
        )
        val rightInner = Path().apply {
            moveTo(centerX + width * 0.28f, centerY - height * 0.16f)
            lineTo(centerX + width * 0.26f, centerY - height * 0.36f)
            lineTo(centerX + width * 0.14f, centerY - height * 0.23f)
            close()
        }
        drawPath(rightInner, innerColor)

        val rightTip = Path().apply {
            moveTo(centerX + width * 0.30f, centerY - height * 0.36f)
            lineTo(centerX + width * 0.28f, centerY - height * 0.42f)
            lineTo(centerX + width * 0.22f, centerY - height * 0.37f)
            close()
        }
        drawPath(rightTip, Color(0xFF202229))
    }
}

private fun DrawScope.drawCatEars(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    primaryColor: Color,
    lightColor: Color,
    shadowColor: Color,
    earWiggle: Float
) {
    rotate(earWiggle, pivot = Offset(centerX - width * 0.19f, centerY - height * 0.16f)) {
        val leftEar = Path().apply {
            moveTo(centerX - width * 0.29f, centerY - height * 0.12f)
            lineTo(centerX - width * 0.24f, centerY - height * 0.39f)
            lineTo(centerX - width * 0.08f, centerY - height * 0.24f)
            lineTo(centerX - width * 0.18f, centerY - height * 0.15f)
            close()
        }
        drawPath(leftEar, Brush.linearGradient(listOf(lightColor, primaryColor, shadowColor)))
        val leftInner = Path().apply {
            moveTo(centerX - width * 0.25f, centerY - height * 0.16f)
            lineTo(centerX - width * 0.22f, centerY - height * 0.33f)
            lineTo(centerX - width * 0.12f, centerY - height * 0.23f)
            close()
        }
        drawPath(leftInner, Color(0xFFFFD6E4))
    }

    rotate(-earWiggle, pivot = Offset(centerX + width * 0.19f, centerY - height * 0.16f)) {
        val rightEar = Path().apply {
            moveTo(centerX + width * 0.29f, centerY - height * 0.12f)
            lineTo(centerX + width * 0.24f, centerY - height * 0.39f)
            lineTo(centerX + width * 0.08f, centerY - height * 0.24f)
            lineTo(centerX + width * 0.18f, centerY - height * 0.15f)
            close()
        }
        drawPath(rightEar, Brush.linearGradient(listOf(lightColor, primaryColor, shadowColor)))
        val rightInner = Path().apply {
            moveTo(centerX + width * 0.25f, centerY - height * 0.16f)
            lineTo(centerX + width * 0.22f, centerY - height * 0.33f)
            lineTo(centerX + width * 0.12f, centerY - height * 0.23f)
            close()
        }
        drawPath(rightInner, Color(0xFFFFD6E4))
    }
}

private fun DrawScope.drawPandaEars(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    earWiggle: Float
) {
    rotate(earWiggle, pivot = Offset(centerX - width * 0.26f, centerY - height * 0.28f)) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF383B44), Color(0xFF141518)),
                center = Offset(centerX - width * 0.26f, centerY - height * 0.28f),
                radius = width * 0.12f
            ),
            topLeft = Offset(centerX - width * 0.36f, centerY - height * 0.38f),
            size = Size(width * 0.20f, height * 0.20f)
        )
    }
    rotate(-earWiggle, pivot = Offset(centerX + width * 0.26f, centerY - height * 0.28f)) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF383B44), Color(0xFF141518)),
                center = Offset(centerX + width * 0.26f, centerY - height * 0.28f),
                radius = width * 0.12f
            ),
            topLeft = Offset(centerX + width * 0.16f, centerY - height * 0.38f),
            size = Size(width * 0.20f, height * 0.20f)
        )
    }
}

private fun DrawScope.drawFoxCheekFur(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    secondaryColor: Color
) {
    // Левая белая щечка лисенка
    val leftCheek = Path().apply {
        moveTo(centerX - width * 0.10f, centerY - height * 0.04f)
        lineTo(centerX - width * 0.36f, centerY + height * 0.08f)
        lineTo(centerX - width * 0.30f, centerY + height * 0.15f)
        lineTo(centerX - width * 0.05f, centerY + height * 0.18f)
        close()
    }
    drawPath(leftCheek, secondaryColor)

    // Правая белая щечка
    val rightCheek = Path().apply {
        moveTo(centerX + width * 0.10f, centerY - height * 0.04f)
        lineTo(centerX + width * 0.36f, centerY + height * 0.08f)
        lineTo(centerX + width * 0.30f, centerY + height * 0.15f)
        lineTo(centerX + width * 0.05f, centerY + height * 0.18f)
        close()
    }
    drawPath(rightCheek, secondaryColor)
}

private fun DrawScope.drawPandaEyePatches(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float
) {
    // Наклонные черные овалы вокруг глаз панды
    rotate(-18f, pivot = Offset(centerX - width * 0.14f, centerY - height * 0.07f)) {
        drawOval(
            color = Color(0xFF181A20),
            topLeft = Offset(centerX - width * 0.21f, centerY - height * 0.13f),
            size = Size(width * 0.15f, height * 0.13f)
        )
    }
    rotate(18f, pivot = Offset(centerX + width * 0.14f, centerY - height * 0.07f)) {
        drawOval(
            color = Color(0xFF181A20),
            topLeft = Offset(centerX + width * 0.06f, centerY - height * 0.13f),
            size = Size(width * 0.15f, height * 0.13f)
        )
    }
}

private fun DrawScope.drawPandaShoulders(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float
) {
    // Черная жилетка панды на теле
    val shoulderPath = Path().apply {
        moveTo(centerX - width * 0.34f, centerY + height * 0.08f)
        lineTo(centerX + width * 0.34f, centerY + height * 0.08f)
        lineTo(centerX + width * 0.32f, centerY + height * 0.24f)
        lineTo(centerX - width * 0.32f, centerY + height * 0.24f)
        close()
    }
    drawPath(shoulderPath, Color(0xFF1E2026))
}

private fun DrawScope.drawCatForeheadStripes(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    color: Color
) {
    drawLine(color, Offset(centerX, centerY - height * 0.18f), Offset(centerX, centerY - height * 0.11f), strokeWidth = 4f, cap = StrokeCap.Round)
    drawLine(color, Offset(centerX - width * 0.05f, centerY - height * 0.16f), Offset(centerX - width * 0.04f, centerY - height * 0.10f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(color, Offset(centerX + width * 0.05f, centerY - height * 0.16f), Offset(centerX + width * 0.04f, centerY - height * 0.10f), strokeWidth = 3f, cap = StrokeCap.Round)
}

private fun DrawScope.drawCatWhiskers(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float
) {
    val whiskerColor = Color(0x992B2D38)
    // Левые усики
    drawLine(whiskerColor, Offset(centerX - width * 0.14f, centerY + height * 0.01f), Offset(centerX - width * 0.34f, centerY - height * 0.01f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(whiskerColor, Offset(centerX - width * 0.14f, centerY + height * 0.03f), Offset(centerX - width * 0.33f, centerY + height * 0.05f), strokeWidth = 2.5f, cap = StrokeCap.Round)

    // Правые усики
    drawLine(whiskerColor, Offset(centerX + width * 0.14f, centerY + height * 0.01f), Offset(centerX + width * 0.34f, centerY - height * 0.01f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(whiskerColor, Offset(centerX + width * 0.14f, centerY + height * 0.03f), Offset(centerX + width * 0.33f, centerY + height * 0.05f), strokeWidth = 2.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawPetPaws(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    type: PetType,
    primaryColor: Color,
    lightColor: Color,
    shadowColor: Color
) {
    val pawY = centerY + height * 0.28f
    val pawColor = if (type == PetType.PANDA) Color(0xFF1E2026) else primaryColor

    // Левая лапка
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(lightColor, pawColor, shadowColor),
            center = Offset(centerX - width * 0.13f, pawY - height * 0.02f),
            radius = width * 0.08f
        ),
        topLeft = Offset(centerX - width * 0.18f, pawY - height * 0.04f),
        size = Size(width * 0.12f, height * 0.08f)
    )
    // Правая лапка
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(lightColor, pawColor, shadowColor),
            center = Offset(centerX + width * 0.11f, pawY - height * 0.02f),
            radius = width * 0.08f
        ),
        topLeft = Offset(centerX + width * 0.06f, pawY - height * 0.04f),
        size = Size(width * 0.12f, height * 0.08f)
    )

    // Розовые подушечки лап у котика
    if (type == PetType.CAT) {
        drawCircle(Color(0xFFFFD6E4), radius = width * 0.020f, center = Offset(centerX - width * 0.12f, pawY))
        drawCircle(Color(0xFFFFD6E4), radius = width * 0.020f, center = Offset(centerX + width * 0.12f, pawY))
    }
}

private fun DrawScope.drawAccessories(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    accessory: PetAccessory
) {
    when (accessory) {
        PetAccessory.GLASSES -> {
            val frameColor = Color(0xFF1E2026)
            val lensFill = Color(0x3360A5FA)

            // Левая линза и оправа
            drawRoundRect(
                color = lensFill,
                topLeft = Offset(centerX - width * 0.24f, centerY - height * 0.14f),
                size = Size(width * 0.19f, height * 0.13f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = frameColor,
                topLeft = Offset(centerX - width * 0.24f, centerY - height * 0.14f),
                size = Size(width * 0.19f, height * 0.13f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 4.5f)
            )

            // Правая линза и оправа
            drawRoundRect(
                color = lensFill,
                topLeft = Offset(centerX + width * 0.05f, centerY - height * 0.14f),
                size = Size(width * 0.19f, height * 0.13f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = frameColor,
                topLeft = Offset(centerX + width * 0.05f, centerY - height * 0.14f),
                size = Size(width * 0.19f, height * 0.13f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 4.5f)
            )

            // Переносица
            drawLine(
                color = frameColor,
                start = Offset(centerX - width * 0.05f, centerY - height * 0.08f),
                end = Offset(centerX + width * 0.05f, centerY - height * 0.08f),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )

            // Блики на линзах
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(centerX - width * 0.20f, centerY - height * 0.12f),
                end = Offset(centerX - width * 0.11f, centerY - height * 0.04f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(centerX + width * 0.09f, centerY - height * 0.12f),
                end = Offset(centerX + width * 0.18f, centerY - height * 0.04f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        PetAccessory.CAP -> {
            val capPath = Path().apply {
                moveTo(centerX - width * 0.28f, centerY - height * 0.22f)
                lineTo(centerX + width * 0.28f, centerY - height * 0.22f)
                cubicTo(
                    centerX + width * 0.24f, centerY - height * 0.38f,
                    centerX - width * 0.24f, centerY - height * 0.38f,
                    centerX - width * 0.28f, centerY - height * 0.22f
                )
                close()
            }
            drawPath(
                path = capPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0052FF), Color(0xFF1E1B4B)),
                    startY = centerY - height * 0.38f,
                    endY = centerY - height * 0.22f
                )
            )
            // Пуговка на макушке кепки
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = width * 0.02f,
                center = Offset(centerX, centerY - height * 0.38f)
            )
            // Козырек кепки
            val visorPath = Path().apply {
                moveTo(centerX - width * 0.26f, centerY - height * 0.22f)
                cubicTo(
                    centerX - width * 0.20f, centerY - height * 0.28f,
                    centerX + width * 0.20f, centerY - height * 0.28f,
                    centerX + width * 0.32f, centerY - height * 0.22f
                )
                lineTo(centerX + width * 0.26f, centerY - height * 0.18f)
                cubicTo(
                    centerX + width * 0.14f, centerY - height * 0.22f,
                    centerX - width * 0.14f, centerY - height * 0.22f,
                    centerX - width * 0.26f, centerY - height * 0.22f
                )
                close()
            }
            drawPath(
                path = visorPath,
                brush = Brush.horizontalGradient(listOf(BrandLavender, BrandRoseWarm))
            )
        }

        PetAccessory.BOW -> {
            val bowColor = Color(0xFFF43F5E)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.5f), bowColor),
                    center = Offset(centerX, centerY - height * 0.26f),
                    radius = width * 0.04f
                ),
                radius = width * 0.035f,
                center = Offset(centerX, centerY - height * 0.26f)
            )
            val leftBow = Path().apply {
                moveTo(centerX, centerY - height * 0.26f)
                lineTo(centerX - width * 0.11f, centerY - height * 0.31f)
                lineTo(centerX - width * 0.11f, centerY - height * 0.21f)
                close()
            }
            drawPath(leftBow, Brush.horizontalGradient(listOf(Color(0xFFBE123C), bowColor)))
            val rightBow = Path().apply {
                moveTo(centerX, centerY - height * 0.26f)
                lineTo(centerX + width * 0.11f, centerY - height * 0.31f)
                lineTo(centerX + width * 0.11f, centerY - height * 0.21f)
                close()
            }
            drawPath(rightBow, Brush.horizontalGradient(listOf(bowColor, Color(0xFFBE123C))))
        }

        PetAccessory.SCARF -> {
            val scarfBrush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFE11D48)))

            // 1. Обернутый воротник шарфика вокруг шеи
            drawRoundRect(
                brush = scarfBrush,
                topLeft = Offset(centerX - width * 0.28f, centerY + height * 0.17f),
                size = Size(width * 0.56f, height * 0.10f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Белые вязаные полосочки на воротнике
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(centerX - width * 0.12f, centerY + height * 0.17f),
                end = Offset(centerX - width * 0.12f, centerY + height * 0.27f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(centerX - width * 0.04f, centerY + height * 0.17f),
                end = Offset(centerX - width * 0.04f, centerY + height * 0.27f),
                strokeWidth = 3f
            )

            // 2. Узелок шарфика справа
            val knotCenter = Offset(centerX + width * 0.10f, centerY + height * 0.23f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFB7185), Color(0xFFBE123C)),
                    center = knotCenter,
                    radius = width * 0.06f
                ),
                radius = width * 0.055f,
                center = knotCenter
            )

            // 3. Свисающий хвостик шарфа с бахромой
            val tailLeft = centerX + width * 0.05f
            val tailTop = centerY + height * 0.23f
            val tailWidth = width * 0.14f
            val tailHeight = height * 0.15f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFB7185), Color(0xFFE11D48)),
                    startY = tailTop,
                    endY = tailTop + tailHeight
                ),
                topLeft = Offset(tailLeft, tailTop),
                size = Size(tailWidth, tailHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Золотистая полосочка на хвостике
            drawLine(
                color = Color(0xFFFDE047),
                start = Offset(tailLeft + 2f, tailTop + tailHeight * 0.65f),
                end = Offset(tailLeft + tailWidth - 2f, tailTop + tailHeight * 0.65f),
                strokeWidth = 4f
            )

            // Пушистые кисточки/бахрома на конце шарфика
            val fringeY = tailTop + tailHeight
            for (i in 0..4) {
                val fx = tailLeft + (tailWidth / 4f) * i
                drawLine(
                    color = Color.White,
                    start = Offset(fx, fringeY),
                    end = Offset(fx, fringeY + height * 0.035f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        PetAccessory.COLLAR -> {
            val collarBrush = Brush.horizontalGradient(listOf(Color(0xFF78350F), Color(0xFF92400E), Color(0xFF78350F)))
            val collarY = centerY + height * 0.17f
            val collarW = width * 0.52f
            val collarH = height * 0.055f

            // Кожаный ремешок
            drawRoundRect(
                brush = collarBrush,
                topLeft = Offset(centerX - collarW * 0.5f, collarY),
                size = Size(collarW, collarH),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Прострочка светлой нитью
            drawLine(Color(0xFFFDE68A).copy(alpha = 0.8f), Offset(centerX - collarW * 0.45f, collarY + 2f), Offset(centerX + collarW * 0.45f, collarY + 2f), 1.2f)
            drawLine(Color(0xFFFDE68A).copy(alpha = 0.8f), Offset(centerX - collarW * 0.45f, collarY + collarH - 2f), Offset(centerX + collarW * 0.45f, collarY + collarH - 2f), 1.2f)

            // Золотая пряжка
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(centerX - collarW * 0.28f, collarY - 1.5f),
                size = Size(width * 0.07f, collarH + 3f),
                cornerRadius = CornerRadius(2.5f, 2.5f),
                style = Stroke(width = 2.5f)
            )

            // Золотое колечко и медальон-адресник
            val ringCenter = Offset(centerX, collarY + collarH)
            drawCircle(Color(0xFFFFD700), radius = width * 0.018f, center = ringCenter, style = Stroke(width = 2f))

            val medalCenter = Offset(centerX, ringCenter.y + height * 0.045f)
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFFFDF00), Color(0xFFD97706)), medalCenter, width * 0.045f),
                radius = width * 0.045f,
                center = medalCenter
            )
            drawCircle(Color(0xFFB45309), radius = width * 0.045f, center = medalCenter, style = Stroke(width = 1.5f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = width * 0.015f, center = medalCenter)
        }

        PetAccessory.CROWN -> {
            val crownBaseY = centerY - height * 0.32f
            val crownW = width * 0.42f
            val crownLeft = centerX - crownW * 0.5f

            // Внутренняя бархатная малиновая шапочка
            val velvetPath = Path().apply {
                moveTo(crownLeft + crownW * 0.12f, crownBaseY)
                cubicTo(
                    crownLeft + crownW * 0.12f, crownBaseY - height * 0.16f,
                    crownLeft + crownW * 0.88f, crownBaseY - height * 0.16f,
                    crownLeft + crownW * 0.88f, crownBaseY
                )
                close()
            }
            drawPath(velvetPath, Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF881337)), crownBaseY - height * 0.16f, crownBaseY))

            // Золотые 5 пиков короны
            val crownPath = Path().apply {
                moveTo(crownLeft, crownBaseY)
                lineTo(crownLeft + crownW * 0.05f, crownBaseY - height * 0.14f)
                lineTo(crownLeft + crownW * 0.24f, crownBaseY - height * 0.07f)
                lineTo(crownLeft + crownW * 0.32f, crownBaseY - height * 0.18f)
                lineTo(crownLeft + crownW * 0.44f, crownBaseY - height * 0.08f)
                lineTo(crownLeft + crownW * 0.50f, crownBaseY - height * 0.22f)
                lineTo(crownLeft + crownW * 0.56f, crownBaseY - height * 0.08f)
                lineTo(crownLeft + crownW * 0.68f, crownBaseY - height * 0.18f)
                lineTo(crownLeft + crownW * 0.76f, crownBaseY - height * 0.07f)
                lineTo(crownLeft + crownW * 0.95f, crownBaseY - height * 0.14f)
                lineTo(crownLeft + crownW, crownBaseY)
                close()
            }
            drawPath(crownPath, Brush.verticalGradient(listOf(Color(0xFFFFDF00), Color(0xFFF59E0B), Color(0xFFCA8A04)), crownBaseY - height * 0.22f, crownBaseY))
            drawPath(crownPath, Color(0xFFB45309), style = Stroke(width = 1.5f))

            // Рубиновые наконечники
            val peakRubyRadius = width * 0.016f
            drawCircle(Color(0xFFDC2626), peakRubyRadius, Offset(crownLeft + crownW * 0.05f, crownBaseY - height * 0.14f))
            drawCircle(Color(0xFFDC2626), peakRubyRadius, Offset(crownLeft + crownW * 0.32f, crownBaseY - height * 0.18f))
            drawCircle(Color(0xFFDC2626), peakRubyRadius * 1.3f, Offset(crownLeft + crownW * 0.50f, crownBaseY - height * 0.22f))
            drawCircle(Color(0xFFDC2626), peakRubyRadius, Offset(crownLeft + crownW * 0.68f, crownBaseY - height * 0.18f))
            drawCircle(Color(0xFFDC2626), peakRubyRadius, Offset(crownLeft + crownW * 0.95f, crownBaseY - height * 0.14f))

            // Золотой базовый обод с камнями
            val bandH = height * 0.045f
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFFCA8A04), Color(0xFFFFD700), Color(0xFFCA8A04))),
                topLeft = Offset(crownLeft - 2f, crownBaseY - 2f),
                size = Size(crownW + 4f, bandH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawCircle(Color(0xFF2563EB), width * 0.014f, Offset(crownLeft + crownW * 0.22f, crownBaseY + bandH * 0.45f))
            drawCircle(Color(0xFFDC2626), width * 0.016f, Offset(crownLeft + crownW * 0.50f, crownBaseY + bandH * 0.45f))
            drawCircle(Color(0xFF10B981), width * 0.014f, Offset(crownLeft + crownW * 0.78f, crownBaseY + bandH * 0.45f))
        }

        PetAccessory.ROYAL_MANTLE -> {
            val mantleTopY = centerY + height * 0.14f
            val mantleBottomY = centerY + height * 0.36f
            val mantleLeft = centerX - width * 0.35f
            val mantleW = width * 0.70f

            // 1. Бархатная алая мантия
            val mantlePath = Path().apply {
                moveTo(centerX - width * 0.22f, mantleTopY)
                cubicTo(
                    mantleLeft - width * 0.05f, mantleTopY + height * 0.08f,
                    mantleLeft, mantleBottomY,
                    centerX - width * 0.15f, mantleBottomY
                )
                lineTo(centerX, mantleBottomY - height * 0.04f)
                lineTo(centerX + width * 0.15f, mantleBottomY)
                cubicTo(
                    mantleLeft + mantleW, mantleBottomY,
                    mantleLeft + mantleW + width * 0.05f, mantleTopY + height * 0.08f,
                    centerX + width * 0.22f, mantleTopY
                )
                close()
            }
            drawPath(
                mantlePath,
                Brush.radialGradient(
                    colors = listOf(Color(0xFFE11D48), Color(0xFF881337), Color(0xFF4C0519)),
                    center = Offset(centerX, mantleTopY + height * 0.10f),
                    radius = mantleW * 0.65f
                )
            )

            // 2. Горностаевая белая опушка
            val furPath = Path().apply {
                moveTo(centerX - width * 0.24f, mantleTopY)
                cubicTo(
                    centerX - width * 0.26f, mantleTopY + height * 0.08f,
                    centerX + width * 0.26f, mantleTopY + height * 0.08f,
                    centerX + width * 0.24f, mantleTopY
                )
                cubicTo(
                    centerX + width * 0.20f, mantleTopY - height * 0.03f,
                    centerX - width * 0.20f, mantleTopY - height * 0.03f,
                    centerX - width * 0.24f, mantleTopY
                )
                close()
            }
            drawPath(furPath, Color(0xFFF8FAFC))
            drawPath(furPath, Color(0xFFE2E8F0), style = Stroke(width = 1.5f))

            // Черные крапинки
            drawCircle(Color(0xFF0F172A), 2.2f, Offset(centerX - width * 0.16f, mantleTopY + height * 0.04f))
            drawCircle(Color(0xFF0F172A), 2.2f, Offset(centerX - width * 0.06f, mantleTopY + height * 0.05f))
            drawCircle(Color(0xFF0F172A), 2.2f, Offset(centerX + width * 0.06f, mantleTopY + height * 0.05f))
            drawCircle(Color(0xFF0F172A), 2.2f, Offset(centerX + width * 0.16f, mantleTopY + height * 0.04f))

            // 3. Золотая фибула с рубином
            val broochCenter = Offset(centerX, mantleTopY + height * 0.05f)
            drawCircle(Color(0xFFFFD700), radius = width * 0.035f, center = broochCenter)
            drawCircle(Color(0xFFDC2626), radius = width * 0.022f, center = broochCenter)
            drawCircle(Color.White.copy(alpha = 0.8f), radius = width * 0.008f, center = Offset(broochCenter.x - 2f, broochCenter.y - 2f))
        }

        PetAccessory.ROYAL_SCEPTER -> {
            val scepterX = centerX + width * 0.32f
            val scepterTopY = centerY - height * 0.14f
            val scepterBottomY = centerY + height * 0.34f

            // Золотой жезл
            drawLine(
                brush = Brush.verticalGradient(listOf(Color(0xFFFFDF00), Color(0xFFCA8A04))),
                start = Offset(scepterX, scepterTopY + height * 0.06f),
                end = Offset(scepterX - width * 0.03f, scepterBottomY),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
            drawLine(Color(0xFFFEF08A), Offset(scepterX - 1.5f, scepterTopY + height * 0.12f), Offset(scepterX + 1.5f, scepterTopY + height * 0.14f), 2.5f)
            drawLine(Color(0xFFFEF08A), Offset(scepterX - 2f, scepterTopY + height * 0.20f), Offset(scepterX + 1f, scepterTopY + height * 0.22f), 2.5f)

            // Золотая держава
            val orbCenter = Offset(scepterX, scepterTopY + height * 0.03f)
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFFFE082), Color(0xFFD97706)), orbCenter, width * 0.04f),
                radius = width * 0.038f,
                center = orbCenter
            )
            drawCircle(Color(0xFFB45309), radius = width * 0.038f, center = orbCenter, style = Stroke(width = 1.5f))

            // Золотой крест с рубином
            val crossY = scepterTopY - height * 0.01f
            drawLine(Color(0xFFFFD700), Offset(scepterX, crossY - 7f), Offset(scepterX, crossY + 7f), 3f)
            drawLine(Color(0xFFFFD700), Offset(scepterX - 5f, crossY - 1f), Offset(scepterX + 5f, crossY - 1f), 3f)
            drawCircle(Color(0xFFDC2626), 3f, Offset(scepterX, crossY - 1f))
            drawCircle(Color.White.copy(alpha = 0.9f), 1.2f, Offset(scepterX - 1f, crossY - 2f))
        }

        PetAccessory.VISOR -> {
            val visorTopY = centerY - height * 0.14f
            val visorH = height * 0.10f
            val visorW = width * 0.54f
            val visorLeft = centerX - visorW * 0.5f

            val visorPath = Path().apply {
                moveTo(visorLeft, visorTopY + visorH * 0.25f)
                cubicTo(
                    visorLeft + visorW * 0.25f, visorTopY - visorH * 0.15f,
                    visorLeft + visorW * 0.75f, visorTopY - visorH * 0.15f,
                    visorLeft + visorW, visorTopY + visorH * 0.25f
                )
                lineTo(visorLeft + visorW * 0.92f, visorTopY + visorH)
                cubicTo(
                    visorLeft + visorW * 0.70f, visorTopY + visorH * 0.80f,
                    visorLeft + visorW * 0.30f, visorTopY + visorH * 0.80f,
                    visorLeft + visorW * 0.08f, visorTopY + visorH
                )
                close()
            }

            drawPath(
                visorPath,
                Brush.verticalGradient(
                    colors = listOf(Color(0xCC00E5FF), Color(0xDD0284C7), Color(0xEE0F172A)),
                    startY = visorTopY,
                    endY = visorTopY + visorH
                )
            )
            drawPath(visorPath, Color(0xFF00F5FF), style = Stroke(width = 2.5f))

            // Голографический прицел
            val hudCenter = Offset(centerX - visorW * 0.18f, visorTopY + visorH * 0.45f)
            drawLine(Color.White.copy(alpha = 0.85f), Offset(hudCenter.x - 10f, hudCenter.y), Offset(hudCenter.x + 10f, hudCenter.y), 1.2f)
            drawLine(Color.White.copy(alpha = 0.85f), Offset(hudCenter.x, hudCenter.y - 6f), Offset(hudCenter.x, hudCenter.y + 6f), 1.2f)
            drawCircle(Color(0xFF38BDF8), 4f, hudCenter, style = Stroke(width = 1f))

            // График
            val chartX = centerX + visorW * 0.12f
            val chartY = visorTopY + visorH * 0.48f
            drawLine(Color(0xFF10B981), Offset(chartX, chartY + 3f), Offset(chartX + 6f, chartY - 2f), 1.8f)
            drawLine(Color(0xFF10B981), Offset(chartX + 6f, chartY - 2f), Offset(chartX + 12f, chartY - 5f), 1.8f)

            // Боковые крепления
            drawRoundRect(Color(0xFF334155), Offset(visorLeft - 3f, visorTopY + visorH * 0.20f), Size(6f, visorH * 0.60f), CornerRadius(2f, 2f))
            drawCircle(Color(0xFF10B981), 2f, Offset(visorLeft, visorTopY + visorH * 0.50f))
            drawRoundRect(Color(0xFF334155), Offset(visorLeft + visorW - 3f, visorTopY + visorH * 0.20f), Size(6f, visorH * 0.60f), CornerRadius(2f, 2f))
            drawCircle(Color(0xFF10B981), 2f, Offset(visorLeft + visorW, visorTopY + visorH * 0.50f))
        }

        PetAccessory.HEADBAND -> {
            val bandY = centerY - height * 0.20f
            val bandW = width * 0.56f
            val bandLeft = centerX - bandW * 0.5f
            val bandH = height * 0.055f

            // Повязка
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E293B))),
                topLeft = Offset(bandLeft, bandY),
                size = Size(bandW, bandH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Металлическая пластина
            val plateW = width * 0.20f
            val plateH = bandH + 2f
            val plateLeft = centerX - plateW * 0.5f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8))),
                topLeft = Offset(plateLeft, bandY - 1f),
                size = Size(plateW, plateH),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(Color(0xFF475569), Offset(plateLeft, bandY - 1f), Size(plateW, plateH), CornerRadius(3f, 3f), style = Stroke(1.2f))

            drawCircle(Color(0xFF334155), 1.2f, Offset(plateLeft + 3f, bandY + 2f))
            drawCircle(Color(0xFF334155), 1.2f, Offset(plateLeft + plateW - 3f, bandY + 2f))
            drawCircle(Color(0xFF334155), 1.2f, Offset(plateLeft + 3f, bandY + plateH - 3f))
            drawCircle(Color(0xFF334155), 1.2f, Offset(plateLeft + plateW - 3f, bandY + plateH - 3f))

            drawCircle(Color(0xFFF59E0B), radius = 4f, center = Offset(centerX, bandY + plateH * 0.5f - 1f))
            drawCircle(Color(0xFFCA8A04), radius = 4f, center = Offset(centerX, bandY + plateH * 0.5f - 1f), style = Stroke(width = 1f))

            // Развевающиеся ленты узла слева
            val ribbonTail = Path().apply {
                moveTo(bandLeft + 2f, bandY + bandH * 0.3f)
                cubicTo(
                    bandLeft - width * 0.12f, bandY + height * 0.04f,
                    bandLeft - width * 0.10f, bandY + height * 0.12f,
                    bandLeft - width * 0.16f, bandY + height * 0.18f
                )
                lineTo(bandLeft - width * 0.13f, bandY + height * 0.14f)
                cubicTo(
                    bandLeft - width * 0.08f, bandY + height * 0.08f,
                    bandLeft - width * 0.06f, bandY + height * 0.05f,
                    bandLeft + 2f, bandY + bandH * 0.8f
                )
                close()
            }
            drawPath(ribbonTail, Color(0xFF0F172A))
        }

        PetAccessory.HEADPHONES -> {
            val headTopY = centerY - height * 0.32f
            val earLeftX = centerX - width * 0.28f
            val earRightX = centerX + width * 0.28f
            val earY = centerY - height * 0.08f

            // Оголовье
            val bandPath = Path().apply {
                moveTo(earLeftX, earY - height * 0.06f)
                cubicTo(
                    earLeftX - 4f, headTopY - height * 0.06f,
                    earRightX + 4f, headTopY - height * 0.06f,
                    earRightX, earY - height * 0.06f
                )
            }
            drawPath(bandPath, Color(0xFF1E293B), style = Stroke(width = 6f, cap = StrokeCap.Round))
            drawPath(bandPath, Color(0xFF00E5FF), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

            val cupW = width * 0.09f
            val cupH = height * 0.14f
            // Левый амбушюр
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(earLeftX - cupW * 0.5f, earY - cupH * 0.5f),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(earLeftX - cupW * 0.5f, earY - cupH * 0.5f),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(earLeftX - cupW * 0.2f, earY - cupH * 0.4f),
                size = Size(cupW * 0.6f, cupH * 0.8f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Правый амбушюр
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(earRightX - cupW * 0.5f, earY - cupH * 0.5f),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFFEC4899),
                topLeft = Offset(earRightX - cupW * 0.5f, earY - cupH * 0.5f),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(earRightX - cupW * 0.4f, earY - cupH * 0.4f),
                size = Size(cupW * 0.6f, cupH * 0.8f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        PetAccessory.WIZARD_HAT -> {
            val hatBaseY = centerY - height * 0.28f
            val brimW = width * 0.56f
            val brimH = height * 0.08f
            val hatLeft = centerX - brimW * 0.5f

            // Поля шляпы
            drawOval(
                brush = Brush.radialGradient(listOf(Color(0xFF312E81), Color(0xFF1E1B4B)), Offset(centerX, hatBaseY), brimW * 0.5f),
                topLeft = Offset(hatLeft, hatBaseY - brimH * 0.5f),
                size = Size(brimW, brimH)
            )
            drawOval(Color(0xFFFACC15).copy(alpha = 0.6f), Offset(hatLeft, hatBaseY - brimH * 0.5f), Size(brimW, brimH), style = Stroke(1.5f))

            // Конический колпак
            val conePath = Path().apply {
                moveTo(centerX - width * 0.18f, hatBaseY)
                cubicTo(
                    centerX - width * 0.12f, hatBaseY - height * 0.16f,
                    centerX + width * 0.05f, hatBaseY - height * 0.22f,
                    centerX + width * 0.20f, hatBaseY - height * 0.26f
                )
                cubicTo(
                    centerX + width * 0.12f, hatBaseY - height * 0.18f,
                    centerX + width * 0.16f, hatBaseY - height * 0.10f,
                    centerX + width * 0.18f, hatBaseY
                )
                close()
            }
            drawPath(
                conePath,
                Brush.linearGradient(
                    colors = listOf(Color(0xFF4338CA), Color(0xFF1E1B4B)),
                    start = Offset(centerX - width * 0.18f, hatBaseY),
                    end = Offset(centerX + width * 0.20f, hatBaseY - height * 0.26f)
                )
            )

            // Звездочка на кончике шляпы
            val tipPos = Offset(centerX + width * 0.20f, hatBaseY - height * 0.26f)
            drawCircle(Color(0xFFFFD700), radius = width * 0.02f, center = tipPos)
            drawCircle(Color.White, radius = width * 0.009f, center = tipPos)

            // Золотая лента с пряжкой
            drawRoundRect(
                color = Color(0xFFF59E0B),
                topLeft = Offset(centerX - width * 0.19f, hatBaseY - height * 0.035f),
                size = Size(width * 0.38f, height * 0.035f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(centerX - width * 0.04f, hatBaseY - height * 0.045f),
                size = Size(width * 0.08f, height * 0.055f),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = 2.5f)
            )

            drawCircle(Color(0xFFFFE082), 2f, Offset(centerX - width * 0.08f, hatBaseY - height * 0.10f))
            drawCircle(Color(0xFFFFE082), 2.5f, Offset(centerX + width * 0.04f, hatBaseY - height * 0.15f))
        }

        PetAccessory.BADGE -> {
            val badgeCenter = Offset(centerX - width * 0.12f, centerY + height * 0.23f)
            val starR = width * 0.045f

            // Ленты ордена
            val ribbon1 = Path().apply {
                moveTo(badgeCenter.x - 6f, badgeCenter.y + 4f)
                lineTo(badgeCenter.x - 12f, badgeCenter.y + height * 0.08f)
                lineTo(badgeCenter.x - 7f, badgeCenter.y + height * 0.065f)
                lineTo(badgeCenter.x - 2f, badgeCenter.y + height * 0.08f)
                close()
            }
            drawPath(ribbon1, Color(0xFFDC2626))

            val ribbon2 = Path().apply {
                moveTo(badgeCenter.x + 2f, badgeCenter.y + 4f)
                lineTo(badgeCenter.x - 2f, badgeCenter.y + height * 0.08f)
                lineTo(badgeCenter.x + 3f, badgeCenter.y + height * 0.065f)
                lineTo(badgeCenter.x + 8f, badgeCenter.y + height * 0.08f)
                close()
            }
            drawPath(ribbon2, Color(0xFF2563EB))

            // Золотая 5-конечная звезда
            val starPath = Path().apply {
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) starR else starR * 0.45f
                    val angle = (i * 36 - 90) * Math.PI.toFloat() / 180f
                    val px = badgeCenter.x + r * kotlin.math.cos(angle)
                    val py = badgeCenter.y + r * kotlin.math.sin(angle)
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(starPath, Brush.radialGradient(listOf(Color(0xFFFFDF00), Color(0xFFF59E0B), Color(0xFFB45309)), badgeCenter, starR))
            drawPath(starPath, Color(0xFF78350F), style = Stroke(width = 1.2f))

            drawCircle(Color.White.copy(alpha = 0.9f), radius = width * 0.012f, center = Offset(badgeCenter.x - 2f, badgeCenter.y - 2f))
        }

        PetAccessory.EMERALD_GEM -> {
            val chainCenter = Offset(centerX, centerY + height * 0.17f)
            val pendantCenter = Offset(centerX, centerY + height * 0.25f)

            // Золотая цепочка
            val chainPath = Path().apply {
                moveTo(centerX - width * 0.18f, chainCenter.y)
                cubicTo(
                    centerX - width * 0.10f, pendantCenter.y + 2f,
                    centerX + width * 0.10f, pendantCenter.y + 2f,
                    centerX + width * 0.18f, chainCenter.y
                )
            }
            drawPath(chainPath, Color(0xFFFFD700), style = Stroke(width = 2.2f))

            drawCircle(Color(0xFFFFD700), radius = 3.5f, center = Offset(pendantCenter.x, pendantCenter.y - height * 0.03f))

            // Ограненный изумруд
            val gemW = width * 0.07f
            val gemH = height * 0.065f
            val gemPath = Path().apply {
                moveTo(pendantCenter.x, pendantCenter.y - gemH * 0.5f)
                lineTo(pendantCenter.x + gemW * 0.5f, pendantCenter.y)
                lineTo(pendantCenter.x, pendantCenter.y + gemH * 0.5f)
                lineTo(pendantCenter.x - gemW * 0.5f, pendantCenter.y)
                close()
            }
            drawPath(
                gemPath,
                Brush.radialGradient(
                    colors = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF047857)),
                    center = pendantCenter,
                    radius = gemW * 0.55f
                )
            )
            drawPath(gemPath, Color(0xFF064E3B), style = Stroke(width = 1.5f))

            drawLine(Color(0x80FFFFFF), Offset(pendantCenter.x, pendantCenter.y - gemH * 0.5f), Offset(pendantCenter.x, pendantCenter.y + gemH * 0.5f), 1f)
            drawLine(Color(0x80FFFFFF), Offset(pendantCenter.x - gemW * 0.5f, pendantCenter.y), Offset(pendantCenter.x + gemW * 0.5f, pendantCenter.y), 1f)
            drawCircle(Color.White, 2f, Offset(pendantCenter.x - 2f, pendantCenter.y - 2f))
        }

        PetAccessory.FLOWER_WREATH -> {
            val wreathY = centerY - height * 0.25f
            val wreathW = width * 0.54f
            val wreathLeft = centerX - wreathW * 0.5f

            // Зеленая лоза
            val vinePath = Path().apply {
                moveTo(wreathLeft, wreathY + height * 0.02f)
                cubicTo(
                    wreathLeft + wreathW * 0.25f, wreathY - height * 0.05f,
                    wreathLeft + wreathW * 0.75f, wreathY - height * 0.05f,
                    wreathLeft + wreathW, wreathY + height * 0.02f
                )
            }
            drawPath(vinePath, Color(0xFF059669), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

            drawOval(Color(0xFF10B981), Offset(centerX - wreathW * 0.32f, wreathY - height * 0.03f), Size(10f, 5f))
            drawOval(Color(0xFF10B981), Offset(centerX + wreathW * 0.22f, wreathY - height * 0.03f), Size(10f, 5f))

            // 5 цветков сакуры
            val flowerOffsets = listOf(
                Offset(centerX - wreathW * 0.36f, wreathY + height * 0.01f),
                Offset(centerX - wreathW * 0.18f, wreathY - height * 0.035f),
                Offset(centerX, wreathY - height * 0.045f),
                Offset(centerX + wreathW * 0.18f, wreathY - height * 0.035f),
                Offset(centerX + wreathW * 0.36f, wreathY + height * 0.01f)
            )

            for (pos in flowerOffsets) {
                val petalR = width * 0.022f
                for (p in 0 until 5) {
                    val angle = (p * 72) * Math.PI.toFloat() / 180f
                    val px = pos.x + petalR * kotlin.math.cos(angle)
                    val py = pos.y + petalR * kotlin.math.sin(angle)
                    drawCircle(Color(0xFFFBCFE8), radius = petalR * 0.9f, center = Offset(px, py))
                    drawCircle(Color(0xFFF472B6), radius = petalR * 0.9f, center = Offset(px, py), style = Stroke(width = 0.8f))
                }
                drawCircle(Color(0xFFFACC15), radius = petalR * 0.5f, center = pos)
            }
        }

        PetAccessory.POCKET_WATCH -> {
            val chainStartY = centerY + height * 0.18f
            val watchCenter = Offset(centerX + width * 0.14f, centerY + height * 0.27f)
            val watchR = width * 0.055f

            // Золотая цепочка
            val watchChainPath = Path().apply {
                moveTo(centerX - width * 0.10f, chainStartY)
                cubicTo(
                    centerX - width * 0.02f, watchCenter.y + height * 0.02f,
                    centerX + width * 0.08f, watchCenter.y + height * 0.02f,
                    watchCenter.x, watchCenter.y - watchR
                )
            }
            drawPath(watchChainPath, Color(0xFFFFD700), style = Stroke(width = 2f))

            // Заводная головка
            drawRect(Color(0xFFCA8A04), Offset(watchCenter.x - 2f, watchCenter.y - watchR - 4f), Size(4f, 4f))
            drawCircle(Color(0xFFFFD700), radius = 3.5f, center = Offset(watchCenter.x, watchCenter.y - watchR - 5f), style = Stroke(width = 1.5f))

            // Корпус часов
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFFFDF00), Color(0xFFD97706)), watchCenter, watchR),
                radius = watchR,
                center = watchCenter
            )
            drawCircle(Color(0xFFB45309), radius = watchR, center = watchCenter, style = Stroke(width = 1.5f))

            // Циферблат
            val dialR = watchR * 0.78f
            drawCircle(Color(0xFFFFFBEB), radius = dialR, center = watchCenter)

            // Засечки циферблата
            drawLine(Color(0xFF1E293B), Offset(watchCenter.x, watchCenter.y - dialR + 2f), Offset(watchCenter.x, watchCenter.y - dialR + 5f), 1.5f)
            drawLine(Color(0xFF1E293B), Offset(watchCenter.x, watchCenter.y + dialR - 2f), Offset(watchCenter.x, watchCenter.y + dialR - 5f), 1.5f)
            drawLine(Color(0xFF1E293B), Offset(watchCenter.x - dialR + 2f, watchCenter.y), Offset(watchCenter.x - dialR + 5f, watchCenter.y), 1.5f)
            drawLine(Color(0xFF1E293B), Offset(watchCenter.x + dialR - 2f, watchCenter.y), Offset(watchCenter.x + dialR - 5f, watchCenter.y), 1.5f)

            // Стрелки часов
            drawLine(Color(0xFF1E3A8A), watchCenter, Offset(watchCenter.x - dialR * 0.45f, watchCenter.y - dialR * 0.45f), 1.8f, StrokeCap.Round)
            drawLine(Color(0xFF1E3A8A), watchCenter, Offset(watchCenter.x + dialR * 0.60f, watchCenter.y - dialR * 0.35f), 1.2f, StrokeCap.Round)
            drawCircle(Color(0xFFDC2626), 1.5f, watchCenter)
        }

        PetAccessory.NONE -> {}
    }
}

private fun DrawScope.drawMasterCrown(centerX: Float, centerY: Float, width: Float, height: Float) {
    // Золотая сияющая корона для стадии Эксперта
    val crownPath = Path().apply {
        moveTo(centerX - width * 0.14f, centerY - height * 0.32f)
        lineTo(centerX - width * 0.16f, centerY - height * 0.44f)
        lineTo(centerX - width * 0.07f, centerY - height * 0.36f)
        lineTo(centerX, centerY - height * 0.47f)
        lineTo(centerX + width * 0.07f, centerY - height * 0.36f)
        lineTo(centerX + width * 0.16f, centerY - height * 0.44f)
        lineTo(centerX + width * 0.14f, centerY - height * 0.32f)
        close()
    }
    drawPath(crownPath, Brush.verticalGradient(listOf(Color(0xFFFFDF00), Color(0xFFD4AF37))))
    // Рубинчик по центру короны
    drawCircle(Color(0xFFE11D48), radius = width * 0.015f, center = Offset(centerX, centerY - height * 0.37f))
}

private fun DrawScope.drawTeenMedal(centerX: Float, centerY: Float, width: Float, height: Float) {
    // Звездочка на груди
    drawCircle(Color(0xFFFFD700), radius = width * 0.035f, center = Offset(centerX, centerY + height * 0.18f))
}

private fun DrawScope.drawBabySprout(centerX: Float, centerY: Float, width: Float, height: Float) {
    // Зеленый милый росточек на макушке малыша
    val sproutColor = Color(0xFF10B981)
    drawLine(sproutColor, Offset(centerX, centerY - height * 0.30f), Offset(centerX, centerY - height * 0.38f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawOval(
        color = sproutColor,
        topLeft = Offset(centerX - width * 0.04f, centerY - height * 0.41f),
        size = Size(width * 0.05f, height * 0.03f)
    )
}

/**
 * Отрисовка стильной полосатой кофточки и наплечной сумочки (вдохновлено 3D-референсом)
 */
private fun DrawScope.drawStripedSweaterAndBag(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    breathScale: Float
) {
    val sweaterTopY = centerY + height * 0.11f
    val sweaterBottomY = centerY + height * 0.31f * breathScale
    val sweaterRadiusX = width * 0.28f

    // Основа кофточки (чередующиеся полосы: стильный темно-серый и кремово-белый)
    val darkStripe = Color(0xFF334155)
    val lightStripe = Color(0xFFF1F5F9)

    val stripesCount = 4
    val stripeHeight = (sweaterBottomY - sweaterTopY) / stripesCount
    for (i in 0 until stripesCount) {
        val y = sweaterTopY + i * stripeHeight
        val col = if (i % 2 == 0) darkStripe else lightStripe
        val arcScale = 1f - (i * 0.03f)
        drawOval(
            color = col,
            topLeft = Offset(centerX - sweaterRadiusX * arcScale, y),
            size = Size(sweaterRadiusX * 2f * arcScale, stripeHeight * 1.5f)
        )
    }

    // Белый круглый воротничок кофточки
    drawOval(
        color = Color.White,
        topLeft = Offset(centerX - width * 0.12f, sweaterTopY - height * 0.015f),
        size = Size(width * 0.24f, height * 0.045f)
    )

    // Стильный темный ремешок через плечо (crossbody strap)
    val strapColor = Color(0xFF1E293B)
    drawLine(
        color = strapColor,
        start = Offset(centerX - width * 0.20f, sweaterTopY - height * 0.01f),
        end = Offset(centerX + width * 0.18f, sweaterBottomY - height * 0.02f),
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )

    // Маленькая аккуратная наплечная сумочка справа
    val bagCenter = Offset(centerX + width * 0.18f, sweaterBottomY - height * 0.02f)
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(bagCenter.x - width * 0.045f, bagCenter.y - height * 0.035f),
        size = Size(width * 0.09f, height * 0.065f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Золотистая застежка сумочки
    drawCircle(
        color = Color(0xFFFFD700),
        radius = width * 0.012f,
        center = Offset(bagCenter.x, bagCenter.y - height * 0.005f)
    )
}

/**
 * Процедурные пятна пыли и грязи на мордочке и теле питомца при падении показателя заботы (care < 0.65).
 * Смываются при уходе и гигиенических процедурах (мыло, расческа, ванна).
 */
private fun DrawScope.drawDirtPatches(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    care: Float
) {
    val dirtAlpha = ((0.75f - care) / 0.75f).coerceIn(0.40f, 0.85f)
    val dirtColor = Color(0xFF5D4037).copy(alpha = dirtAlpha)
    val smudgeColor = Color(0xFF4E342E).copy(alpha = dirtAlpha * 0.7f)

    // 1. Пятнышко грязи на левой щечке
    val pathLeft = Path().apply {
        moveTo(centerX - width * 0.22f, centerY + height * 0.08f)
        quadraticBezierTo(centerX - width * 0.18f, centerY + height * 0.05f, centerX - width * 0.14f, centerY + height * 0.07f)
        quadraticBezierTo(centerX - width * 0.12f, centerY + height * 0.11f, centerX - width * 0.16f, centerY + height * 0.13f)
        quadraticBezierTo(centerX - width * 0.24f, centerY + height * 0.14f, centerX - width * 0.22f, centerY + height * 0.08f)
        close()
    }
    drawPath(
        path = pathLeft,
        brush = Brush.radialGradient(
            colors = listOf(dirtColor, smudgeColor),
            center = Offset(centerX - width * 0.18f, centerY + height * 0.09f),
            radius = width * 0.07f
        )
    )

    // 2. Пятнышко пыли на правой стороне лобика
    val pathForehead = Path().apply {
        moveTo(centerX + width * 0.12f, centerY - height * 0.12f)
        quadraticBezierTo(centerX + width * 0.18f, centerY - height * 0.15f, centerX + width * 0.22f, centerY - height * 0.11f)
        quadraticBezierTo(centerX + width * 0.24f, centerY - height * 0.07f, centerX + width * 0.16f, centerY - height * 0.08f)
        close()
    }
    drawPath(
        path = pathForehead,
        brush = Brush.radialGradient(
            colors = listOf(dirtColor, smudgeColor),
            center = Offset(centerX + width * 0.17f, centerY - height * 0.10f),
            radius = width * 0.06f
        )
    )

    // 3. Пятнышко на животике/лапке
    drawOval(
        color = smudgeColor,
        topLeft = Offset(centerX + width * 0.04f, centerY + height * 0.15f),
        size = Size(width * 0.08f, height * 0.05f)
    )

    // Мелкие крапинки пыли
    drawCircle(dirtColor, width * 0.012f, Offset(centerX - width * 0.25f, centerY + height * 0.06f))
    drawCircle(dirtColor, width * 0.010f, Offset(centerX - width * 0.11f, centerY + height * 0.13f))
    drawCircle(dirtColor, width * 0.011f, Offset(centerX + width * 0.25f, centerY - height * 0.09f))
    drawCircle(dirtColor, width * 0.009f, Offset(centerX + width * 0.09f, centerY - height * 0.16f))
}

