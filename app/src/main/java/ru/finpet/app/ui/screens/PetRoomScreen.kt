package ru.finpet.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.model.RoomSlotType
import ru.finpet.app.model.ShopItem
import ru.finpet.app.model.WallAward
import ru.finpet.app.model.WallAwardsRepository
import ru.finpet.app.ui.components.BankDepositDialog
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.CoinBadge
import ru.finpet.app.ui.components.MemoryAlbumDialog
import ru.finpet.app.ui.components.PetAvatarView
import ru.finpet.app.ui.components.PetRoomSceneView
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetRoomScreen(
    state: GameState,
    onBack: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    val pet = state.pet
    val currentTheme = LocalAppTheme.current
    var showCustomizeSheet by remember { mutableStateOf(false) }
    var showAwardsDialog by remember { mutableStateOf(false) }
    var showMemoryAlbum by remember { mutableStateOf(false) }
    var showBankDeposit by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Комната",
                            fontFamily = UnboundedFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.isRoomLightOff) "🌙 Питомец сладко спит" else "Личное уютное пространство",
                            fontSize = 11.5.sp,
                            color = if (state.isRoomLightOff) BrandButtonPrimary else TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Назад",
                            tint = currentTheme.primaryColor
                        )
                    }
                },
                actions = {
                    // Переключатель процедурной Lo-Fi музыки
                    IconButton(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            GameRepository.toggleMusic()
                        }
                    ) {
                        Text(
                            text = if (state.isMusicEnabled) "🎵" else "🔇",
                            fontSize = 18.sp
                        )
                    }

                    // Кнопка банковского сейфа
                    IconButton(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            showBankDeposit = true
                        }
                    ) {
                        Text("🏦", fontSize = 18.sp)
                    }

                    CoinBadge(coins = state.totalCoins, modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 2.5D сцена комнаты питомца
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                PetRoomSceneView(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 0f,
                    showDayNightBadge = true,
                    onAwardsClick = {
                        SoundHapticManager.performClickHaptic()
                        showAwardsDialog = true
                    },
                    onLampClick = {
                        GameRepository.toggleRoomLight()
                    },
                    onWindowClick = {
                        GameRepository.cycleRoomWeather()
                    }
                ) {
                    // Интерактивный питомец по центру комнаты
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        PetAvatarView(
                            pet = pet,
                            modifier = Modifier.size(126.dp),
                            onPetClick = {
                                GameRepository.petThePet()
                                SoundHapticManager.performPetHaptic()
                            }
                        )
                    }
                }
            }

            // Быстрые интерактивные действия комнаты (Лампа, Погода, Альбом, Вклад)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Лампа / Режим сна
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.isRoomLightOff) Color(0xFF1E293B) else SurfaceLight,
                    border = BorderStroke(1.dp, if (state.isRoomLightOff) FinGoldAccent else OutlineLight),
                    modifier = Modifier.bounceClick {
                        GameRepository.toggleRoomLight()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(if (state.isRoomLightOff) "💡" else "🛋️", fontSize = 14.sp)
                        Text(
                            text = if (state.isRoomLightOff) "Включить свет" else "Выключить свет",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isRoomLightOff) FinGoldAccent else TextPrimary
                        )
                    }
                }

                // Смена погоды за окном
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.bounceClick {
                        GameRepository.cycleRoomWeather()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(state.roomWeather.icon, fontSize = 14.sp)
                        Text(
                            text = state.roomWeather.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Банковский вклад под сложный процент
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.hasActiveBankDeposit) StatGreenEmerald.copy(alpha = 0.12f) else SurfaceLight,
                    border = BorderStroke(1.dp, if (state.hasActiveBankDeposit) StatGreenEmerald else OutlineLight),
                    modifier = Modifier.bounceClick {
                        SoundHapticManager.performClickHaptic()
                        showBankDeposit = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🏦", fontSize = 14.sp)
                        Text(
                            text = if (state.hasActiveBankDeposit) "Вклад: ${state.bankDepositAmount} 🪙" else "Вклад под +20%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.hasActiveBankDeposit) StatGreenEmerald else TextPrimary
                        )
                    }
                }
            }

            val allAwards = remember { WallAwardsRepository.allAwards }
            val unlockedAwardsCount = remember(state) { allAwards.count { it.isUnlocked(state) } }

            // Интерактивная плашка стены наград
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .bounceClick {
                        SoundHapticManager.performClickHaptic()
                        showAwardsDialog = true
                    },
                shape = RoundedCornerShape(14.dp),
                color = currentTheme.primaryColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.22f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📜", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Стена наград ($unlockedAwardsCount / ${allAwards.size})",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "На стене висят твои дипломы и грамоты! Нажми, чтобы рассмотреть.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "Открыть",
                        tint = currentTheme.primaryColor
                    )
                }
            }

            // Блок статуса обустройства
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.5.dp, currentTheme.borderColor),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = currentTheme.bubbleBg,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Home,
                                        contentDescription = null,
                                        tint = currentTheme.primaryColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Уют дома: ${state.ownedRoomItemIds.size * 10}%",
                                    fontFamily = UnboundedFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Куплено предметов: ${state.ownedRoomItemIds.size}",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Чип настроения
                    }

                    // Описание интерактива
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentTheme.bubbleBg)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Интерактивная комната питомца:",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "• Нажимай на питомца для общения и веселья",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• Свежие обои и мягкие полы создают уют и атмосферу",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "• Настенные дипломы и грамоты отмечают твои финансовые победы",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Мультяшные кнопки управления комнатой
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CartoonButton(
                    text = "🏆 Стена наград ($unlockedAwardsCount / ${allAwards.size})",
                    onClick = {
                        SoundHapticManager.performClickHaptic()
                        showAwardsDialog = true
                    },
                    containerColor = Color(0xFFD97706),
                    modifier = Modifier.fillMaxWidth()
                )

                CartoonButton(
                    text = "Обустроить комнату 🎨",
                    onClick = { showCustomizeSheet = true },
                    containerColor = currentTheme.primaryColor,
                    modifier = Modifier.fillMaxWidth()
                )

                CartoonButton(
                    text = "Купить новинки в магазине 🏪",
                    onClick = onNavigateToShop,
                    containerColor = Color(0xFF27272A),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Модальное окно кастомизации комнаты (Обустроить комнату)
    if (showCustomizeSheet) {
        RoomCustomizerDialog(
            state = state,
            onDismiss = { showCustomizeSheet = false },
            onEquip = { itemId, slot ->
                GameRepository.equipRoomItem(itemId, slot)
            },
            onUnequip = { slot ->
                GameRepository.unequipRoomItem(slot)
            },
            onNavigateToShop = {
                showCustomizeSheet = false
                onNavigateToShop()
            }
        )
    }

    // Модальное окно «Стена наград» (официальные грамоты и дипломы)
    if (showAwardsDialog) {
        WallAwardsDialog(
            state = state,
            onDismiss = { showAwardsDialog = false }
        )
    }

    // Фотоальбом воспоминаний Polaroid
    if (showMemoryAlbum) {
        MemoryAlbumDialog(
            state = state,
            onDismiss = { showMemoryAlbum = false }
        )
    }

    // Банковский сейф (вклад под сложный процент)
    if (showBankDeposit) {
        BankDepositDialog(
            state = state,
            onDismiss = { showBankDeposit = false }
        )
    }
}

