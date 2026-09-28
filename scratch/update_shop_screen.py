import os

code = '''package ru.finpet.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.app.R
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.model.RoomSlotType
import ru.finpet.app.model.ShopCategory
import ru.finpet.app.model.ShopItem
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.PetAvatarView
import ru.finpet.app.ui.components.PetRoomSceneView
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.components.shake
import ru.finpet.app.ui.theme.*
import ru.finpet.app.util.FormatUtils
import java.util.Calendar
import java.util.Random

/**
 * Категории товаров рынка (с чистыми текстовыми названиями, без голых эмодзи):
 */
enum class MarketCategory(val title: String) {
    ALL("Все"),
    FOOD("Еда"),
    HYGIENE("Уход"),
    ACCESSORIES("Одежда"),
    FURNITURE("Мебель"),
    WALLPAPER("Интерьер"),
    DAILY_SPECIALS("Спецпредложения дня")
}

/**
 * Режимы экрана магазина:
 * 0 - Рынок (Покупка новых предметов со скидками и примеркой)
 * 1 - Гардероб / Склад (Инвентарь купленных вещей и экипировка)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    state: GameState,
    onBuyItem: (ShopItem) -> Unit,
    onClose: () -> Unit = {}
) {
    val currentTheme = LocalAppTheme.current
    val coroutineScope = rememberCoroutineScope()

    var mainTabMode by remember { mutableIntStateOf(0) } // 0: Рынок, 1: Гардероб
    var selectedCategory by remember { mutableStateOf(MarketCategory.ALL) }
    var wardrobeCategory by remember { mutableStateOf<RoomSlotType?>(null) } // null: Все

    var tryOnItem by remember { mutableStateOf<ShopItem?>(null) }
    var insufficientCoinsShakeTrigger by remember { mutableIntStateOf(0) }
    val coinBadgeRedFlash = remember { Animatable(0f) }

    // 24-часовой таймер спецпредложений
    val calendar = remember { Calendar.getInstance() }
    val currentHour = remember { calendar.get(Calendar.HOUR_OF_DAY) }
    val currentMinute = remember { calendar.get(Calendar.MINUTE) }
    val hoursRemaining = 23 - currentHour
    val minutesRemaining = 59 - currentMinute

    // 6-часовой цикл ротации скидок
    val sixHoursMs = 6 * 3600 * 1000L
    val timeBlock = remember { System.currentTimeMillis() / sixHoursMs }

    // Каталог с детерминированными скидками
    val catalogWithDiscounts = remember(timeBlock, state.shopItems) {
        val rnd = Random(timeBlock)
        state.shopItems.mapIndexed { index, item ->
            val hasDiscount = ((index + timeBlock) % 3L == 0L)
            val discount = if (hasDiscount) {
                when (rnd.nextInt(3)) {
                    0 -> 15
                    1 -> 20
                    else -> 25
                }
            } else 0
            item.copy(discountPercent = discount)
        }
    }

    // Фильтрация товаров рынка
    val marketItems = remember(selectedCategory, catalogWithDiscounts, state.purchasedItemIds, state.ownedRoomItemIds) {
        val unownedCatalog = catalogWithDiscounts.filter { item ->
            !state.purchasedItemIds.contains(item.id) && !state.ownedRoomItemIds.contains(item.id)
        }
        when (selectedCategory) {
            MarketCategory.ALL -> unownedCatalog
            MarketCategory.FOOD -> unownedCatalog.filter { it.category == ShopCategory.FOOD }
            MarketCategory.HYGIENE -> unownedCatalog.filter { it.category == ShopCategory.HYGIENE }
            MarketCategory.ACCESSORIES -> unownedCatalog.filter { it.category == ShopCategory.ACCESSORIES }
            MarketCategory.FURNITURE -> unownedCatalog.filter { it.category == ShopCategory.FURNITURE }
            MarketCategory.WALLPAPER -> unownedCatalog.filter { it.category == ShopCategory.WALLPAPER }
            MarketCategory.DAILY_SPECIALS -> unownedCatalog.filter { it.discountPercent > 0 }
        }
    }

    // Фильтрация гардероба / инвентаря
    val allOwnedItems = remember(state.shopItems, state.purchasedItemIds, state.ownedRoomItemIds) {
        state.shopItems.filter { item ->
            state.purchasedItemIds.contains(item.id) || state.ownedRoomItemIds.contains(item.id)
        }
    }

    val wardrobeItems = remember(allOwnedItems, wardrobeCategory) {
        if (wardrobeCategory == null) {
            allOwnedItems
        } else {
            allOwnedItems.filter { it.roomSlotType == wardrobeCategory }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Верхний тулбар: Заголовок и баланс монет
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mainTabMode == 0) "Рынок" else "Гардероб",
                    fontFamily = UnboundedFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                // Баланс монет с анимацией тряски при нехватке
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .shake(insufficientCoinsShakeTrigger, 6f)
                        .background(
                            color = if (coinBadgeRedFlash.value > 0f) DangerRed.copy(alpha = 0.22f * coinBadgeRedFlash.value) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_coin_vector),
                        contentDescription = null,
                        tint = if (coinBadgeRedFlash.value > 0f) DangerRed else FinCoinGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = FormatUtils.formatCoins(state.totalCoins),
                        fontFamily = UnboundedFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = if (coinBadgeRedFlash.value > 0f) DangerRed else TextPrimary
                    )
                }
            }

            // Переключатель верхних вкладок: «Рынок» / «Гардероб и склад»
            Surface(
                shape = RoundedCornerShape(14.dp),
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
                                mainTabMode = 0
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (mainTabMode == 0) currentTheme.primaryColor else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Рынок 🏪",
                                fontFamily = UnboundedFamily,
                                fontWeight = if (mainTabMode == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = if (mainTabMode == 0) Color.White else TextPrimary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .bounceClick {
                                SoundHapticManager.performClickHaptic()
                                mainTabMode = 1
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (mainTabMode == 1) currentTheme.primaryColor else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Гардероб / Склад 👗",
                                fontFamily = UnboundedFamily,
                                fontWeight = if (mainTabMode == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = if (mainTabMode == 1) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            if (mainTabMode == 0) {
                // =========================================================================
                // ВКЛАДКА «РЫНОК»: Текстовые категории, баннер спецпредложений и витрина
                // =========================================================================

                // Горизонтальный скролл текстовых категорий
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarketCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) currentTheme.primaryColor else SurfaceLight,
                            border = BorderStroke(1.dp, if (isSelected) currentTheme.primaryColor else OutlineLight),
                            modifier = Modifier.bounceClick {
                                SoundHapticManager.performClickHaptic()
                                selectedCategory = cat
                            }
                        ) {
                            Text(
                                text = cat.title,
                                fontFamily = UnboundedFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Баннер спецпредложений дня с таймером (если выбрана эта категория или при наличии скидок)
                if (selectedCategory == MarketCategory.DAILY_SPECIALS || selectedCategory == MarketCategory.ALL) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🔥", fontSize = 15.sp)
                                Text(
                                    text = "Скидки дня до -25%",
                                    fontFamily = UnboundedFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Text(
                                text = "Смена через: ${hoursRemaining}ч ${minutesRemaining}м",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                // Витрина: Сетка карточек товаров
                Box(modifier = Modifier.weight(1f)) {
                    if (marketItems.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎉", fontSize = 36.sp)
                                Text(
                                    text = "Все товары в этой категории уже куплены!",
                                    fontFamily = UnboundedFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(marketItems, key = { it.id }) { item ->
                                MarketItemCard(
                                    item = item,
                                    onItemClick = {
                                        SoundHapticManager.performClickHaptic()
                                        tryOnItem = item
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // =========================================================================
                // ВКЛАДКА «ГАРДЕРОБ / СКЛАД»: Купленные предметы с возможностью надеть
                // =========================================================================

                // Фильтры гардероба
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterAll = wardrobeCategory == null
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (filterAll) currentTheme.primaryColor else SurfaceLight,
                        border = BorderStroke(1.dp, if (filterAll) currentTheme.primaryColor else OutlineLight),
                        modifier = Modifier.bounceClick { wardrobeCategory = null }
                    ) {
                        Text(
                            text = "Все (${allOwnedItems.size})",
                            fontFamily = UnboundedFamily,
                            fontWeight = if (filterAll) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = if (filterAll) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    RoomSlotType.values().forEach { slot ->
                        val isSel = wardrobeCategory == slot
                        val count = allOwnedItems.count { it.roomSlotType == slot }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) currentTheme.primaryColor else SurfaceLight,
                            border = BorderStroke(1.dp, if (isSel) currentTheme.primaryColor else OutlineLight),
                            modifier = Modifier.bounceClick { wardrobeCategory = slot }
                        ) {
                            Text(
                                text = "${slot.title} ($count)",
                                fontFamily = UnboundedFamily,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (isSel) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Список купленных предметов
                Box(modifier = Modifier.weight(1f)) {
                    if (wardrobeItems.isEmpty()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "В этой категории пока нет купленных предметов",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(wardrobeItems, key = { it.id }) { item ->
                                val isEquipped = when (item.roomSlotType) {
                                    RoomSlotType.WALLPAPER -> state.equippedWallpaper == item.id
                                    RoomSlotType.FLOOR -> state.equippedFloor == item.id
                                    RoomSlotType.WARDROBE -> state.equippedWardrobe == item.id
                                    RoomSlotType.DESK -> state.equippedDesk == item.id
                                    RoomSlotType.BED -> state.equippedBed == item.id
                                    RoomSlotType.LAMP -> state.equippedLamp == item.id
                                    RoomSlotType.POSTER -> state.equippedPoster == item.id
                                    null -> false
                                }

                                WardrobeItemCard(
                                    item = item,
                                    isEquipped = isEquipped,
                                    onEquip = {
                                        SoundHapticManager.performClickHaptic()
                                        if (item.roomSlotType != null) {
                                            GameRepository.equipRoomItem(item.id, item.roomSlotType)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // Шторка интерактивной примерки (Live Try-On Bottom Sheet)
        // =========================================================================
        tryOnItem?.let { item ->
            val canAfford = state.totalCoins >= item.price
            val neededCoins = (item.price - state.totalCoins).coerceAtLeast(0)

            ModalBottomSheet(
                onDismissRequest = { tryOnItem = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Заголовок шторки примерки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Примерка товара",
                            fontFamily = UnboundedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )

                        IconButton(onClick = { tryOnItem = null }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Закрыть", tint = TextSecondary)
                        }
                    }

                    // Интерактивная зона предпросмотра на питомце
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.5.dp, currentTheme.cardOutlineLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            // Если примеряем предмет интерьера — показываем мини-комнату с этим предметом
                            if (item.roomSlotType != null) {
                                val previewState = remember(state, item) {
                                    when (item.roomSlotType) {
                                        RoomSlotType.WALLPAPER -> state.copy(equippedWallpaper = item.id)
                                        RoomSlotType.FLOOR -> state.copy(equippedFloor = item.id)
                                        RoomSlotType.WARDROBE -> state.copy(equippedWardrobe = item.id)
                                        RoomSlotType.DESK -> state.copy(equippedDesk = item.id)
                                        RoomSlotType.BED -> state.copy(equippedBed = item.id)
                                        RoomSlotType.LAMP -> state.copy(equippedLamp = item.id)
                                        RoomSlotType.POSTER -> state.copy(equippedPoster = item.id)
                                    }
                                }

                                PetRoomSceneView(
                                    state = previewState,
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 18f,
                                    showDayNightBadge = false
                                ) {
                                    PetAvatarView(
                                        pet = state.pet,
                                        modifier = Modifier
                                            .size(90.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                            } else {
                                // Примерка еды или аксессуара на питомце
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PetAvatarView(
                                        pet = state.pet,
                                        modifier = Modifier.size(95.dp)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xDD000000),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (item.iconRes != null) {
                                                Icon(
                                                    painter = painterResource(item.iconRes),
                                                    contentDescription = null,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(item.icon, fontSize = 14.sp)
                                            }
                                            Text(
                                                text = item.name,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Название и описание товара
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
                                text = item.name,
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )

                            if (item.discountPercent > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DangerRed
                                ) {
                                    Text(
                                        text = "-${item.discountPercent}% скидка",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (item.tip.isNotEmpty()) {
                            Text(
                                text = item.tip,
                                fontSize = 12.5.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Бусты параметров
                    if (item.hungerBoost > 0f || item.happinessBoost > 0f || item.careBoost > 0f || item.energyBoost > 0f) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (item.hungerBoost > 0f) {
                                Surface(shape = RoundedCornerShape(8.dp), color = StatGreenEmerald.copy(alpha = 0.12f)) {
                                    Text(
                                        text = "Сытость +${(item.hungerBoost * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatGreenEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (item.happinessBoost > 0f) {
                                Surface(shape = RoundedCornerShape(8.dp), color = BrandLavender.copy(alpha = 0.12f)) {
                                    Text(
                                        text = "Радость +${(item.happinessBoost * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandLavender,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (item.careBoost > 0f) {
                                Surface(shape = RoundedCornerShape(8.dp), color = ElectricCobaltPrimary.copy(alpha = 0.12f)) {
                                    Text(
                                        text = "Уход +${(item.careBoost * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCobaltPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineLight)

                    // Секция цены и кнопка покупки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Стоимость:", fontSize = 11.5.sp, color = TextSecondary)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (item.discountPercent > 0) {
                                    Text(
                                        text = "${item.basePrice}",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                                Icon(
                                    painter = painterResource(R.drawable.ic_coin_vector),
                                    contentDescription = null,
                                    tint = FinCoinGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = FormatUtils.formatCoins(item.price),
                                    fontFamily = UnboundedFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = currentTheme.primaryColor
                                )
                            }
                        }

                        if (canAfford) {
                            CartoonButton(
                                text = "Купить за ${item.price} 🪙",
                                onClick = {
                                    SoundHapticManager.performHeavyClickHaptic()
                                    // Немедленно закрываем шторку примерки, чтобы распаковка открылась без наложения модалок
                                    val toBuy = item
                                    tryOnItem = null
                                    onBuyItem(toBuy)
                                },
                                containerColor = currentTheme.primaryColor,
                                height = 44.dp,
                                fontSize = 12.5.sp,
                                modifier = Modifier.width(170.dp)
                            )
                        } else {
                            CartoonButton(
                                text = "Не хватает ${neededCoins} 🪙",
                                onClick = {
                                    insufficientCoinsShakeTrigger++
                                    SoundHapticManager.performErrorHaptic()
                                    coroutineScope.launch {
                                        coinBadgeRedFlash.snapTo(1f)
                                        coinBadgeRedFlash.animateTo(0f, tween(650, easing = LinearEasing))
                                    }
                                },
                                containerColor = Color(0xFF6B7280),
                                height = 44.dp,
                                fontSize = 12.sp,
                                modifier = Modifier.width(170.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

/**
 * Карточка товара на витрине рынка
 */
@Composable
private fun MarketItemCard(
    item: ShopItem,
    onItemClick: () -> Unit
) {
    val currentTheme = LocalAppTheme.current
    val cardBg = when (item.category) {
        ShopCategory.FOOD -> Color(0xFFF0FDF4)
        ShopCategory.FURNITURE -> Color(0xFFFFF7ED)
        ShopCategory.ACCESSORIES -> Color(0xFFFAF5FF)
        ShopCategory.HYGIENE -> Color(0xFFF0FDFA)
        ShopCategory.WALLPAPER -> Color(0xFFEFF6FF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = BorderStroke(2.dp, currentTheme.cardOutlineLight),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .bounceClick(scaleDown = 0.95f) { onItemClick() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Название в 2 строки без неправильных переносов
                val formattedTitle = remember(item.name) {
                    item.name.replace("-", "\u2011")
                }
                val titleFontSize = when {
                    item.name.length > 20 -> 8.5.sp
                    item.name.length > 14 -> 9.5.sp
                    else -> 10.5.sp
                }

                Text(
                    text = formattedTitle,
                    fontFamily = UnboundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleFontSize,
                    lineHeight = (titleFontSize.value + 3.5f).sp,
                    minLines = 2,
                    maxLines = 2,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )

                // Иллюстрация: Векторный XML если задан, иначе эмодзи
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.iconRes != null) {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.name,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(46.dp)
                        )
                    } else {
                        Text(text = item.icon, fontSize = 34.sp)
                    }
                }

                // Кнопка с ценой
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (item.discountPercent > 0) {
                            Text(
                                text = "${item.basePrice}",
                                fontSize = 8.5.sp,
                                color = TextSecondary,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = "Купить",
                            fontFamily = UnboundedFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_coin_vector),
                            contentDescription = null,
                            tint = FinCoinGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${item.price}",
                            fontFamily = UnboundedFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        // Ярлык скидки в верхнем углу карточки
        if (item.discountPercent > 0) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 8.dp),
                color = DangerRed,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "-${item.discountPercent}%",
                    fontFamily = UnboundedFamily,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Карточка купленного предмета в гардеробе / на складе
 */
@Composable
private fun WardrobeItemCard(
    item: ShopItem,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    val currentTheme = LocalAppTheme.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isEquipped) currentTheme.bubbleBg else SurfaceLight,
        border = BorderStroke(
            width = if (isEquipped) 2.dp else 1.dp,
            color = if (isEquipped) currentTheme.primaryColor else OutlineLight
        ),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                fontFamily = UnboundedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                maxLines = 1,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.iconRes != null) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.name,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Text(text = item.icon, fontSize = 32.sp)
                }
            }

            if (item.roomSlotType != null) {
                Button(
                    onClick = onEquip,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEquipped) StatGreenEmerald else currentTheme.primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isEquipped) "Надето ✓" else "Надеть",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "В наличии ✓",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
'''

with open(r'C:\Users\festik\.gemini\antigravity\scratch\finpetrustore\app\src\main\java\ru\finpet\app\ui\screens\ShopScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("ShopScreen.kt updated successfully!")
