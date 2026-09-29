package ru.finpet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.model.*
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.components.shake
import ru.finpet.app.ui.theme.*
import ru.finpet.app.util.FormatUtils

@Composable
fun BudgetScreen(
    state: GameState,
    onUpdatePlan: (needs: Int, wants: Int, savings: Int) -> Unit,
    onConfirmPlan: () -> Unit,
    onAdvancePeriod: () -> Unit = {}
) {
    val currentTheme = LocalAppTheme.current
    val plan = state.budgetPlan
    var isEditing by remember(plan.isConfirmed) { mutableStateOf(!plan.isConfirmed) }
    var mainBudgetMode by remember { mutableIntStateOf(0) } // 0: План бюджета, 1: История операций

    var needsState by remember(plan.needsPlan) { mutableStateOf(plan.needsPlan.toFloat()) }
    var wantsState by remember(plan.wantsPlan) { mutableStateOf(plan.wantsPlan.toFloat()) }
    var savingsState by remember(plan.savingsPlan) { mutableStateOf(plan.savingsPlan.toFloat()) }

    val totalAmount = plan.availableAmount
    val currentTotalAllocated = (needsState.toInt() + wantsState.toInt() + savingsState.toInt())
    val unallocated = totalAmount - currentTotalAllocated

    var shakeTrigger by remember { mutableIntStateOf(0) }
    var prevUnallocated by remember { mutableIntStateOf(unallocated) }
    val badgeBounceScale = remember { Animatable(1.0f) }
    val glowFlashAlpha = remember { Animatable(0f) }

    LaunchedEffect(unallocated) {
        if (unallocated == 0 && prevUnallocated != 0) {
            // Идеальный баланс 100 из 100 монет!
            SoundHapticManager.performSuccessHaptic()
            launch {
                badgeBounceScale.snapTo(1.0f)
                badgeBounceScale.animateTo(1.18f, tween(120, easing = FastOutSlowInEasing))
                badgeBounceScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
            launch {
                glowFlashAlpha.snapTo(1.0f)
                glowFlashAlpha.animateTo(0f, tween(650, easing = LinearEasing))
            }
        } else if (unallocated < 0 && prevUnallocated >= 0) {
            // Превышение лимита (>100%)
            shakeTrigger++
            SoundHapticManager.performErrorHaptic()
        }
        prevUnallocated = unallocated
    }

    var isDraggingNeeds by remember { mutableStateOf(false) }
    var isDraggingWants by remember { mutableStateOf(false) }
    var isDraggingSavings by remember { mutableStateOf(false) }
    val isDraggingSlider = isDraggingNeeds || isDraggingWants || isDraggingSavings
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState, enabled = !isDraggingSlider)
            .padding(16.dp)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Главный переключатель: План бюджета / История трат и покупок
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceVariantLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .bounceClick {
                            SoundHapticManager.performClickHaptic()
                            mainBudgetMode = 0
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (mainBudgetMode == 0) currentTheme.primaryColor else Color.Transparent
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painterResource(ru.finpet.app.R.drawable.ic_wallet),
                                contentDescription = null,
                                tint = if (mainBudgetMode == 0) Color.White else currentTheme.primaryColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "План бюджета",
                                fontSize = 12.sp,
                                fontFamily = UnboundedFamily,
                                fontWeight = if (mainBudgetMode == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (mainBudgetMode == 0) Color.White else TextPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .bounceClick {
                            SoundHapticManager.performClickHaptic()
                            mainBudgetMode = 1
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (mainBudgetMode == 1) currentTheme.primaryColor else Color.Transparent
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Receipt,
                                contentDescription = null,
                                tint = if (mainBudgetMode == 1) Color.White else currentTheme.primaryColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "История трат",
                                fontSize = 12.sp,
                                fontFamily = UnboundedFamily,
                                fontWeight = if (mainBudgetMode == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (mainBudgetMode == 1) Color.White else TextPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (mainBudgetMode == 1) {
            TransactionHistorySection(state = state)
        } else {
            // Заголовок раздела бюджета
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Бюджет периода ${state.currentPeriod}",
                    fontFamily = UnboundedFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Планирование и контроль карманных денег по правилу 50/30/20",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Сегментированный переключатель: Планирование / Исполнение (Факт)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceVariantLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .bounceClick {
                                SoundHapticManager.performClickHaptic()
                                isEditing = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isEditing) SurfaceLight else Color.Transparent,
                        border = if (isEditing) BorderStroke(1.dp, OutlineLight) else null,
                        shadowElevation = if (isEditing) 1.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = "Планирование",
                                fontSize = 12.sp,
                                fontFamily = UnboundedFamily,
                                fontWeight = if (isEditing) FontWeight.Bold else FontWeight.Medium,
                                color = if (isEditing) currentTheme.primaryColor else TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .bounceClick {
                                SoundHapticManager.performClickHaptic()
                                isEditing = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isEditing) SurfaceLight else Color.Transparent,
                        border = if (!isEditing) BorderStroke(1.dp, OutlineLight) else null,
                        shadowElevation = if (!isEditing) 1.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = "Исполнение",
                                fontSize = 12.sp,
                                fontFamily = UnboundedFamily,
                                fontWeight = if (!isEditing) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isEditing) currentTheme.primaryColor else TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Карточка дохода периода и накоплений (аккуратный дизайн без съезжания)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceLight,
                border = if (glowFlashAlpha.value > 0f) {
                    BorderStroke(2.dp, StatGreenEmerald.copy(alpha = glowFlashAlpha.value))
                } else {
                    BorderStroke(1.5.dp, currentTheme.borderColor)
                },
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Верхний ряд: Название и сумма дохода периода
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "ДОХОД ПЕРИОДА",
                                fontFamily = UnboundedFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Savings,
                                    contentDescription = null,
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = FormatUtils.formatCoins(totalAmount),
                                    fontFamily = UnboundedFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTheme.primaryColor
                                )
                            }
                        }

                        // Чип периода
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = currentTheme.primaryColor.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Период ${state.currentPeriod}",
                                fontFamily = UnboundedFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Text(
                        text = "Карманные деньги",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    HorizontalDivider(color = OutlineLight, thickness = 1.dp)

                    // Нижний ряд: Кошелек и Копилка - стабильная фиксированная высота без съезжания
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(ru.finpet.app.R.drawable.ic_wallet),
                                    contentDescription = null,
                                    tint = BrandRoseWarm,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "В кошельке",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = FormatUtils.formatCoins(needsState.toInt() + wantsState.toInt()),
                                        fontFamily = UnboundedFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(ru.finpet.app.R.drawable.ic_piggy_bank),
                                    contentDescription = null,
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "В копилке",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = FormatUtils.formatCoins(savingsState.toInt()),
                                        fontFamily = UnboundedFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentTheme.primaryColor,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (isEditing) {
                        val statusColor = if (unallocated == 0) StatGreenEmerald else if (unallocated > 0) currentTheme.primaryColor else DangerRedColor
                        val statusBg = statusColor.copy(alpha = 0.12f)

                        // Визуальная диаграмма правила 50/30/20
                        val safeTotal = totalAmount.coerceAtLeast(1).toFloat()
                        val needsRatio = (needsState / safeTotal).coerceIn(0f, 1f)
                        val wantsRatio = (wantsState / safeTotal).coerceIn(0f, 1f)
                        val savingsRatio = (savingsState / safeTotal).coerceIn(0f, 1f)

                        val totalAllocatedCoins = (needsState + wantsState + savingsState).toInt()
                        val totalAvailableCoins = plan.availableAmount
                        val allocatedPercent = if (totalAvailableCoins > 0) (totalAllocatedCoins * 100 / totalAvailableCoins) else 0

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Заголовок правила 50/30/20 и единый горизонтальный индикатор распределения
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Правило 50/30/20",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$totalAllocatedCoins из $totalAvailableCoins ($allocatedPercent%)",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        maxLines = 1
                                    )
                                }

                                // Полоса прогресса 3 сегментов
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(OutlineLight.copy(alpha = 0.4f))
                                ) {
                                    if (needsRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(needsRatio.coerceAtLeast(0.01f))
                                                .fillMaxHeight()
                                                .background(BrandRoseWarm)
                                        )
                                    }
                                    if (wantsRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(wantsRatio.coerceAtLeast(0.01f))
                                                .fillMaxHeight()
                                                .background(BrandLavender)
                                        )
                                    }
                                    if (savingsRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(savingsRatio.coerceAtLeast(0.01f))
                                                .fillMaxHeight()
                                                .background(currentTheme.primaryColor)
                                        )
                                    }
                                }
                            }

                            // Легенда диаграммы
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BudgetLegend("Обязательное", needsRatio, BrandRoseWarm, Modifier.weight(1f))
                                BudgetLegend("Хотелки", wantsRatio, BrandLavender, Modifier.weight(1f))
                                BudgetLegend("Копилка", savingsRatio, currentTheme.primaryColor, Modifier.weight(1f))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusBg,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = if (unallocated == 0) "Бюджет сбалансирован!" else if (unallocated > 0) "Осталось распределить: ${FormatUtils.formatCoins(unallocated)}" else "Превышение: ${FormatUtils.formatCoins(-unallocated)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (isEditing) {
                Text(
                    text = "Распределение дохода по конвертам:",
                    fontFamily = UnboundedFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                EnvelopeEditCard(
                    envelope = EnvelopeType.NEEDS,
                    value = needsState.toInt(),
                    onValueChange = {
                        needsState = it.coerceIn(0, totalAmount).toFloat()
                        onUpdatePlan(needsState.toInt(), wantsState.toInt(), savingsState.toInt())
                    },
                    maxVal = totalAmount,
                    accentColor = BrandRoseWarm,
                    onInteractingChange = { isDraggingNeeds = it }
                )

                EnvelopeEditCard(
                    envelope = EnvelopeType.WANTS,
                    value = wantsState.toInt(),
                    onValueChange = {
                        wantsState = it.coerceIn(0, totalAmount).toFloat()
                        onUpdatePlan(needsState.toInt(), wantsState.toInt(), savingsState.toInt())
                    },
                    maxVal = totalAmount,
                    accentColor = BrandLavender,
                    onInteractingChange = { isDraggingWants = it }
                )

                EnvelopeEditCard(
                    envelope = EnvelopeType.SAVINGS,
                    value = savingsState.toInt(),
                    onValueChange = {
                        savingsState = it.coerceIn(0, totalAmount).toFloat()
                        onUpdatePlan(needsState.toInt(), wantsState.toInt(), savingsState.toInt())
                    },
                    maxVal = totalAmount,
                    accentColor = currentTheme.primaryColor,
                    onInteractingChange = { isDraggingSavings = it }
                )

                CartoonButton(
                    text = if (unallocated == 0) "Зафиксировать бюджет" else "Осталось: ${FormatUtils.formatCoins(unallocated)}",
                    onClick = {
                        SoundHapticManager.performClickHaptic()
                        onConfirmPlan()
                        isEditing = false
                    },
                    enabled = unallocated == 0,
                    containerColor = currentTheme.primaryColor,
                    height = 48.dp,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Режим исполнения (Факт vs План)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ФАКТИЧЕСКИЕ РАСХОДЫ:",
                        fontFamily = UnboundedFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    TextButton(
                        onClick = { isEditing = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Изменить план",
                                fontFamily = UnboundedFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.primaryColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                PlanVsFactCard(
                    envelope = EnvelopeType.NEEDS,
                    planned = plan.needsPlan,
                    fact = plan.needsFact,
                    accentColor = BrandRoseWarm
                )

                PlanVsFactCard(
                    envelope = EnvelopeType.WANTS,
                    planned = plan.wantsPlan,
                    fact = plan.wantsFact,
                    accentColor = BrandLavender
                )

                PlanVsFactCard(
                    envelope = EnvelopeType.SAVINGS,
                    planned = plan.savingsPlan,
                    fact = plan.savingsFact,
                    accentColor = currentTheme.primaryColor
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, OutlineLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                painter = painterResource(ru.finpet.app.R.drawable.ic_tip),
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Совет от питомца", fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                        Text(
                            text = if (plan.isSuccessful) {
                                "Отличная работа! Ты вписался в бюджет и сберёг монеты на мечту! Питомец гордится тобой!"
                            } else {
                                "Ничего страшного! В следующем периоде попробуй чуть аккуратнее расходовать монетки. У тебя всё получится!"
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Итоги последнего игрового периода
                if (state.currentPeriod > 1) {
                    var showPrevPeriodSummary by remember { mutableStateOf(false) }
                    val prevPeriodNum = state.currentPeriod - 1
                    val prevPeriodInfo = ru.finpet.app.model.GamePeriodRepository.getPeriod(prevPeriodNum)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { showPrevPeriodSummary = !showPrevPeriodSummary },
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, OutlineLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Rounded.Assessment,
                                        contentDescription = null,
                                        tint = currentTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = "Итоги периода $prevPeriodNum (завершен)",
                                            fontFamily = UnboundedFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = prevPeriodInfo.title.substringAfter(": "),
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (showPrevPeriodSummary) "Скрыть" else "Детали",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTheme.primaryColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            if (showPrevPeriodSummary) {
                                HorizontalDivider(color = OutlineLight)
                                Text(
                                    text = "Цель периода: ${prevPeriodInfo.educationalGoal}",
                                    fontSize = 11.5.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Бюджетный план периода: ${prevPeriodInfo.pocketMoneyAmount} монет",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatGreenEmerald.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, StatGreenEmerald.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "✓ Период успешно пройден! Питомец получил опыт и развил базовые финансовые навыки.",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StatGreenEmerald,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Кнопка подведения итогов текущего периода и перехода к следующему
                if (state.currentPeriod < 5) {
                    CartoonButton(
                        text = "Подвести итоги периода ${state.currentPeriod} 🏆",
                        onClick = {
                            SoundHapticManager.performSuccessHaptic()
                            onAdvancePeriod()
                        },
                        containerColor = currentTheme.primaryColor,
                        height = 48.dp,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👑", fontSize = 20.sp)
                            Text(
                                text = "Достигнут высший статус: Мастер финансов! Все периоды успешно пройдены.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Визуальная стеклянная банка с динамическим уровнем наполнения и плавающими золотыми монетами (Аудит п.14)
 */
@Composable
private fun CoinJarGlassVisual(
    envelope: EnvelopeType,
    value: Int,
    maxVal: Int,
    accentColor: Color
) {
    val fillRatio = if (maxVal > 0) (value.toFloat() / maxVal).coerceIn(0f, 1f) else 0f
    val animRatio by animateFloatAsState(
        targetValue = fillRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "jarFill"
    )
    val percentage = if (maxVal > 0) (value * 100 / maxVal) else 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.06f),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка конверта
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            when (envelope) {
                                EnvelopeType.NEEDS -> ru.finpet.app.R.drawable.ic_wallet
                                EnvelopeType.SAVINGS -> ru.finpet.app.R.drawable.ic_piggy_bank
                                EnvelopeType.WANTS -> ru.finpet.app.R.drawable.ic_gift
                            }
                        ),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Линия наполнения с монетками строго внутри полосы (никогда не выезжают за край)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val totalTrackWidth = maxWidth

                // Фоновый трек
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariantLight)
                )

                // Заполненная часть линии
                val fillWidth = (totalTrackWidth * animRatio).coerceIn(0.dp, totalTrackWidth)
                if (fillWidth > 0.dp) {
                    Box(
                        modifier = Modifier
                            .width(fillWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.70f), accentColor)
                                )
                            )
                    )
                }
            }

            // Бейдж процента
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "$percentage%",
                    fontFamily = UnboundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnvelopeEditCard(
    envelope: EnvelopeType,
    value: Int,
    onValueChange: (Int) -> Unit,
    maxVal: Int,
    accentColor: Color,
    onInteractingChange: (Boolean) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineLight)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            when (envelope) {
                                EnvelopeType.NEEDS -> ru.finpet.app.R.drawable.ic_wallet
                                EnvelopeType.SAVINGS -> ru.finpet.app.R.drawable.ic_piggy_bank
                                EnvelopeType.WANTS -> ru.finpet.app.R.drawable.ic_gift
                            }
                        ),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = envelope.shortName,
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = envelope.title,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "$value монет",
                    fontFamily = UnboundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = accentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            CoinJarGlassVisual(
                envelope = envelope,
                value = value,
                maxVal = maxVal,
                accentColor = accentColor
            )

            var lastTickVal by remember { mutableIntStateOf(value) }
            val sliderInteractionSource = remember { MutableInteractionSource() }
            val isPressed by sliderInteractionSource.collectIsPressedAsState()
            val isDragged by sliderInteractionSource.collectIsDraggedAsState()
            val isInteracting = isPressed || isDragged

            LaunchedEffect(isInteracting) {
                onInteractingChange(isInteracting)
            }

            val thumbScale by animateFloatAsState(
                targetValue = if (isInteracting) 1.25f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "thumbScale"
            )

            val percentage = if (maxVal > 0) (value * 100 / maxVal) else 0

            // Динамический цвет трека (зеленый -> предупреждающий желтый -> красный)
            val dynamicTrackColor = when {
                percentage > 85 -> DangerRedColor
                percentage > 60 -> Color(0xFFF59E0B)
                else -> accentColor
            }

            Slider(
                value = value.toFloat(),
                onValueChange = { newVal ->
                    val intVal = newVal.toInt().coerceIn(0, maxVal)
                    if (kotlin.math.abs(intVal - lastTickVal) >= 5) {
                        SoundHapticManager.performTickHaptic()
                        lastTickVal = intVal
                    }
                    onValueChange(intVal)
                },
                valueRange = 0f..maxVal.toFloat(),
                steps = 0,
                interactionSource = sliderInteractionSource,
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = sliderInteractionSource,
                        modifier = Modifier.scale(thumbScale),
                        colors = SliderDefaults.colors(thumbColor = dynamicTrackColor)
                    )
                },
                colors = SliderDefaults.colors(
                    thumbColor = dynamicTrackColor,
                    activeTrackColor = dynamicTrackColor,
                    inactiveTrackColor = OutlineLight
                )
            )

            val categoryTags = when (envelope) {
                EnvelopeType.NEEDS -> listOf("Корм", "Аптека", "Уход")
                EnvelopeType.SAVINGS -> listOf("Копилка", "Вклад", "Мечта")
                EnvelopeType.WANTS -> listOf("Игры", "Сладости", "Хобби")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalButton(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            onValueChange((value - 5).coerceAtLeast(0))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                        onValueChange((value + 5).coerceAtMost(maxVal))
                        },
                        enabled = value < maxVal,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanVsFactCard(
    envelope: EnvelopeType,
    planned: Int,
    fact: Int,
    accentColor: Color
) {
    val progress = if (planned > 0) (fact.toFloat() / planned).coerceIn(0f, 1f) else 0f
    val isOverspent = fact > planned
    val remaining = (planned - fact).coerceAtLeast(0)
    val overspentAmount = (fact - planned).coerceAtLeast(0)

    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "progress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineLight)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(
                            when (envelope) {
                                EnvelopeType.NEEDS -> ru.finpet.app.R.drawable.ic_wallet
                                EnvelopeType.SAVINGS -> ru.finpet.app.R.drawable.ic_piggy_bank
                                EnvelopeType.WANTS -> ru.finpet.app.R.drawable.ic_gift
                            }
                        ),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(envelope.shortName, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(
                            text = if (isOverspent) "Перерасход: $overspentAmount 🪙" else "Осталось: $remaining 🪙",
                            fontSize = 11.sp,
                            fontWeight = if (isOverspent) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isOverspent) DangerRedColor else TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Потрачено: $fact из $planned 🪙",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = if (isOverspent) DangerRedColor else accentColor,
                        softWrap = false
                    )
                    Text(
                        text = if (planned > 0) "${(fact * 100 / planned)}%" else "0%",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isOverspent) DangerRedColor else accentColor,
                trackColor = SurfaceVariantLight
            )
        }
    }
}