@Composable
private fun RoomCustomizerDialog(
    state: GameState,
    onDismiss: () -> Unit,
    onEquip: (String, RoomSlotType) -> Unit,
    onUnequip: (RoomSlotType) -> Unit,
    onNavigateToShop: () -> Unit
) {
    val currentTheme = LocalAppTheme.current
    val dialogTabs = remember {
        listOf(
            RoomSlotType.WALLPAPER to "Обои 🎨",
            RoomSlotType.FLOOR to "Пол 🪵",
            RoomSlotType.POSTER to "Постеры 🖼️"
        )
    }
    var selectedTab by remember { mutableStateOf(RoomSlotType.WALLPAPER) }

    val slotItems = remember(state.shopItems, selectedTab) {
        state.shopItems.filter { it.roomSlotType == selectedTab }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Обустройство комнаты",
                    fontFamily = UnboundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Закрыть", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Переключатель слотов (горизонтальный скролл)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dialogTabs.forEach { (slot, title) ->
                        val isSelected = selectedTab == slot
                        Surface(
                            modifier = Modifier.bounceClick { selectedTab = slot },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) currentTheme.primaryColor else SurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSelected) currentTheme.primaryColor else OutlineLight)
                        ) {
                            Text(
                                text = title,
                                fontFamily = UnboundedFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Сетка предметов выбранного слота
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (slotItems.isEmpty()) {
                        Text(
                            text = "В этой категории пока нет предметов",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        slotItems.forEach { item ->
                            val isOwned = state.ownedRoomItemIds.contains(item.id) || item.price == 0
                            val isEquipped = when (selectedTab) {
                                RoomSlotType.WALLPAPER -> state.equippedWallpaper == item.id
                                RoomSlotType.FLOOR -> state.equippedFloor == item.id
                                RoomSlotType.POSTER -> state.equippedPoster == item.id
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isEquipped) currentTheme.bubbleBg else SurfaceLight,
                                border = BorderStroke(
                                    width = if (isEquipped) 2.dp else 1.dp,
                                    color = if (isEquipped) currentTheme.primaryColor else OutlineLight
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Левая часть (иконка + название + подсказка)
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (item.iconRes != null) {
                                            Icon(
                                                painter = painterResource(item.iconRes),
                                                contentDescription = item.name,
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Text(item.icon, fontSize = 24.sp)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontFamily = UnboundedFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (item.tip.isNotBlank()) {
                                                Text(
                                                    text = item.tip,
                                                    fontSize = 9.5.sp,
                                                    color = Color(0xFF64748B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = if (isEquipped) "Экипировано ✓" else if (isOwned) "В инвентаре" else "Не куплено (${item.price} 🪙)",
                                                fontSize = 10.sp,
                                                color = if (isEquipped) currentTheme.primaryColor else TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Кнопка действия (Надеть / Снять / В магазин)
                                    if (isOwned) {
                                        Button(
                                            onClick = {
                                                SoundHapticManager.performClickHaptic()
                                                if (isEquipped) {
                                                    onUnequip(selectedTab)
                                                } else {
                                                    onEquip(item.id, selectedTab)
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEquipped) Color(0xFFDC2626) else currentTheme.primaryColor
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                            modifier = Modifier
                                                .height(32.dp)
                                                .defaultMinSize(minWidth = 72.dp)
                                        ) {
                                            Text(
                                                text = if (isEquipped) "Снять" else "Надеть",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = onNavigateToShop,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F46)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                            modifier = Modifier
                                                .height(32.dp)
                                                .defaultMinSize(minWidth = 78.dp)
                                        ) {
                                            Text("В магазин", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            CartoonButton(
                text = "Готово ✨",
                onClick = onDismiss,
                containerColor = currentTheme.primaryColor,
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = SurfaceLight
    )
}

@Composable
private fun WallAwardsDialog(
    state: GameState,
    onDismiss: () -> Unit
) {
    val currentTheme = LocalAppTheme.current
    val allAwards = remember { WallAwardsRepository.allAwards }
    val unlockedCount = remember(state) { allAwards.count { it.isUnlocked(state) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Стена наград",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$unlockedCount / ${allAwards.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Официальные дипломы и грамоты на стене",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Закрыть", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                allAwards.forEach { award ->
                    val isUnlocked = award.isUnlocked(state)
                    val ribbonColor = Color(award.ribbonColorHex)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isUnlocked) SurfaceLight else SurfaceSubtle,
                        border = BorderStroke(
                            width = if (isUnlocked) 1.5.dp else 1.dp,
                            color = if (isUnlocked) ribbonColor.copy(alpha = 0.7f) else OutlineLight
                        ),
                        shadowElevation = if (isUnlocked) 2.dp else 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Верхняя строка грамоты: иконка + заголовок + статус
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = award.emoji,
                                        fontSize = 26.sp
                                    )
                                    Column {
                                        Text(
                                            text = award.diplomaName,
                                            fontFamily = UnboundedFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isUnlocked) TextPrimary else TextSecondary
                                        )
                                        Text(
                                            text = award.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isUnlocked) ribbonColor else TextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUnlocked) Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0x2064748B),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isUnlocked) Color(0xFF16A34A).copy(alpha = 0.4f) else Color(0x3064748B)
                                    )
                                ) {
                                    Text(
                                        text = if (isUnlocked) "На стене ✓" else "В процессе 🔒",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color(0xFF16A34A) else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Описание грамоты
                            Text(
                                text = award.description,
                                fontSize = 11.5.sp,
                                color = if (isUnlocked) TextPrimary else TextSecondary,
                                lineHeight = 16.sp
                            )

                            // Условие получения
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUnlocked) ribbonColor.copy(alpha = 0.08f) else SurfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(if (isUnlocked) "🎖️" else "🎯", fontSize = 12.sp)
                                    Text(
                                        text = if (isUnlocked) "Награда заслужена и повешена на стену!" else "Цель: ${award.conditionDescription}",
                                        fontSize = 10.5.sp,
                                        color = if (isUnlocked) ribbonColor else TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            CartoonButton(
                text = "Отлично! ✨",
                onClick = onDismiss,
                containerColor = currentTheme.primaryColor,
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = SurfaceLight
    )
}
