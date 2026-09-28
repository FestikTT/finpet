import os

with open(r'app/src/main/java/ru/finpet/app/ui/screens/QuestsScreen.kt', 'r', encoding='utf-8') as f:
    full_text = f.read()

lesson_start = full_text.find('private fun DuolingoQuestLessonScreen(')
lesson_and_below = full_text[lesson_start:]

new_top = '''package ru.finpet.app.ui.screens

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
    val currentTheme = LocalAppTheme.current
    var activeModalQuest by remember { mutableStateOf<FinancialQuest?>(null) }
    var reviewModalQuest by remember { mutableStateOf<FinancialQuest?>(null) }
    var lockToastMessage by remember { mutableStateOf<String?>(null) }
    var activeTheoryTopic by remember { mutableStateOf<QuestTopic?>(null) }
    var completedRewardCoins by remember { mutableStateOf<Int?>(null) }

    val units = remember { QuestTopic.values().toList() }
    val activeQuest = remember(state.quests, state.nextActiveQuestOrder) {
        state.quests.find { it.orderIndex == state.nextActiveQuestOrder }
            ?: state.quests.firstOrNull { !it.isCompleted }
            ?: state.quests.lastOrNull()
    }
    var selectedUnitIndex by remember { mutableIntStateOf(activeQuest?.topic?.unitIndex ?: 1) }

    val selectedUnit = remember(selectedUnitIndex) {
        units.find { it.unitIndex == selectedUnitIndex } ?: units.first()
    }

    val currentBiome = remember(selectedUnitIndex) {
        CHAPTER_BIOMES.find { it.unitIndex == selectedUnitIndex } ?: CHAPTER_BIOMES.first()
    }

    val currentUnitQuests = remember(selectedUnitIndex, state.quests) {
        state.quests.filter { it.topic.unitIndex == selectedUnitIndex }
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
                            modifier = Modifier.weight(1f, fill = false)
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

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = currentBiome.accentColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "ГЛАВА $selectedUnitIndex: ${currentBiome.name.uppercase()}",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = currentBiome.accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = selectedUnit.unitTitle.substringAfter(": ").trim(),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
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
                        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
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

                            // Каждые 3 задания — дополнительное ответвление (бонусный сайд-квест)
                            if ((index + 1) % 3 == 0 && index < currentUnitQuests.size - 1) {
                                SagaSideQuestNode(
                                    branchIndex = (index + 1) / 3,
                                    sideOffsetDp = if (serpentineX <= 0f) 85f else -85f,
                                    onClick = {
                                        SoundHapticManager.performClickHaptic()
                                        ru.finpet.app.data.GameRepository.awardParentCoins(15, "Фин-блиц за выполнение 3 заданий подряд!")
                                        completedRewardCoins = 15
                                    }
                                )
                            }
                        }

                        // Финальный сундук биома в конце главы
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SagaMilestoneChestNode(
                                unitIndex = selectedUnitIndex,
                                isAllCompleted = unitCompletedCount == unitTotalCount,
                                biome = currentBiome,
                                onClaim = {
                                    if (unitCompletedCount == unitTotalCount) {
                                        SoundHapticManager.performSuccessHaptic()
                                        ru.finpet.app.data.GameRepository.awardParentCoins(50, "Бонус за прохождение главы $selectedUnitIndex")
                                        completedRewardCoins = 50
                                    } else {
                                        SoundHapticManager.performLockHaptic()
                                        lockToastMessage = "Пройдите все $unitTotalCount заданий главы, чтобы открыть сундук с 50 🪙!"
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
                coinsEarned = coins,
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
        // Если это текущий активный уровень — отображаем мини-питомца прямо над узлом!
        if (isCurrent) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                // Аватарка питомца
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, accentColor),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PetAvatarView(
                            pet = pet,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Облачко диалога
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "Твой ход! ✨",
                        fontSize = 10.sp,
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Основной круглый узел уровня
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Соединяющая тропинка вниз к следующему узлу
        if (hasNext) {
            Canvas(
                modifier = Modifier
                    .width(100.dp)
                    .height(44.dp)
            ) {
                val startX = size.width / 2f
                val startY = 0f
                val deltaX = (nextXOffsetDp - xOffsetDp) * 2.5f
                val endX = startX + deltaX
                val endY = size.height

                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        startX, startY + size.height * 0.5f,
                        endX, startY + size.height * 0.5f,
                        endX, endY
                    )
                }

                drawPath(
                    path = path,
                    color = if (isCompleted) StatGreenEmerald else OutlineLight,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = if (isCompleted) null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Боковое бонусное испытание («Фин-блиц»)
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
            .padding(vertical = 4.dp)
            .offset(x = sideOffsetDp.dp),
        horizontalArrangement = Alignment.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            shadowElevation = 3.dp,
            modifier = Modifier.bounceClick { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("⚡", fontSize = 18.sp)
                Column {
                    Text(
                        text = "Фин-блиц!",
                        fontFamily = UnboundedFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = "+15 монет",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD97706)
                    )
                }
            }
        }
    }
}

/**
 * Финальный сундук главы
 */
@Composable
private fun SagaMilestoneChestNode(
    unitIndex: Int,
    isAllCompleted: Boolean,
    biome: ChapterBiome,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isAllCompleted) Color(0xFFFEF3C7) else SurfaceSubtle,
        border = BorderStroke(2.dp, if (isAllCompleted) Color(0xFFF59E0B) else OutlineLight),
        shadowElevation = if (isAllCompleted) 4.dp else 1.dp,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .bounceClick { onClaim() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isAllCompleted) "🎁" else "🔒",
                fontSize = 32.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isAllCompleted) "Сундук главы $unitIndex готов!" else "Сундук главы $unitIndex",
                    fontFamily = UnboundedFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllCompleted) Color(0xFFB45309) else TextPrimary
                )
                Text(
                    text = if (isAllCompleted) "Нажмите, чтобы забрать 50 🪙!" else "Пройдите все задания биома",
                    fontSize = 11.sp,
                    color = if (isAllCompleted) Color(0xFFD97706) else TextSecondary
                )
            }
        }
    }
}
'''

new_full = new_top + '\n\n' + lesson_and_below

with open(r'app/src/main/java/ru/finpet/app/ui/screens/QuestsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(new_full)

print("QuestsScreen.kt updated with Vertical Serpentine Saga Map successfully!")
