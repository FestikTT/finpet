package ru.finpet.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.app.NavScreen
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.model.Pet
import ru.finpet.app.ui.theme.*

/**
 * 5 ключевых экранов приложения в интерактивном туре
 */
enum class TutorialStep(
    val stepIndex: Int,
    val totalSteps: Int = 5,
    val tab: NavScreen,
    val title: String,
    val emoji: String,
    val speechBubble: String,
    val nextButtonText: String
) {
    PET(
        1,
        5,
        NavScreen.PET,
        "Главная и питомец",
        "🐾",
        "Это твой верный спутник! Корми его, гладь и обустраивай комнату 🏠. Довольный питомец приносит больше бонусов!",
        "Дальше: Бюджет ➔"
    ),
    BUDGET(
        2,
        5,
        NavScreen.BUDGET,
        "Бюджет: 50 / 30 / 20",
        "💰",
        "Твой бюджет по правилу 3 конвертов: 50% Обязательное 🥣, 30% Хотелки 🎮 и 20% Копилка на мечту 🚀",
        "Дальше: Квесты ➔"
    ),
    QUESTS(
        3,
        5,
        NavScreen.QUESTS,
        "Квесты и Антифрод",
        "🎓",
        "Зарабатывай монеты 🪙! Решай ежедневные квесты из жизни и учись выявлять обман в Антифрод-симуляторе 🛡️",
        "Дальше: Магазин ➔"
    ),
    SHOP(
        4,
        5,
        NavScreen.SHOP,
        "Магазин и склад",
        "🏪",
        "Трать монеты разумно! Покупай лакомства, выбирай мебель и обои для комнаты, а всё купленное храни на складе ✨",
        "Дальше: Профиль ➔"
    ),
    PROFILE(
        5,
        5,
        NavScreen.PROFILE,
        "Профиль и Родители",
        "⭐",
        "Твой прогресс! Следи за Фин-рейтингом, меняй цветовую тему и выполняй задания от родителей за награду 👨‍👩‍👧",
        "Всё понятно, погнали! 🚀"
    )
}

/**
 * Ненавязчивый плавающий гид Финни прямо над панелью навигации.
 * Никакого затемнения экрана — экран виден и интерактивен на 100%!
 */
@Composable
fun SpotlightTutorialOverlay(
    currentStep: TutorialStep?,
    pet: Pet,
    onStepCompleted: (TutorialStep) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentStep == null) return

    val coroutineScope = rememberCoroutineScope()
    val currentTheme = LocalAppTheme.current
    val primaryColor = currentTheme.primaryColor

    // Возможность свернуть гид в компактную полоску для полного обзора экрана
    var isMinimized by remember { mutableStateOf(false) }

    // Анимации реакций маскота Финни
    val petJumpAnim = remember { Animatable(0f) }
    val petPawWave = remember { Animatable(0f) }
    val petNodAnim = remember { Animatable(0f) }
    val petSpinRotation = remember { Animatable(0f) }
    var isCelebratingClick by remember { mutableStateOf(false) }

    LaunchedEffect(currentStep) {
        isCelebratingClick = false
        petSpinRotation.snapTo(0f)

        launch {
            petJumpAnim.snapTo(0f)
            petJumpAnim.animateTo(-14f, tween(150, easing = FastOutSlowInEasing))
            petJumpAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        launch {
            repeat(2) {
                petPawWave.animateTo(12f, tween(100, easing = LinearEasing))
                petPawWave.animateTo(-12f, tween(100, easing = LinearEasing))
            }
            petPawWave.animateTo(0f, tween(70))
        }

        delay(180)
        launch {
            petNodAnim.animateTo(8f, tween(120, easing = FastOutSlowInEasing))
            petNodAnim.animateTo(-2f, tween(100, easing = FastOutSlowInEasing))
            petNodAnim.animateTo(0f, tween(80))
        }
    }

    val onAdvance = {
        if (!isCelebratingClick) {
            isCelebratingClick = true
            SoundHapticManager.performSuccessHaptic()
            onStepCompleted(currentStep)
            coroutineScope.launch {
                petSpinRotation.animateTo(360f, tween(260, easing = FastOutSlowInEasing))
                isCelebratingClick = false
            }
        }
    }

    // ВАЖНО: Box прозрачный, без затемняющего фона.
    // Сквозь него виден весь экран: питомец, кнопки, графики, меню.
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.padding(bottom = 86.dp, start = 12.dp, end = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = SurfaceLight,
                border = BorderStroke(1.5.dp, primaryColor),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                if (isMinimized) {
                    // Компактный режим: полоска внизу экрана
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMinimized = false }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${currentStep.emoji} Финни: ${currentStep.title} (${currentStep.stepIndex}/5)",
                            fontFamily = UnboundedFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Развернуть ▴",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                            IconButton(
                                onClick = {
                                    SoundHapticManager.performClickHaptic()
                                    onSkip()
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Закрыть",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Развернутый виджет-гид
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        // Верхняя строка: бейдж шага и кнопки свернуть / закрыть
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = primaryColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${currentStep.emoji} Шаг ${currentStep.stepIndex} из 5 • ${currentStep.title}",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = { isMinimized = true },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Свернуть",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        SoundHapticManager.performClickHaptic()
                                        onSkip()
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Пропустить",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Маскот Финни + понятное объяснение экрана
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .graphicsLayer {
                                        translationY = petJumpAnim.value
                                        rotationZ = petPawWave.value
                                        rotationY = petSpinRotation.value
                                        rotationX = petNodAnim.value
                                    }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = currentTheme.bubbleBg,
                                    border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        PetAvatarView(
                                            pet = pet,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceSubtle,
                                border = BorderStroke(1.dp, OutlineLight),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = currentStep.speechBubble,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
                                )
                            }
                        }

                        // Нижняя строка: точки шагов, кнопка пропустить и переход вперед
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 5 индикаторов прогресса
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..currentStep.totalSteps) {
                                    val isActive = i == currentStep.stepIndex
                                    val isPast = i < currentStep.stepIndex
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(if (isActive) 16.dp else 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                when {
                                                    isActive -> primaryColor
                                                    isPast -> primaryColor.copy(alpha = 0.45f)
                                                    else -> OutlineLight.copy(alpha = 0.4f)
                                                }
                                            )
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Пропустить",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable {
                                            SoundHapticManager.performClickHaptic()
                                            onSkip()
                                        }
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                )

                                Button(
                                    onClick = { onAdvance() },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(35.dp)
                                ) {
                                    Text(
                                        text = currentStep.nextButtonText,
                                        fontFamily = UnboundedFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
