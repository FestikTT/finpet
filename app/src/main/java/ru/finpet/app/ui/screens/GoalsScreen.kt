package ru.finpet.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.data.GameState
import ru.finpet.app.model.FinancialGoal
import ru.finpet.app.ui.components.BalanceChips
import ru.finpet.app.ui.components.CoinBadge
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*

@Composable
fun GoalsScreen(
    state: GameState,
    onSelectGoal: (String) -> Unit,
    onDeposit: (Int) -> Unit,
    onRequestWithdraw: (Int) -> Unit,
    onCreateGoal: (title: String, targetAmount: Int, icon: String) -> Unit,
    onBack: () -> Unit = {}
) {
    val activeGoal = state.activeGoal
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Шапка экрана
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Назад",
                        tint = BrandVioletPrimary
                    )
                }
                Column {
                    Text(
                        text = "Копилка и цели",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Маленькие шаги ведут к большой мечте!",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Карточка активной цели (в стиле аналитической карточки из ЕИС)
        if (activeGoal != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "АКТИВНАЯ ЦЕЛЬ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        if (activeGoal.isAchieved) {
                            Surface(shape = RoundedCornerShape(6.dp), color = SurfaceSubtle) {
                                Text(
                                    "Достигнуто! ✓",
                                    color = FinGreenEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = activeGoal.icon, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                activeGoal.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                "Накоплено: ${activeGoal.currentAmount} из ${activeGoal.targetAmount} монет",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            "${(activeGoal.progress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BrandVioletPrimary
                        )
                    }

                    val animGoalProgress by animateFloatAsState(
                        targetValue = activeGoal.progress,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        label = "activeGoalProgress"
                    )

                    LinearProgressIndicator(
                        progress = { animGoalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BrandVioletPrimary,
                        trackColor = SurfaceVariantLight
                    )

                    // Расчет срока
                    val estimatedPeriods = activeGoal.calculateEstimatedPeriods(avgSavingsPerPeriod = 25)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (activeGoal.isAchieved) "Поздравляем! Цель достигнута!" else "Достижение цели: через $estimatedPeriods период(а) при откладывании по 25 монет",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Кнопки пополнения и снятия (строго фиксированная высота 42dp, однострочный текст)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onDeposit(20) },
                            enabled = state.totalCoins > 0 && !activeGoal.isAchieved,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandButtonPrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Отложить +20 🪙",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { onRequestWithdraw(15) },
                            enabled = activeGoal.currentAmount > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, OutlineLight),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Снять 15 🪙",
                                fontSize = 11.5.sp,
                                color = BrandRoseWarm,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Список всех целей
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Доступные цели:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(
                onClick = { showCreateDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandVioletPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Своя цель", color = BrandVioletPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.goals) { goal ->
                    val isSelected = goal.id == state.activeGoalId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectGoal(goal.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SurfaceSubtle else SurfaceLight,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BrandVioletPrimary) else androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(goal.icon, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    goal.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text("Цель: ${goal.targetAmount} монет • В копилке: ${goal.currentAmount} монет", fontSize = 11.sp, color = TextSecondary)
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = "Выбрано", tint = BrandVioletPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                OutlinedButton(
                                    onClick = { onSelectGoal(goal.id) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                                ) {
                                    Text("Выбрать", fontSize = 11.sp, color = BrandVioletPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог создания своей цели
    if (showCreateDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newAmount by remember { mutableStateOf("120") }
        var selectedIcon by remember { mutableStateOf("🚲") }
        val iconsList = listOf("🚲", "🎮", "🎸", "🎪", "📚", "🎨", "🛹", "🤖")

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Создать свою цель мечты 🎯", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandVioletPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Название цели") },
                        placeholder = { Text("Например: Новый велосипед") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = { newAmount = it },
                        label = { Text("Стоимость в монетах 🪙") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Выбери значок:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconsList.forEach { ic ->
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .bounceClick { selectedIcon = ic },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedIcon == ic) BrandPinkPastel else SurfaceVariantLight,
                                border = if (selectedIcon == ic) androidx.compose.foundation.BorderStroke(1.5.dp, BrandButtonPrimary) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text(ic, fontSize = 18.sp) }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newAmount.toIntOrNull() ?: 100
                        onCreateGoal(newTitle, amount, selectedIcon)
                        showCreateDialog = false
                    },
                    enabled = newTitle.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp).bounceClick(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandButtonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Создать цель",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }, modifier = Modifier.height(46.dp)) {
                    Text(
                        text = "Отмена",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        )
    }
}
