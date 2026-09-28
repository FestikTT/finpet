package ru.finpet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.model.GamePeriodRepository
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.CoinBadge
import ru.finpet.app.ui.components.FlyingCoinArcOverlay
import ru.finpet.app.ui.components.FullscreenConfettiCelebration
import ru.finpet.app.ui.components.PetAvatarView
import ru.finpet.app.ui.components.PetRoomSceneView
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*

@Suppress("UNUSED_PARAMETER")
@Composable
fun PetMainScreen(
    state: GameState,
    onNavigateToBudget: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToQuests: () -> Unit,
    onNavigateToGlossary: () -> Unit,
    onNavigateToMiniGame: () -> Unit = {},
    onNavigateToFinanceLab: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {},
    onPlayClick: () -> Unit,
    onSleepClick: () -> Unit,
    onAdvancePeriod: () -> Unit,
    onHelpClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentTheme = LocalAppTheme.current
    val pet = state.pet
    val currentPeriodInfo = GamePeriodRepository.getPeriod(state.currentPeriod)
    val activeGoal = state.activeGoal
    val coroutineScope = rememberCoroutineScope()

    var isContributingCoins by remember { mutableStateOf(false) }
    var showConfettiCelebration by remember { mutableStateOf(false) }
    val progressBarScale = remember { Animatable(1.0f) }
    val trophyWobbleAngle = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 0. Заголовок комнаты в самом верху
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Комната ${pet.name.ifBlank { "питомца" }}",
                    fontFamily = UnboundedFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // 1. Верхняя панель статуса
            Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Чип текущего периода (без лишних кликов на правила)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = currentTheme.primaryColor
            ) {
                Text(
                    text = "Период ${state.currentPeriod} из 5",
                    fontFamily = UnboundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            if (activeGoal != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.clickable { onNavigateToGoals() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${activeGoal.currentAmount}/${activeGoal.targetAmount} монет",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = currentTheme.primaryColor,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.clickable { onNavigateToGoals() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Выбрать цель",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Интерактивная сцена комнаты с питомцем (Аудит п.13: комната встроена на главный экран, ~40-45% фокуса)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceLight,
            border = BorderStroke(1.5.dp, currentTheme.cardOutlineLight),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Шапка карточки комнаты: Имя, Уровень и кнопка входа в комнату для кастомизации
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = pet.name,
                            fontFamily = UnboundedFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pet.type.title} • Уровень ${pet.level}",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = currentTheme.primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier.bounceClick { onNavigateToRoom() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Rounded.MeetingRoom,
                                contentDescription = "Комната питомца",
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Комната 🏠",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = currentTheme.primaryColor
                            )
                        }
                    }
                }

                // 2D сцена комнаты с живым питомцем внутри (увеличенная высота 340.dp)
                PetRoomSceneView(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    cornerRadius = 16f,
                    showDayNightBadge = true
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        PetAvatarView(
                            pet = pet,
                            modifier = Modifier.size(150.dp),
                            onPetClick = onPlayClick
                        )
                    }
                }

                // Состояние и совет от питомца
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceSubtle,
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = pet.moodExplanation,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Панель быстрых действий с питомцем: В комнату 🏠, Погладить 🐾 и Помыть 🧼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CartoonButton(
                        text = "Комната 🏠",
                        icon = Icons.Rounded.MeetingRoom,
                        onClick = onNavigateToRoom,
                        containerColor = currentTheme.primaryColor,
                        height = 42.dp,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CartoonButton(
                        text = "Погладить 🐾",
                        onClick = onPlayClick,
                        containerColor = StatGreenEmerald,
                        height = 42.dp,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (pet.isDirty || pet.care < 0.70f) {
                        CartoonButton(
                            text = "Помыть 🧼",
                            onClick = {
                                SoundHapticManager.performSuccessHaptic()
                                GameRepository.showerPetQuick()
                            },
                            containerColor = Color(0xFF0EA5E9),
                            height = 42.dp,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Текущая финансовая цель с прямым переходом к редактированию цели
        if (activeGoal != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGoals() },
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineLight)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ФИНАНСОВАЯ ЦЕЛЬ",
                            fontFamily = UnboundedFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(ru.finpet.app.R.drawable.ic_coin_vector),
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${activeGoal.currentAmount} / ${activeGoal.targetAmount}",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = currentTheme.primaryColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            IconButton(
                                onClick = onNavigateToGoals,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = "Изменить цель",
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = trophyWobbleAngle.value }
                        )
                        Text(
                            text = activeGoal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val animGoalProgress by animateFloatAsState(
                        targetValue = activeGoal.progress,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        label = "goalProgress"
                    )

                    LinearProgressIndicator(
                        progress = { animGoalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .graphicsLayer {
                                scaleX = progressBarScale.value
                                scaleY = progressBarScale.value
                            }
                            .clip(RoundedCornerShape(4.dp)),
                        color = currentTheme.primaryColor,
                        trackColor = SurfaceVariantLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (activeGoal.isAchieved) "Цель достигнута!" else "Осталось: ${activeGoal.remainingAmount}",
                                fontSize = 11.5.sp,
                                color = if (activeGoal.isAchieved) StatGreenEmerald else TextSecondary,
                                fontWeight = if (activeGoal.isAchieved) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (!activeGoal.isAchieved) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(ru.finpet.app.R.drawable.ic_coin_vector),
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        if (!activeGoal.isAchieved) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = currentTheme.bubbleBg,
                                border = BorderStroke(1.dp, currentTheme.borderColor),
                                modifier = Modifier.bounceClick(enabled = !isContributingCoins) {
                                    if (state.totalCoins <= 0) {
                                        SoundHapticManager.performErrorHaptic()
                                        android.widget.Toast.makeText(context, "Недостаточно монет для пополнения цели", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        isContributingCoins = true
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(ru.finpet.app.R.drawable.ic_coin_vector),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "В копилку +10 →",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentTheme.primaryColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatGreenEmerald.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, StatGreenEmerald),
                                modifier = Modifier.bounceClick { showConfettiCelebration = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = StatGreenEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Праздновать",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatGreenEmerald,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Актуальный квест
        val activeQuest = state.quests.find { it.orderIndex == state.nextActiveQuestOrder }
            ?: state.quests.firstOrNull { !it.isCompleted }
            ?: state.quests.lastOrNull()

        if (activeQuest != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onNavigateToQuests() },
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineLight)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = currentTheme.bubbleBg,
                        border = BorderStroke(1.dp, currentTheme.borderColor),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.School,
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Квест #${activeQuest.orderIndex} из 54",
                                fontFamily = UnboundedFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.primaryColor
                            )
                            if (state.isDailyLimitReached) {
                                Surface(
                                    shape = RoundedCornerShape(1.dp),
                                    color = StatGreenEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatGreenEmerald,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (state.questStreakDays > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = currentTheme.bubbleBg,
                                    border = BorderStroke(1.dp, currentTheme.borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text("🔥", fontSize = 11.sp)
                                        Text(
                                            text = "${state.questStreakDays} дн.",
                                            fontFamily = UnboundedFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentTheme.primaryColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = activeQuest.title,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentTheme.primaryColor
                    ) {
                        Text(
                            text = if (activeQuest.isCompleted) "Обзор" else "Решить",
                            fontFamily = UnboundedFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 5. Завершение периода
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceLight,
            border = BorderStroke(1.dp, OutlineLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Период ${state.currentPeriod} из 5",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = currentPeriodInfo.title,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                CartoonButton(
                    text = "Следующий",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onAdvancePeriod,
                    containerColor = currentTheme.primaryColor,
                    height = 40.dp,
                    fontSize = 11.sp,
                    depth = 3.dp,
                    modifier = Modifier.wrapContentWidth().defaultMinSize(minWidth = 110.dp)
                )
            }
        }
    }

    // Анимированный пролет золотых монеток в прогресс-бар копилки
    FlyingCoinArcOverlay(
            isFlying = isContributingCoins,
            onCoinLanded = {
                coroutineScope.launch {
                    SoundHapticManager.performTickHaptic()
                    progressBarScale.animateTo(1.08f, tween(60))
                    progressBarScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            },
            onAllLanded = {
                isContributingCoins = false
                val goal = activeGoal
                if (goal != null) {
                    val deposit = minOf(10, state.totalCoins, goal.remainingAmount)
                    if (deposit > 0) {
                        GameRepository.depositToActiveGoal(deposit)
                        SoundHapticManager.performHeavyClickHaptic()
                        if (goal.currentAmount + deposit >= goal.targetAmount) {
                            showConfettiCelebration = true
                            coroutineScope.launch {
                                trophyWobbleAngle.animateTo(-15f, tween(60))
                                trophyWobbleAngle.animateTo(15f, tween(60))
                                trophyWobbleAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    }
                }
            }
        )

        // Полноэкранный взрыв праздничного конфетти при 100% достижении цели
        if (showConfettiCelebration && activeGoal != null) {
            FullscreenConfettiCelebration(
                title = "Цель «${activeGoal.title}» достигнута! 🏆",
                subtitle = "Ты накопил ${activeGoal.targetAmount} монет! Гордимся твоей финансовой дисциплиной!",
                onDismiss = { showConfettiCelebration = false }
            )
        }
    }
}
