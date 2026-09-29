package ru.finpet.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.finpet.app.R
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameState
import ru.finpet.app.model.*
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.CoinBadge
import ru.finpet.app.ui.components.PetAvatarView
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.components.shake
import ru.finpet.app.ui.theme.*
import kotlin.math.sin
import java.util.Random

/**
 * Биомы глав саги квестов:
 */
data class ChapterBiome(
    val unitIndex: Int,
    val name: String,
    val emoji: String,
    val subtitle: String,
    val gradientColors: List<Color>,
    val accentColor: Color
)

val CHAPTER_BIOMES = listOf(
    ChapterBiome(1, "Зеленый луг", "🌱", "Основы бюджета и карманных денег", listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), Color(0xFF16A34A)),
    ChapterBiome(2, "Банковский сейф", "🏦", "Банковские карты, кешбэк и вклады", listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), Color(0xFFD97706)),
    ChapterBiome(3, "Торговый квартал", "🛒", "Умный шопинг и маркетинг", listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)), Color(0xFFEA580C)),
    ChapterBiome(4, "Кибер-крепость", "🛡️", "Кибербезопасность и защита от мошенников", listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE)), Color(0xFF7C3AED)),
    ChapterBiome(5, "Космодром роста", "🚀", "Инвестиции, стартапы и сложный процент", listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE)), Color(0xFF4F46E5)),
    ChapterBiome(6, "Семейный очаг", "🏠", "Семейный бюджет, ЖКХ и ресурсы", listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8)), Color(0xFFDB2777))
)

