package ru.finpet.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

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

                    // Кнопки пополнения и снятия произвольной суммы
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDepositDialog = true },
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
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Отложить 🪙",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { showWithdrawDialog = true },
                            enabled = activeGoal.currentAmount > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, OutlineLight),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandRoseWarm)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Снять 🪙",
                                fontSize = 12.sp,
                                color = BrandRoseWarm,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
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

    // Диалог пополнения цели на свою сумму
    if (showDepositDialog && activeGoal != null) {
        var depositInput by remember { mutableStateOf("20") }
        val depositVal = depositInput.toIntOrNull() ?: 0
        val maxAvailable = state.totalCoins
        val isValid = depositVal > 0 && depositVal <= maxAvailable

        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activeGoal.icon, fontSize = 22.sp)
                    Text("Пополнить копилку 🪙", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandVioletPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Цель: ${activeGoal.title}\nНакоплено: ${activeGoal.currentAmount} из ${activeGoal.targetAmount} 🪙",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Доступно в кошельке:", fontSize = 12.sp, color = TextSecondary)
                            Text("$maxAvailable 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatGreenEmerald)
                        }
                    }

                    OutlinedTextField(
                        value = depositInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                depositInput = newValue
                            }
                        },
                        label = { Text("Сумма пополнения") },
                        placeholder = { Text("Например: 35") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = depositVal > maxAvailable || (depositInput.isNotEmpty() && depositVal <= 0),
                        supportingText = {
                            if (depositVal > maxAvailable) {
                                Text("Недостаточно монет в кошельке", color = BrandRoseWarm, fontSize = 11.sp)
                            } else if (depositInput.isNotEmpty() && depositVal <= 0) {
                                Text("Введите сумму больше 0", color = BrandRoseWarm, fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Быстрый выбор сумм
                    Text("Быстрый выбор:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 20, 50, 100).forEach { preset ->
                            SuggestionChip(
                                onClick = { depositInput = preset.toString() },
                                label = { Text("+$preset 🪙", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        if (maxAvailable > 0) {
                            SuggestionChip(
                                onClick = { depositInput = maxAvailable.toString() },
                                label = { Text("Все ($maxAvailable)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValid) {
                            onDeposit(depositVal)
                            showDepositDialog = false
                        }
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandButtonPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(44.dp).bounceClick()
                ) {
                    Text(
                        text = if (depositVal > 0) "Внести $depositVal 🪙" else "Внести",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }, modifier = Modifier.height(44.dp)) {
                    Text("Отмена", color = TextSecondary, fontSize = 13.sp)
                }
            }
        )
    }

    // Диалог снятия из цели своей суммы
    if (showWithdrawDialog && activeGoal != null) {
        var withdrawInput by remember { mutableStateOf("15") }
        val withdrawVal = withdrawInput.toIntOrNull() ?: 0
        val maxInGoal = activeGoal.currentAmount
        val isValid = withdrawVal > 0 && withdrawVal <= maxInGoal

        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activeGoal.icon, fontSize = 22.sp)
                    Text("Снять из копилки 🪙", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandRoseWarm)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Цель: ${activeGoal.title}\nВ копилке накоплено: $maxInGoal 🪙",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Монеты вернутся в твой свободный кошелек, но прогресс цели уменьшится.",
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = withdrawInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                withdrawInput = newValue
                            }
                        },
                        label = { Text("Сколько монет снять") },
                        placeholder = { Text("Например: 15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = withdrawVal > maxInGoal || (withdrawInput.isNotEmpty() && withdrawVal <= 0),
                        supportingText = {
                            if (withdrawVal > maxInGoal) {
                                Text("В копилке всего $maxInGoal монет", color = BrandRoseWarm, fontSize = 11.sp)
                            } else if (withdrawInput.isNotEmpty() && withdrawVal <= 0) {
                                Text("Введите сумму больше 0", color = BrandRoseWarm, fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Быстрый выбор сумм
                    Text("Быстрый выбор:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 25, 50).filter { it <= maxInGoal }.forEach { preset ->
                            SuggestionChip(
                                onClick = { withdrawInput = preset.toString() },
                                label = { Text("$preset 🪙", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        if (maxInGoal > 0) {
                            SuggestionChip(
                                onClick = { withdrawInput = maxInGoal.toString() },
                                label = { Text("Всё ($maxInGoal)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValid) {
                            onRequestWithdraw(withdrawVal)
                            showWithdrawDialog = false
                        }
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRoseWarm,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(44.dp).bounceClick()
                ) {
                    Text(
                        text = if (withdrawVal > 0) "Снять $withdrawVal 🪙" else "Снять",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }, modifier = Modifier.height(44.dp)) {
                    Text("Отмена", color = TextSecondary, fontSize = 13.sp)
                }
            }
        )
    }
}
