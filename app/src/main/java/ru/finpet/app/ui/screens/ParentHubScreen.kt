package ru.finpet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.model.TaskStatus
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*

@Composable
fun ParentHubScreen(
    state: GameState,
    onUnlock: (Boolean) -> Unit,
    onApproveTask: (String) -> Unit,
    onAddNewTask: (title: String, reward: Int) -> Unit,
    onAwardDirectCoins: (amount: Int, reason: String) -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    onResetTestProfile: () -> Unit,
    onBack: () -> Unit = {}
) {
    val currentTheme = LocalAppTheme.current
    val coroutineScope = rememberCoroutineScope()
    var isUnlocked by remember { mutableStateOf(state.isParentUnlocked) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var currentPin by remember { mutableStateOf(GameRepository.getParentPin()) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAwardDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val completedQuestsCount = state.quests.count { it.isCompleted }

    if (!isUnlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Назад",
                    tint = currentTheme.primaryColor
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = SurfaceLight,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, currentTheme.cardOutlineLight)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = currentTheme.primaryColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "Кабинет родителя",
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Введите 4-значный PIN-код для входа\n(по умолчанию: 2026)",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    // 4 индикатора точек
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (isFilled) currentTheme.primaryColor else SurfaceVariantLight,
                                        shape = CircleShape
                                    )
                                    .then(
                                        if (!isFilled) Modifier.border(1.5.dp, OutlineLight, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }

                    if (pinError) {
                        Text(
                            text = "Неверный PIN-код. Попробуйте еще раз!",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Цифровая клавиатура 3x4
                    val pinButtons = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        pinButtons.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { key ->
                                    when (key) {
                                        "C" -> {
                                            Surface(
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .bounceClick {
                                                        SoundHapticManager.playClickSound()
                                                        SoundHapticManager.performClickHaptic()
                                                        enteredPin = ""
                                                        pinError = false
                                                    },
                                                shape = CircleShape,
                                                color = SurfaceVariantLight,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "C",
                                                        fontFamily = UnboundedFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                        "⌫" -> {
                                            Surface(
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .bounceClick {
                                                        if (enteredPin.isNotEmpty()) {
                                                            SoundHapticManager.playClickSound()
                                                            SoundHapticManager.performClickHaptic()
                                                            enteredPin = enteredPin.dropLast(1)
                                                            pinError = false
                                                        }
                                                    },
                                                shape = CircleShape,
                                                color = SurfaceVariantLight,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                                        contentDescription = "Стереть",
                                                        tint = TextPrimary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            Surface(
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .bounceClick {
                                                        if (enteredPin.length < 4) {
                                                            SoundHapticManager.playClickSound()
                                                            SoundHapticManager.performClickHaptic()
                                                            val nextPin = enteredPin + key
                                                            enteredPin = nextPin
                                                            pinError = false
                                                            if (nextPin.length == 4) {
                                                                if (nextPin == currentPin) {
                                                                    SoundHapticManager.playSuccessSound()
                                                                    SoundHapticManager.performSuccessHaptic()
                                                                    isUnlocked = true
                                                                    onUnlock(true)
                                                                    enteredPin = ""
                                                                } else {
                                                                    SoundHapticManager.performErrorHaptic()
                                                                    pinError = true
                                                                    coroutineScope.launch {
                                                                        delay(500)
                                                                        enteredPin = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                shape = CircleShape,
                                                color = SurfaceVariantLight,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = key,
                                                        fontFamily = UnboundedFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp,
                                                        color = TextPrimary
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
            }
        }
        return
    }

    // Раздел для взрослого открыт
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Шапка
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
                        text = "👨‍👩‍👧 Уголок родителя",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandVioletPrimary
                    )
                    Text(
                        text = "Игрок: ${state.playerName} • Питомец: ${state.pet.name}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            IconButton(onClick = { isUnlocked = false }) {
                Icon(Icons.Rounded.LockOpen, contentDescription = "Заблокировать", tint = BrandVioletPrimary)
            }
        }

        // Карточка успехов ребенка
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceLight,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandLavender.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📈 Прогресс ребенка в игре", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandVioletPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceSubtle
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Пройдено квестов:", fontSize = 11.sp, color = TextSecondary)
                            Text("$completedQuestsCount из ${state.quests.size}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrandVioletPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = BrandPinkPastel
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("В копилке на мечту:", fontSize = 11.sp, color = TextSecondary)
                            Text("${state.totalSavingsAmount} 🪙", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrandRoseWarm)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceVariantLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🌱 Стадия развития питомца: ${state.pet.evolutionStage.title}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandVioletPrimary)
                        Text(state.pet.evolutionStage.description, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Управление поощрениями за реальные дела
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceLight,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandLavender.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ Награда за дела по дому",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        color = BrandVioletPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showAddTaskDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceSubtle
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 30.dp).bounceClick()
                        ) {
                            Text("+ Дело", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                        Button(
                            onClick = { showAwardDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 30.dp).bounceClick()
                        ) {
                            Text("+ Бонус", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }

                state.parentTasks.forEach { task ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandLavender.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = task.icon,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(end = 2.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        softWrap = true,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+${task.rewardCoins} 🪙",
                                        fontSize = 12.sp,
                                        color = FinCoinGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (task.status == TaskStatus.COMPLETED) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FinGreenEmerald.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FinGreenEmerald.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "Начислено ✓",
                                        color = FinGreenEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        SoundHapticManager.playCoinSound()
                                        SoundHapticManager.performSuccessHaptic()
                                        onApproveTask(task.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatGreenEmerald,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 32.dp).bounceClick()
                                ) {
                                    Text(
                                        text = "Одобрить",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
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

        // Демо-режим и управление профилем
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Панель управления (Демо)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = true)) {
                        Text(
                            text = "Демонстрационный режим",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Мгновенный переход между периодами",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = state.isDemoMode,
                        onCheckedChange = {
                            SoundHapticManager.playClickSound()
                            SoundHapticManager.performClickHaptic()
                            onToggleDemoMode(it)
                        }
                    )
                }

                HorizontalDivider(color = OutlineLight, thickness = 1.dp)

                // Смена PIN-кода родительского контроля (Аудит п.4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = true)) {
                        Text(
                            text = "PIN-код доступа",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Защита входа в кабинет родителя",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Сменить PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = OutlineLight, thickness = 1.dp)

                // Кнопка сброса тестового профиля (строго фиксированная высота 46dp, лаконичный текст)
                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Сбросить профиль к началу",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Диалог добавления задания родителем
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskReward by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Новое задание для ребенка 📝", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandVioletPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Описание дела") },
                        placeholder = { Text("Например: Полить цветы, убрать игрушки") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskReward,
                        onValueChange = { taskReward = it },
                        label = { Text("Награда в монетах 🪙") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reward = taskReward.toIntOrNull() ?: 20
                        SoundHapticManager.playSuccessSound()
                        SoundHapticManager.performSuccessHaptic()
                        onAddNewTask(taskTitle, reward)
                        showAddTaskDialog = false
                    },
                    enabled = taskTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandButtonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Добавить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Отмена") }
            }
        )
    }

    // Диалог начисления монет родителем
    if (showAwardDialog) {
        var coinsText by remember { mutableStateOf("25") }
        var reasonText by remember { mutableStateOf("Помощь по дому") }

        AlertDialog(
            onDismissRequest = { showAwardDialog = false },
            title = { Text("Поощрение ребенка 🪙", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandVioletPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = coinsText,
                        onValueChange = { coinsText = it },
                        label = { Text("Количество монет") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("За что начислено") },
                        placeholder = { Text("Например: Уборка, чтение книги, отличная оценка") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = coinsText.toIntOrNull() ?: 20
                        SoundHapticManager.playCoinSound()
                        SoundHapticManager.performSuccessHaptic()
                        onAwardDirectCoins(amount, reasonText)
                        showAwardDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandButtonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Начислить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAwardDialog = false }) { Text("Отмена") }
            }
        )
    }

    // Диалог подтверждения сброса
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Сбросить данные профиля?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandVioletPrimary) },
            text = {
                Text(
                    "Локальная база данных будет очищена. Приложение вернется к Шагу 1 сценария (онбординг и создание питомца).",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetTestProfile()
                        showResetConfirmDialog = false
                        isUnlocked = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandButtonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Сбросить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Отмена") }
            }
        )
    }

    // Диалог смены PIN-кода (Аудит п.4)
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var changePinError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = {
                Text(
                    "Смена PIN-кода",
                    fontFamily = UnboundedFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Задайте новый 4-значный цифровой пароль для доступа к кабинету родителя.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                newPin = it
                                changePinError = null
                            }
                        },
                        label = { Text("Новый PIN (4 цифры)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    changePinError?.let {
                        Text(it, color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            GameRepository.setParentPin(newPin)
                            currentPin = newPin
                            showChangePinDialog = false
                            SoundHapticManager.performSuccessHaptic()
                        } else {
                            changePinError = "PIN-код должен состоять из 4 цифр"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor)
                ) {
                    Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