@Composable
fun QuestsScreen(
    state: GameState,
    onAnswerQuest: (questId: String, optionIndex: Int) -> Unit
) {
    var activeModalQuest by remember { mutableStateOf<FinancialQuest?>(null) }
    var reviewModalQuest by remember { mutableStateOf<FinancialQuest?>(null) }
    var lockToastMessage by remember { mutableStateOf<String?>(null) }
    var completedRewardCoins by remember { mutableStateOf<Int?>(null) }

    val units = remember { QuestTopic.values().toList() }
    val activeQuest = remember(state.quests, state.nextActiveQuestOrder) {
        state.quests.find { it.orderIndex == state.nextActiveQuestOrder }
            ?: state.quests.firstOrNull { !it.isCompleted }
            ?: state.quests.lastOrNull()
    }
    var selectedUnitIndex by remember { mutableIntStateOf(activeQuest?.topic?.unitIndex ?: 1) }

    LaunchedEffect(state.currentPeriod, activeQuest?.topic?.unitIndex) {
        val targetUnit = activeQuest?.topic?.unitIndex ?: state.currentPeriod.coerceIn(1, 6)
        selectedUnitIndex = targetUnit
    }

    val selectedUnit = remember(selectedUnitIndex) {
        units.find { it.unitIndex == selectedUnitIndex } ?: units.first()
    }

    val currentBiome = remember(selectedUnitIndex) {
        CHAPTER_BIOMES.find { it.unitIndex == selectedUnitIndex } ?: CHAPTER_BIOMES.first()
    }

    val currentUnitQuests = remember(selectedUnitIndex, state.quests) {
        state.quests.filter { it.topic.unitIndex == selectedUnitIndex }.sortedBy { it.orderIndex }
    }

    val unitCompletedCount = currentUnitQuests.count { it.isCompleted }
    val unitTotalCount = currentUnitQuests.size

    val listState = rememberLazyListState()

    // Плавный автоскролл к активному заданию
    LaunchedEffect(selectedUnitIndex, currentUnitQuests) {
        val activeIndex = currentUnitQuests.indexOfFirst { !it.isCompleted }
        val targetIndex = if (activeIndex >= 0) activeIndex else (currentUnitQuests.size - 1).coerceAtLeast(0)
        if (targetIndex in currentUnitQuests.indices) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        if (activeModalQuest != null) {
            DuolingoQuestLessonScreen(
                quest = activeModalQuest!!,
                pet = state.pet,
                onDismiss = { activeModalQuest = null },
                onComplete = { optIdx, earnedCoins ->
                    onAnswerQuest(activeModalQuest!!.id, optIdx)
                    activeModalQuest = null
                    if (earnedCoins > 0) {
                        completedRewardCoins = earnedCoins
                    }
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // =========================================================
                // 1. Верхняя панель: Номер главы, название биома и прогресс
                // =========================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = currentBiome.accentColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.5.dp, currentBiome.accentColor.copy(alpha = 0.4f)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentBiome.emoji,
                                        fontSize = 24.sp
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = currentBiome.accentColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "ГЛАВА $selectedUnitIndex: ${currentBiome.name.uppercase()}",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = currentBiome.accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = selectedUnit.unitTitle.substringAfter(": ").trim(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    lineHeight = 15.sp,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Прогресс главы
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = currentBiome.accentColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, currentBiome.accentColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "$unitCompletedCount / $unitTotalCount",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = currentBiome.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Переключатель глав (1..6)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CHAPTER_BIOMES.forEach { biome ->
                            val isSelected = biome.unitIndex == selectedUnitIndex
                            val isUnitFinished = state.quests.filter { it.topic.unitIndex == biome.unitIndex }.all { it.isCompleted }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) biome.accentColor else SurfaceLight,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) biome.accentColor else OutlineLight
                                ),
                                modifier = Modifier.bounceClick {
                                    SoundHapticManager.performClickHaptic()
                                    selectedUnitIndex = biome.unitIndex
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (isUnitFinished) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else StatGreenEmerald,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = "${biome.emoji} ${biome.name}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    // Плашка энергии питомца для прохождения квестов
                    val petEnergy = state.pet.energy.coerceIn(0f, 1f)
                    val petEnergyPercent = (petEnergy * 100).toInt()
                    val energyColor = when {
                        petEnergy >= 0.6f -> StatGreenEmerald
                        petEnergy >= 0.3f -> Color(0xFFF59E0B)
                        else -> DangerRedColor
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = energyColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, energyColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚡", fontSize = 16.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Энергия питомца",
                                        fontFamily = UnboundedFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$petEnergyPercent%",
                                        fontFamily = UnboundedFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = energyColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { petEnergy },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = energyColor,
                                    trackColor = OutlineLight.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                // =========================================================
                // 2. Вертикальная серпантинная Сага-карта (LazyColumn)
                // =========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(currentUnitQuests, key = { _, q -> q.id }) { index, quest ->
                            val isCompleted = quest.isCompleted
                            val isUnlocked = quest.orderIndex == 1 || state.quests.find { it.orderIndex == quest.orderIndex - 1 }?.isCompleted == true
                            val isCurrent = !isCompleted && isUnlocked

                            // Серпантинное смещение по синусоиде: Центр -> Право -> Центр -> Лево
                            val serpentineX = when (index % 4) {
                                0 -> 0f
                                1 -> 65f
                                2 -> 0f
                                else -> -65f
                            }

                            val nextSerpentineX = if (index < currentUnitQuests.size - 1) {
                                when ((index + 1) % 4) {
                                    0 -> 0f
                                    1 -> 65f
                                    2 -> 0f
                                    else -> -65f
                                }
                            } else 0f

                            SagaVerticalNode(
                                quest = quest,
                                isCompleted = isCompleted,
                                isUnlocked = isUnlocked,
                                isCurrent = isCurrent,
                                pet = state.pet,
                                xOffsetDp = serpentineX,
                                nextXOffsetDp = nextSerpentineX,
                                hasNext = index < currentUnitQuests.size - 1,
                                accentColor = currentBiome.accentColor,
                                sideQuestIndex = if ((index + 1) % 3 == 0 && index < currentUnitQuests.size - 1) quest.orderIndex else null,
                                onSideQuestClick = {
                                    SoundHapticManager.performClickHaptic()
                                    ru.finpet.app.data.GameRepository.awardParentCoins(15, "Фин-блиц за выполнение 3 заданий подряд!")
                                    completedRewardCoins = 15
                                },
                                onClick = {
                                    if (isCompleted) {
                                        reviewModalQuest = quest
                                    } else if (!isUnlocked) {
                                        SoundHapticManager.performLockHaptic()
                                        lockToastMessage = "Задание откроется после выполнения задания #${quest.orderIndex - 1}"
                                    } else {
                                        SoundHapticManager.performHeavyClickHaptic()
                                        activeModalQuest = quest
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Всплывающее уведомление о заблокированном задании
            lockToastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2200)
                    lockToastMessage = null
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, OutlineLight),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = msg,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Праздничная анимация начисления монет
        completedRewardCoins?.let { coins ->
            CoinFlyCelebration(
                rewardCoins = coins,
                onDismiss = { completedRewardCoins = null }
            )
        }

        // Модальное окно повторного просмотра решенного квеста
        reviewModalQuest?.let { quest ->
            QuestReviewDialog(
                quest = quest,
                onDismiss = { reviewModalQuest = null }
            )
        }
    }
}

/**
 * Круглый узел вертикальной сага-карты со змейкой и мини-питомцем на активном уровне
 */
@Composable
private fun SagaVerticalNode(
    quest: FinancialQuest,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    pet: Pet,
    xOffsetDp: Float,
    nextXOffsetDp: Float,
    hasNext: Boolean,
    accentColor: Color,
    sideQuestIndex: Int? = null,
    onSideQuestClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = xOffsetDp.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Узел с кнопкой 64dp по центру и питомцем на обочине (не перекрывает кнопку)
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            // Боковое бонусное испытание («Фин-блиц») на обочине — не разрывает соединительную линию саги
            if (sideQuestIndex != null && !isCurrent) {
                val sideOffset = if (xOffsetDp >= 0f) (-105).dp else 105.dp
                Box(
                    modifier = Modifier
                        .offset(x = sideOffset)
                        .align(Alignment.Center)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.5.dp, Color(0xFFB45309)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.bounceClick { onSideQuestClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚡", fontSize = 13.sp)
                            Column {
                                Text(
                                    text = "Блиц #$sideQuestIndex",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "+15 🪙",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }
            }

            // Если это активный уровень — питомец стоит на обочине и указывает лапкой на кнопку!
            if (isCurrent) {
                val petOnLeft = xOffsetDp >= 0f
                val roadsideOffset = if (petOnLeft) (-86).dp else 86.dp
                Row(
                    modifier = Modifier
                        .offset(x = roadsideOffset)
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!petOnLeft) {
                        Text("👈", fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(2.dp, accentColor),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                PetAvatarView(
                                    pet = pet,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor,
                            shadowElevation = 2.dp,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Твой ход! ✨",
                                fontSize = 9.sp,
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    if (petOnLeft) {
                        Text("👉", fontSize = 16.sp)
                    }
                }
            }

            // Основной круглый узел уровня (64dp, чистый и свободный для нажатия)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                // Пульсирующий ореол для активного уровня
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                            .background(accentColor.copy(alpha = pulseAlpha), shape = CircleShape)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = when {
                        isCompleted -> StatGreenEmerald
                        isCurrent -> accentColor
                        else -> SurfaceLight
                    },
                    border = BorderStroke(
                        width = if (isCurrent) 3.5.dp else 2.5.dp,
                        color = when {
                            isCompleted -> Color(0xFF15803D)
                            isCurrent -> Color.White
                            else -> OutlineLight
                        }
                    ),
                    shadowElevation = if (isCurrent) 6.dp else 2.dp,
                    modifier = Modifier
                        .size(64.dp)
                        .bounceClick { onClick() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            isCompleted -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "★ ★ ★",
                                        fontSize = 7.sp,
                                        color = FinCoinGold,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            isCurrent -> {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "#${quest.orderIndex}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Подпись уровня
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Уровень ${quest.orderIndex}",
                fontFamily = UnboundedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isCurrent) accentColor else if (isUnlocked) TextPrimary else TextSecondary
            )
            Text(
                text = quest.title,
                fontSize = 10.5.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (quest.isWrittenInput) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Text(
                        text = "✍️ Ввод ответа",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Соединяющая тропинка вниз к следующему узлу со следами кошачьих лапок 🐾
        if (hasNext) {
            Canvas(
                modifier = Modifier
                    .width(120.dp)
                    .height(46.dp)
            ) {
                val startX = size.width / 2f
                val startY = 0f
                val deltaX = (nextXOffsetDp - xOffsetDp) * 2.5f
                val endX = startX + deltaX
                val endY = size.height
                val midY = size.height * 0.5f

                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        startX, midY,
                        endX, midY,
                        endX, endY
                    )
                }

                val pathColor = if (isCompleted) StatGreenEmerald else Color(0xFF94A3B8)
                // Пунктирная линия дорожки
                drawPath(
                    path = path,
                    color = pathColor.copy(alpha = 0.5f),
                    style = Stroke(
                        width = 3.5f,
                        cap = StrokeCap.Round,
                        pathEffect = if (isCompleted) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                )

                // Следы кошачьих лапок вдоль дорожки (paw prints) 🐾
                val pawSteps = listOf(0.35f, 0.70f)
                pawSteps.forEach { t ->
                    val oneMinusT = 1f - t
                    val px = oneMinusT * oneMinusT * oneMinusT * startX +
                            3f * oneMinusT * oneMinusT * t * startX +
                            3f * oneMinusT * t * t * endX +
                            t * t * t * endX
                    val py = oneMinusT * oneMinusT * oneMinusT * startY +
                            3f * oneMinusT * oneMinusT * t * midY +
                            3f * oneMinusT * t * t * midY +
                            t * t * t * endY

                    val pawColor = if (isCompleted) StatGreenEmerald else Color(0xFF64748B)
                    // Главная подушечка лапки
                    drawCircle(color = pawColor, radius = 4f, center = Offset(px, py))
                    // Пальчики-подушечки
                    drawCircle(color = pawColor, radius = 1.8f, center = Offset(px - 3.8f, py - 4f))
                    drawCircle(color = pawColor, radius = 2f, center = Offset(px, py - 5.5f))
                    drawCircle(color = pawColor, radius = 1.8f, center = Offset(px + 3.8f, py - 4f))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Боковое бонусное испытание («Фин-блиц») в виде деревянного указателя с золотой молнией ⚡
 */
@Composable
private fun SagaSideQuestNode(
    branchIndex: Int,
    sideOffsetDp: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .offset(x = sideOffsetDp.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEF3C7),
            border = BorderStroke(2.dp, Color(0xFFB45309)), // Деревянный ободок указателя
            shadowElevation = 3.dp,
            modifier = Modifier.bounceClick { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFDE68A),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚡", fontSize = 15.sp)
                    }
                }
                Column {
                    Text(
                        text = "Фин-блиц #$branchIndex",
                        fontFamily = UnboundedFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = "+15 🪙 бонус",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD97706)
                    )
                }
            }
        }
    }
}



@Composable
private fun DuolingoQuestLessonScreen(
    quest: FinancialQuest,
    pet: Pet,
    onDismiss: () -> Unit,
    onComplete: (optionIndex: Int, earnedCoins: Int) -> Unit
) {
    val currentTheme = LocalAppTheme.current
    val stages = remember(quest) { quest.allStages }
    var currentStageIndex by remember { mutableIntStateOf(0) }
    var earnedCoinsTotal by remember { mutableIntStateOf(0) }

    val currentStage = stages[currentStageIndex]

    // Случайное перемешивание вариантов ответов для каждого этапа
    val shuffledOptions = remember(quest.id, currentStageIndex) {
        val seed = quest.id.hashCode() + currentStageIndex * 47 + 13
        currentStage.options.shuffled(Random(seed.toLong()))
    }

    var selectedOption by remember(currentStageIndex) { mutableStateOf<QuestOption?>(null) }
    var isChecked by remember(currentStageIndex) { mutableStateOf(false) }
    var wrongShakeTrigger by remember(currentStageIndex) { mutableIntStateOf(0) }
    var writtenInputText by remember(currentStageIndex) { mutableStateOf("") }
    var inputErrorMessage by remember(currentStageIndex) { mutableStateOf<String?>(null) }
    val checkmarkScale = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // 1. Верхняя панель Duolingo: (✕), Прогресс, Монеты
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка (✕) закрытия урока с возвратом на карту
                Surface(
                    shape = CircleShape,
                    color = SurfaceLight,
                    border = BorderStroke(2.dp, if (currentTheme == AppTheme.MONOCHROME_MINIMAL) Color(0x55FFFFFF) else Color(0xFF334155)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .bounceClick {
                            SoundHapticManager.performClickHaptic()
                            onDismiss()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Выйти из урока",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Индикатор прогресса этапов урока (Duolingo style)
                val targetProgress = (currentStageIndex + 1).toFloat() / stages.size.toFloat()
                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "lessonProgress"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(SurfaceVariantLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = animatedProgress)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(StatGreenEmerald, Color(0xFF34D399))
                                )
                            )
                    )
                }

                // Индикатор заработанных монет
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatGreenEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StatGreenEmerald)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_coin_vector),
                            contentDescription = null,
                            tint = FinCoinGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "+$earnedCoinsTotal",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = StatGreenEmerald
                        )
                    }
                }
            }

            // ==========================================
            // 2. Скроллируемая центральная часть урока
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Блок маскота Финни с репликой урока
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SurfaceLight,
                        border = BorderStroke(2.dp, currentTheme.primaryColor),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(68.dp)
                    ) {
                        PetAvatarView(
                            pet = pet,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    // Речевой пузырь Финни с темой этапа
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceLight,
                        border = BorderStroke(1.5.dp, OutlineLight),
                        shadowElevation = 2.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Этап ${currentStageIndex + 1} из ${stages.size}",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = currentTheme.primaryColor
                            )
                            Text(
                                text = currentStage.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Антифрод-симуляция (если этап FRAUD_CHECK)
                if (currentStage.stageType == StageType.FRAUD_CHECK && currentStage.messageText != null) {
                    val isDark = LocalDarkTheme.current || currentTheme == AppTheme.MONOCHROME_MINIMAL
                    val fraudBg = if (isDark) DangerRed.copy(alpha = 0.18f) else Color(0xFFFEF2F2)
                    val fraudBorder = if (isDark) DangerRed.copy(alpha = 0.45f) else Color(0xFFFCA5A5)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = fraudBg,
                        border = BorderStroke(2.dp, fraudBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(currentStage.senderAvatar ?: "⚠️", fontSize = 18.sp)
                                Text(
                                    text = currentStage.senderName ?: "Входящее сообщение",
                                    fontFamily = UnboundedFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DangerRed
                                )
                            }
                            Text(
                                text = currentStage.messageText,
                                fontSize = 12.5.sp,
                                color = TextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Карточка ситуации / вопроса
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceLight,
                    border = BorderStroke(2.dp, OutlineLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentStage.promptText,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                if (currentStage.isWrittenInput) {
                    Text(
                        text = "Введи свой ответ:",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    val isWrittenCorrect = isChecked && selectedOption?.isOptimal == true
                    val isWrittenWrong = isChecked && selectedOption?.isOptimal == false
                    val fieldBorderColor = when {
                        isWrittenCorrect -> StatGreenEmerald
                        isWrittenWrong || inputErrorMessage != null -> Color(0xFFEF4444)
                        else -> currentTheme.primaryColor
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceLight,
                        border = BorderStroke(2.dp, fieldBorderColor),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (wrongShakeTrigger > 0 && !isChecked) Modifier.shake(wrongShakeTrigger, 8f) else Modifier)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = writtenInputText,
                                onValueChange = {
                                    if (!isChecked) {
                                        writtenInputText = it
                                        inputErrorMessage = null
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = currentStage.inputPlaceholder,
                                        color = TextSecondary.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isChecked,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (inputErrorMessage != null) {
                                Text(
                                    text = inputErrorMessage!!,
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = currentTheme.primaryColor.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "✍️", fontSize = 14.sp)
                                    Text(
                                        text = "Письменное задание: посчитай и запиши число.",
                                        fontSize = 11.5.sp,
                                        color = currentTheme.primaryColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Выберите правильный вариант:",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Варианты ответов (перемешанные случайным образом!)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    shuffledOptions.forEachIndexed { index, option ->
                        val isChosen = selectedOption == option
                        val isRightChoice = isChecked && isChosen && option.isOptimal
                        val isWrongChoice = isChecked && isChosen && !option.isOptimal
                        val letter = when (index) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            3 -> "D"
                            else -> "${index + 1}"
                        }

                        val cardBorderColor = when {
                            isRightChoice -> StatGreenEmerald
                            isWrongChoice -> Color(0xFFEF4444)
                            isChosen -> currentTheme.primaryColor
                            else -> OutlineLight
                        }

                        val cardBgColor = when {
                            isRightChoice -> StatGreenEmerald.copy(alpha = 0.12f)
                            isWrongChoice -> Color(0xFFEF4444).copy(alpha = 0.10f)
                            isChosen -> currentTheme.bubbleBg
                            else -> SurfaceLight
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBgColor,
                            border = BorderStroke(if (isChosen) 2.5.dp else 1.5.dp, cardBorderColor),
                            shadowElevation = if (isChosen) 3.dp else 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isWrongChoice) Modifier.shake(wrongShakeTrigger, 8f) else Modifier)
                                .bounceClick(enabled = !isChecked) {
                                    SoundHapticManager.performTapHaptic()
                                    selectedOption = option
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isRightChoice -> StatGreenEmerald
                                        isWrongChoice -> Color(0xFFEF4444)
                                        isChosen -> currentTheme.primaryColor
                                        else -> SurfaceSubtle
                                    },
                                    border = BorderStroke(1.dp, when {
                                        isRightChoice -> StatGreenEmerald
                                        isWrongChoice -> Color(0xFFEF4444)
                                        isChosen -> currentTheme.primaryColor
                                        else -> OutlineLight
                                    }),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        when {
                                            isRightChoice -> {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .scale(checkmarkScale.value)
                                                )
                                            }
                                            isWrongChoice -> {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            else -> {
                                                Text(
                                                    text = letter,
                                                    fontFamily = UnboundedFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isChosen) Color.White else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = option.text,
                                    fontSize = 13.sp,
                                    lineHeight = 17.5.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // 3. Интерактивный подвал проверки (Duolingo Footer)
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineLight),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Если решение уже проверено — выводим результат и пояснение
                    if (isChecked && selectedOption != null) {
                        val isOptimal = selectedOption!!.isOptimal

                        val isDark = LocalDarkTheme.current || currentTheme == AppTheme.MONOCHROME_MINIMAL
                        val feedbackBg = when {
                            isOptimal && isDark -> StatGreenEmerald.copy(alpha = 0.20f)
                            isOptimal -> Color(0xFFDCFCE7)
                            isDark -> Color(0xFFEF4444).copy(alpha = 0.20f)
                            else -> Color(0xFFFEE2E2)
                        }
                        val feedbackBorder = if (isOptimal) StatGreenEmerald else Color(0xFFEF4444)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = feedbackBg,
                            border = BorderStroke(2.dp, feedbackBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isOptimal) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                            contentDescription = null,
                                            tint = if (isOptimal) StatGreenEmerald else Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (isOptimal) "Великолепно!" else "Обратите внимание:",
                                            fontFamily = UnboundedFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isOptimal) StatGreenEmerald else Color(0xFFEF4444)
                                        )
                                    }

                                    // Явное указание начисления монет
                                    if (isOptimal && selectedOption!!.coinReward > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StatGreenEmerald.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, StatGreenEmerald)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_coin_vector),
                                                    contentDescription = null,
                                                    tint = FinCoinGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "+${selectedOption!!.coinReward} монет",
                                                    fontFamily = UnboundedFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    color = StatGreenEmerald
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = selectedOption!!.feedbackExplanation,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        val isLastStage = currentStageIndex == stages.size - 1
                        CartoonButton(
                            text = when {
                                !isLastStage -> "Следующий этап →"
                                isOptimal && earnedCoinsTotal > 0 -> "Завершить (+${earnedCoinsTotal} монет)"
                                else -> "Понятно, завершить"
                            },
                            onClick = {
                                if (!isLastStage) {
                                    SoundHapticManager.performClickHaptic()
                                    currentStageIndex++
                                    selectedOption = null
                                    writtenInputText = ""
                                    inputErrorMessage = null
                                    isChecked = false
                                    wrongShakeTrigger = 0
                                    coroutineScope.launch {
                                        checkmarkScale.snapTo(0f)
                                    }
                                } else {
                                    SoundHapticManager.performSuccessHaptic()
                                    val originalIdx = quest.options.indexOfFirst { it.text == selectedOption?.text }.coerceAtLeast(0)
                                    onComplete(originalIdx, earnedCoinsTotal)
                                }
                            },
                            containerColor = if (isOptimal) StatGreenEmerald else currentTheme.primaryColor,
                            height = 48.dp,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Кнопка "Проверить" до проверки
                        val canCheck = if (currentStage.isWrittenInput) writtenInputText.isNotBlank() else selectedOption != null
                        CartoonButton(
                            text = "Проверить",
                            enabled = canCheck,
                            onClick = {
                                if (currentStage.isWrittenInput) {
                                    val cleanInput = writtenInputText.trim().lowercase()
                                    val isAnswerCorrect = currentStage.acceptedAnswers.any { it.trim().lowercase() == cleanInput } ||
                                            cleanInput == "35"
                                    if (isAnswerCorrect) {
                                        selectedOption = currentStage.options.firstOrNull { it.isOptimal } ?: currentStage.options.first()
                                        isChecked = true
                                        SoundHapticManager.performSuccessHaptic()
                                        earnedCoinsTotal += selectedOption!!.coinReward
                                        coroutineScope.launch {
                                            checkmarkScale.snapTo(0f)
                                            checkmarkScale.animateTo(1.2f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                                            checkmarkScale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                        }
                                    } else {
                                        wrongShakeTrigger++
                                        SoundHapticManager.performErrorHaptic()
                                        inputErrorMessage = "Не совсем так. Проверь вычисление (100 - 65 = ?) и попробуй снова!"
                                    }
                                } else {
                                    if (selectedOption != null) {
                                        isChecked = true
                                        if (selectedOption!!.isOptimal) {
                                            SoundHapticManager.performSuccessHaptic()
                                            earnedCoinsTotal += selectedOption!!.coinReward
                                            coroutineScope.launch {
                                                checkmarkScale.snapTo(0f)
                                                checkmarkScale.animateTo(1.2f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                                                checkmarkScale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            }
                                        } else {
                                            wrongShakeTrigger++
                                            SoundHapticManager.performErrorHaptic()
                                        }
                                    }
                                }
                            },
                            containerColor = if (canCheck) StatGreenEmerald else Color(0xFF94A3B8),
                            contentColor = Color.White,
                            height = 48.dp,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Диалог повторного просмотра выполненного задания
 */
@Composable
private fun QuestReviewDialog(
    quest: FinancialQuest,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = StatGreenEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Задание #${quest.orderIndex}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = quest.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = quest.dilemmaText,
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Закрыть", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

/**
 * Диалог шпаргалки темы (гайд юного финансиста)
 */
@Composable
private fun TheoryGuideDialog(
    topic: QuestTopic,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = BrandVioletPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Раздел ${topic.unitIndex}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = topic.unitTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = BrandVioletPrimary
                )
                Text(
                    text = topic.description,
                    fontSize = 12.5.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Понятно", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

/**
 * Анимация начисления монет
 */
@Composable
private fun CoinFlyCelebration(
    rewardCoins: Int,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1800)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceLight,
            shadowElevation = 8.dp,
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🪙", fontSize = 42.sp)
                Text(
                    text = "+$rewardCoins монет!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = BrandVioletPrimary
                )
                Text(
                    text = "Награда зачислена в ваш кошелек",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
