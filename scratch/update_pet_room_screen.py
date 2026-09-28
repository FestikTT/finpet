import os

code = '''package ru.finpet.app.ui.screens

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
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.CoinBadge
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

    // Интерактивный отклик при кормлении и игре
    var showFeedFeedback by remember { mutableStateOf(false) }
    var showPlayFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showFeedFeedback) {
        if (showFeedFeedback) {
            kotlinx.coroutines.delay(1600)
            showFeedFeedback = false
        }
    }

    LaunchedEffect(showPlayFeedback) {
        if (showPlayFeedback) {
            kotlinx.coroutines.delay(1600)
            showPlayFeedback = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Комната ${pet.name.ifBlank { "питомца" }}",
                            fontFamily = UnboundedFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Личное уютное пространство",
                            fontSize = 11.5.sp,
                            color = TextSecondary
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2.5D псевдо-изометрическая сцена комнаты
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                PetRoomSceneView(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 0f,
                    showDayNightBadge = true
                ) {
                    // Интерактивная миска с кормом (Слева от питомца)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnimatedVisibility(
                            visible = showFeedFeedback,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { -it }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFF22C55E))
                            ) {
                                Text(
                                    text = "+Сытость! 🥕",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(50.dp)
                                .bounceClick {
                                    GameRepository.feedPetQuick()
                                    SoundHapticManager.performSuccessHaptic()
                                    showFeedFeedback = true
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🥣", fontSize = 26.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xEEFFFFFF),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Кормить (10🪙)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Интерактивный мячик/игрушка (Справа от питомца)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnimatedVisibility(
                            visible = showPlayFeedback,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { -it }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEDE9FE),
                                border = BorderStroke(1.dp, Color(0xFF8B5CF6))
                            ) {
                                Text(
                                    text = "+Радость! ✨",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6D28D9),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(2.dp, Color(0xFF10B981)),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(50.dp)
                                .bounceClick {
                                    GameRepository.playWithPet()
                                    SoundHapticManager.performSuccessHaptic()
                                    showPlayFeedback = true
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎾", fontSize = 26.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xEEFFFFFF),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Играть",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

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
                                SoundHapticManager.performPetHaptic()
                            }
                        )
                    }
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
                Row(
                    modifier = Modifier.padding(14.dp),
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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentTheme.primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = pet.currentMood.title,
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = currentTheme.primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                    text = "Обустроить интерьер 🛠️",
                    onClick = { showCustomizeSheet = true },
                    containerColor = currentTheme.primaryColor,
                    modifier = Modifier.fillMaxWidth()
                )

                CartoonButton(
                    text = "Купить мебель в магазине 🏪",
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
            onNavigateToShop = {
                showCustomizeSheet = false
                onNavigateToShop()
            }
        )
    }
}

@Composable
private fun RoomCustomizerDialog(
    state: GameState,
    onDismiss: () -> Unit,
    onEquip: (String, RoomSlotType) -> Unit,
    onNavigateToShop: () -> Unit
) {
    val currentTheme = LocalAppTheme.current
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
                    text = "Интерьер комнаты",
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
                    RoomSlotType.values().forEach { slot ->
                        val isSelected = selectedTab == slot
                        Surface(
                            modifier = Modifier.bounceClick { selectedTab = slot },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) currentTheme.primaryColor else SurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSelected) currentTheme.primaryColor else OutlineLight)
                        ) {
                            Text(
                                text = slot.title,
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
                                RoomSlotType.WARDROBE -> state.equippedWardrobe == item.id
                                RoomSlotType.DESK -> state.equippedDesk == item.id
                                RoomSlotType.BED -> state.equippedBed == item.id
                                RoomSlotType.LAMP -> state.equippedLamp == item.id
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
                                    // Левая часть (иконка + название) с weight(1f) для защиты кнопки справа от сдавливания
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
                                            Text(
                                                text = if (isEquipped) "Экипировано ✓" else if (isOwned) "В инвентаре" else "Не куплено (${item.price} 🪙)",
                                                fontSize = 10.5.sp,
                                                color = if (isEquipped) currentTheme.primaryColor else TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Кнопка действия с фиксированной минимальной шириной (не сжимается)
                                    if (isOwned) {
                                        Button(
                                            onClick = {
                                                SoundHapticManager.performClickHaptic()
                                                onEquip(item.id, selectedTab)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEquipped) StatGreenEmerald else currentTheme.primaryColor
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            modifier = Modifier
                                                .height(32.dp)
                                                .defaultMinSize(minWidth = 72.dp)
                                        ) {
                                            Text(
                                                text = if (isEquipped) "Надето" else "Надеть",
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
'''

with open(r'C:\Users\festik\.gemini\antigravity\scratch\finpetrustore\app\src\main\java\ru\finpet\app\ui\screens\PetRoomScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("PetRoomScreen.kt updated successfully!")