/**
 * Раздел полной истории трат и покупок питомца в игровых монетах (🪙)
 */
@Composable
private fun TransactionHistorySection(state: GameState) {
    val currentTheme = LocalAppTheme.current
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Все, 1: Траты, 2: Доходы

    val totalSpent = remember(state.transactions) {
        state.transactions.filter { !it.isIncome }.sumOf { it.amount }
    }
    val totalEarned = remember(state.transactions) {
        state.transactions.filter { it.isIncome }.sumOf { it.amount }
    }

    val filteredTransactions = remember(state.transactions, selectedFilter) {
        when (selectedFilter) {
            1 -> state.transactions.filter { !it.isIncome }
            2 -> state.transactions.filter { it.isIncome }
            else -> state.transactions
        }
    }

    // Группировка транзакций: Сегодня, Вчера, Ранее (Аудит п.17)
    val groupedTransactions: List<Pair<String, List<FinTransaction>>> = remember(filteredTransactions) {
        val todayList = mutableListOf<FinTransaction>()
        val yesterdayList = mutableListOf<FinTransaction>()
        val earlierList = mutableListOf<FinTransaction>()

        val sdf = java.text.SimpleDateFormat("dd.MM", java.util.Locale.getDefault())
        val todayStr = sdf.format(java.util.Date())
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        filteredTransactions.forEach { tx ->
            val datePart = tx.timestampFormatted
            when {
                datePart.startsWith("Сегодня") || datePart.startsWith(todayStr) -> todayList.add(tx)
                datePart.startsWith("Вчера") || datePart.startsWith(yesterdayStr) -> yesterdayList.add(tx)
                else -> earlierList.add(tx)
            }
        }

        val result = mutableListOf<Pair<String, List<FinTransaction>>>()
        if (todayList.isNotEmpty()) result.add("Сегодня" to todayList)
        if (yesterdayList.isNotEmpty()) result.add("Вчера" to yesterdayList)
        if (earlierList.isNotEmpty()) result.add("Ранее" to earlierList)
        result
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Карточка сводки баланса периода
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = BorderStroke(1.5.dp, currentTheme.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Движение монет в периоде ${state.currentPeriod}",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = currentTheme.primaryColor
                        )
                        Text(
                            text = "Все расходы на питомца, покупки в магазине и заработок",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f), thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Потрачено
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = DangerRedColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, DangerRedColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    Icons.Rounded.ArrowDownward,
                                    contentDescription = null,
                                    tint = DangerRedColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("Потрачено", fontSize = 10.5.sp, color = TextSecondary, maxLines = 1)
                            }
                            Text(
                                text = "-$totalSpent 🪙",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = DangerRedColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Заработано
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = StatGreenEmerald.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, StatGreenEmerald.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    Icons.Rounded.ArrowUpward,
                                    contentDescription = null,
                                    tint = StatGreenEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("Доход", fontSize = 10.5.sp, color = TextSecondary, maxLines = 1)
                            }
                            Text(
                                text = "+$totalEarned 🪙",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = StatGreenEmerald,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // В копилке
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = currentTheme.primaryColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    painter = painterResource(ru.finpet.app.R.drawable.ic_piggy_bank),
                                    contentDescription = null,
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("В копилке", fontSize = 10.5.sp, color = TextSecondary, maxLines = 1)
                            }
                            Text(
                                text = "${state.totalSavingsAmount} 🪙",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = currentTheme.primaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Фильтр категорий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "Все (${state.transactions.size})" to 0,
                "Траты (-$totalSpent)" to 1,
                "Доходы (+$totalEarned)" to 2
            )
            filters.forEach { (title, id) ->
                val isSelected = selectedFilter == id
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .bounceClick {
                            SoundHapticManager.performClickHaptic()
                            selectedFilter = id
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) currentTheme.primaryColor else SurfaceLight,
                    border = BorderStroke(1.dp, if (isSelected) currentTheme.primaryColor else OutlineLight)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            text = title,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Список операций
        if (filteredTransactions.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("", fontSize = 32.sp)
                        Text(
                            text = "Операций в этой категории пока нет",
                            fontFamily = UnboundedFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Кормление, покупки и награды за квесты отобразятся здесь",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedTransactions.forEach { (groupTitle, txList) ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = groupTitle.uppercase(),
                            fontFamily = UnboundedFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )

                        txList.forEach { tx ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceLight,
                                border = BorderStroke(1.dp, OutlineLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val txIconRes = if (tx.isIncome) {
                                            ru.finpet.app.R.drawable.ic_coin_vector
                                        } else {
                                            when (tx.envelope) {
                                                EnvelopeType.NEEDS -> ru.finpet.app.R.drawable.ic_wallet
                                                EnvelopeType.SAVINGS -> ru.finpet.app.R.drawable.ic_piggy_bank
                                                EnvelopeType.WANTS -> ru.finpet.app.R.drawable.ic_gift
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (tx.isIncome) FinGreenEmerald.copy(alpha = 0.12f) else currentTheme.primaryColor.copy(alpha = 0.10f),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(txIconRes),
                                                    contentDescription = null,
                                                    tint = if (tx.isIncome) Color.Unspecified else currentTheme.primaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = tx.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${tx.timestampFormatted} • ${tx.note}",
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = if (tx.isIncome) "+${tx.amount} 🪙" else "-${tx.amount} 🪙",
                                        fontFamily = UnboundedFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (tx.isIncome) FinGreenEmerald else BrandPinkNeon,
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
    }
}


@Composable
private fun BudgetLegend(title: String, ratio: Float, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
            Text(
                text = "${(ratio * 100).toInt()}%",
                fontSize = 11.sp,
                fontFamily = UnboundedFamily,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Стеклянная банка бюджета с визуализацией уровня наполнения
 */
@Composable
private fun GlassBudgetJar(
    title: String,
    targetPercent: Int,
    currentAmount: Int,
    ratio: Float,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Крышка банки
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .width(42.dp)
                .height(5.dp)
        ) {}

        // Корпус банки
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
            color = Color.White.copy(alpha = 0.9f),
            border = BorderStroke(1.5.dp, color.copy(alpha = 0.7f)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .width(68.dp)
                .height(84.dp)
        ) {
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
                // Заполнение монетами по высоте
                val fillFraction = ratio.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fillFraction)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.85f))
                            )
                        )
                )

                // Эмодзи и сумма
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                    Text(
                        text = "$currentAmount 🪙",
                        fontFamily = UnboundedFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            }
        }

        // Подпись под банкой
        Text(
            text = "$title $targetPercent%",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}


