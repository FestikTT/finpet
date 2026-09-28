package ru.finpet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import ru.finpet.app.model.ShopItem
import ru.finpet.app.ui.theme.*

/**
 * Анимация экспресс-доставки из открывающейся двери комнаты:
 * 1. Дверь распахивается с золотым свечением из прихожей.
 * 2. Живая покачивающаяся посылка 📦 выезжает вперед с пружинной физикой.
 * 3. Крышка коробки распахивается с вращающимися лучами сияния ✨.
 * 4. Купленный предмет взмывает вверх с аурой и попадает в инвентарь!
 */
@Composable
fun DeliveryUnboxingDialog(
    item: ShopItem,
    onDismiss: () -> Unit,
    onGoToRoom: (() -> Unit)? = null
) {
    val currentTheme = LocalAppTheme.current
    val coroutineScope = rememberCoroutineScope()
    var isUnboxed by remember { mutableStateOf(false) }

    val boxDropY = remember { Animatable(-80f) }
    val boxScale = remember { Animatable(0.6f) }
    val itemScale = remember { Animatable(0.5f) }
    val itemOffsetY = remember { Animatable(0f) }
    val sparksAlpha = remember { Animatable(0f) }

    // 1. Падение коробки сверху с отскоком (-80 dp -> 28 dp) и тяжелый виброотклик при ударе о пол
    LaunchedEffect(Unit) {
        launch {
            boxScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        boxDropY.animateTo(
            targetValue = 28f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        // Тяжелый тактильный щелчок при ударе о пол
        SoundHapticManager.performHeavyClickHaptic()
    }

    // 2. Покачивание коробки из стороны в сторону (-8°..+8°)
    val infiniteTransition = rememberInfiniteTransition(label = "unboxingInfinite")
    val wobbleAngle by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    // Вращение золотых лучей (God rays)
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays"
    )

    val unboxAction: () -> Unit = {
        if (!isUnboxed) {
            isUnboxed = true
            coroutineScope.launch {
                SoundHapticManager.performSuccessHaptic()
                launch {
                    sparksAlpha.snapTo(1f)
                    delay(1200)
                    sparksAlpha.animateTo(0f, tween(500))
                }
                launch {
                    itemOffsetY.animateTo(-24f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                }
                // Масштабирование от 0.5 до 1.2 с затуханием до 1.0
                itemScale.snapTo(0.5f)
                itemScale.animateTo(1.2f, tween(240, easing = FastOutSlowInEasing))
                itemScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceLight,
            border = BorderStroke(2.dp, currentTheme.borderColor),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Заголовок доставки
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = currentTheme.bubbleBg,
                        border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "🚪 ЭКСПРЕСС-ДОСТАВКА В КОМНАТУ",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = currentTheme.primaryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Посылка прибыла!",
                        fontFamily = UnboundedFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                // Интерактивная сцена: дверь + посылка + вылетающий предмет
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Золотое свечение из открытой двери
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFE082).copy(alpha = 0.85f), Color(0xFFFFB74D).copy(alpha = 0.4f))
                                )
                            )
                    )

                    // Картонная покачивающаяся коробка посылки (падает сверху -80dp -> 28dp, покачивается -8°..+8°)
                    if (!isUnboxed) {
                        Box(
                            modifier = Modifier
                                .offset(y = boxDropY.value.dp)
                                .scale(boxScale.value)
                                .graphicsLayer {
                                    rotationZ = wobbleAngle
                                }
                                .bounceClick { unboxAction() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 68.sp)
                        }
                    }

                    // Вращающиеся лучи сияния (God rays) позади открытого предмета
                    if (isUnboxed) {
                        Canvas(
                            modifier = Modifier
                                .size(160.dp)
                                .offset(y = itemOffsetY.value.dp)
                                .graphicsLayer { rotationZ = rayRotation }
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val rayCount = 12
                            val rayRadius = size.width / 2f
                            val angleStep = (2 * Math.PI / rayCount).toFloat()
                            for (i in 0 until rayCount) {
                                val angle = i * angleStep
                                val path = Path().apply {
                                    moveTo(center.x, center.y)
                                    lineTo(
                                        center.x + rayRadius * kotlin.math.cos(angle - 0.12f).toFloat(),
                                        center.y + rayRadius * kotlin.math.sin(angle - 0.12f).toFloat()
                                    )
                                    lineTo(
                                        center.x + rayRadius * kotlin.math.cos(angle + 0.12f).toFloat(),
                                        center.y + rayRadius * kotlin.math.sin(angle + 0.12f).toFloat()
                                    )
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFFFFD54F).copy(alpha = if (i % 2 == 0) 0.35f else 0.15f)
                                )
                            }
                        }

                        // Выплывающий предмет с масштабированием от 0.5 до 1.2 с затуханием до 1.0
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .offset(y = itemOffsetY.value.dp)
                                .scale(itemScale.value)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.primaryColor.copy(alpha = 0.25f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.icon, fontSize = 44.sp)
                            }
                        }

                        // Искры и конфетти вокруг предмета
                        if (sparksAlpha.value > 0f) {
                            Text(
                                "✨",
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 30.dp, top = 20.dp)
                                    .graphicsLayer { alpha = sparksAlpha.value }
                            )
                            Text(
                                "🎉",
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 35.dp, top = 25.dp)
                                    .graphicsLayer { alpha = sparksAlpha.value }
                            )
                        }
                    }
                }

                // Описание предмета
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isUnboxed) item.name else "Твоя посылка ждёт открытия!",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (!isUnboxed) {
                            "Нажми на кнопку ниже или коснись посылки, чтобы распаковать её!"
                        } else if (item.roomSlotType != null) {
                            "Предмет успешно доставлен и готов к обустройству комнаты!"
                        } else {
                            "Товар отправлен в твой инвентарь заботы о питомце!"
                        },
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }

                // Мультяшные кнопки
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isUnboxed) {
                        CartoonButton(
                            text = "Открыть посылку 🎁",
                            onClick = unboxAction,
                            containerColor = currentTheme.primaryColor,
                            height = 48.dp,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        if (onGoToRoom != null) {
                            CartoonButton(
                                text = "Перейти в комнату 🚪",
                                onClick = {
                                    onDismiss()
                                    onGoToRoom()
                                },
                                containerColor = currentTheme.primaryColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        CartoonButton(
                            text = if (onGoToRoom != null) "Остаться в магазине 🛍️" else "Отлично ✨",
                            onClick = onDismiss,
                            containerColor = if (onGoToRoom != null) Color(0xFF3F3F46) else currentTheme.primaryColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
