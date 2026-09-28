package ru.finpet.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*

enum class MiniGameTab(val title: String, val icon: String) {
    SUPERMARKET("Супермаркет", "🛒"),
    SORTING("Нужно / Хочу", "🧺")
}

data class SortingItem(
    val id: String,
    val title: String,
    val emoji: String,
    val isNeed: Boolean, // true = Нужно, false = Хочу
    val explanation: String
)

data class SupermarketProduct(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int,
    val isOnList: Boolean,
    val tag: String? = null,
    val isTrap: Boolean = false,
    val trapTip: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGameScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MiniGameTab.SUPERMARKET) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Верхний бар с кнопкой назад и переключателем режимов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundHapticManager.performClickHaptic()
                    onBack()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Назад",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Переключатель мини-игр
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MiniGameTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BrandButtonPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick {
                                    SoundHapticManager.performClickHaptic()
                                    selectedTab = tab
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tab.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Контент выбранной мини-игры
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                MiniGameTab.SUPERMARKET -> SupermarketBasketGame(onBack = onBack)
                MiniGameTab.SORTING -> NeedsWantsGame(onBack = onBack)
            }
        }
    }
}

/**
 * Мини-игра 1: «Умная корзина супермаркета»
 * Собери все товары из семейного списка в пределах бюджета и не попадись на маркетинговые уловки!
 */
@Composable
private fun SupermarketBasketGame(
    onBack: () -> Unit
) {
    val allProducts = remember {
        listOf(
            SupermarketProduct("bread", "Хлеб ржаной", "🍞", 40, isOnList = true),
            SupermarketProduct("milk", "Молоко 3.2%", "🥛", 75, isOnList = true),
            SupermarketProduct("cheese", "Сыр фермерский", "🧀", 150, isOnList = true),
            SupermarketProduct("apples", "Яблоки (1 кг)", "🍎", 85, isOnList = true),
            SupermarketProduct("choco", "Шоколад у кассы", "🍫", 120, isOnList = false, tag = "Акция! 🔥", isTrap = true, trapTip = "Сладости у кассы — частая импульсивная покупка!"),
            SupermarketProduct("soda", "Газировка 1+1", "🥤", 130, isOnList = false, tag = "Выгода 1+1", isTrap = true, trapTip = "Акция 1+1 побуждает брать то, что не нужно!"),
            SupermarketProduct("toy", "Мягкий мишка", "🧸", 230, isOnList = false, tag = "Хит", isTrap = false, trapTip = "Игрушка превысит семейный бюджет на продукты!"),
            SupermarketProduct("chips", "Чипсы пачка", "🍟", 145, isOnList = false),
            SupermarketProduct("eggs", "Яйца 10 шт", "🥚", 95, isOnList = false),
            SupermarketProduct("bananas", "Бананы (1 кг)", "🍌", 70, isOnList = false)
        )
    }

    val budget = 500
    val selectedIds = remember { mutableStateListOf<String>() }
    var isSuccess by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }
    var coinsAwarded by remember { mutableIntStateOf(0) }

    val totalSpent = selectedIds.sumOf { id -> allProducts.find { it.id == id }?.price ?: 0 }
    val requiredIds = remember { setOf("bread", "milk", "cheese", "apples") }

    if (isSuccess) {
        // Экран победы
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(2.dp, StatGreenEmerald)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("🛒🎉", fontSize = 48.sp)
                    Text(
                        text = "Умный покупатель!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatGreenEmerald
                    )
                    Text(
                        text = "Ты купил всё по списку, не попался на уловки маркетологов и сохранил ${budget - totalSpent} ₽!",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FinGoldAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, FinGoldAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🪙", fontSize = 22.sp)
                            Text(
                                text = "+$coinsAwarded монет в кошелек!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinGoldDark
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                SoundHapticManager.performClickHaptic()
                                onBack()
                            },
                            modifier = Modifier.weight(1f).height(46.dp).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Text("В меню", color = BrandVioletPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                SoundHapticManager.performClickHaptic()
                                selectedIds.clear()
                                isSuccess = false
                                feedbackMessage = null
                            },
                            modifier = Modifier.weight(1.2f).height(46.dp).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary)
                        ) {
                            Text("Еще раз 🔄", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Карточка списка покупок
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Список покупок для дома:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FinGoldAccent.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, FinGoldAccent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "Бюджет: $budget ₽",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp,
                                color = FinGoldDark,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val listItems = allProducts.filter { it.isOnList }
                        listItems.forEach { prod ->
                            val isCollected = prod.id in selectedIds
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCollected) StatGreenEmerald.copy(alpha = 0.15f) else SurfaceSubtle,
                                border = BorderStroke(1.dp, if (isCollected) StatGreenEmerald else OutlineLight),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(prod.emoji, fontSize = 18.sp)
                                    Text(
                                        text = "${prod.price} ₽",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCollected) StatGreenEmerald else TextSecondary,
                                        textDecoration = if (isCollected) TextDecoration.LineThrough else null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Сообщение с подсказкой / ошибкой
            if (feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFeedbackError) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, if (isFeedbackError) Color(0xFFEF4444) else StatGreenEmerald),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = feedbackMessage!!,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFeedbackError) Color(0xFF991B1B) else Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Полки магазина (товары)
            Text(
                text = "Полки супермаркета (нажми, чтобы положить в корзину):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allProducts) { prod ->
                    val isInCart = prod.id in selectedIds
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isInCart) BrandVioletPrimary.copy(alpha = 0.08f) else SurfaceLight,
                        border = BorderStroke(
                            width = if (isInCart) 2.dp else 1.dp,
                            color = if (isInCart) BrandVioletPrimary else OutlineLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick {
                                if (isInCart) {
                                    selectedIds.remove(prod.id)
                                    SoundHapticManager.performTickHaptic()
                                } else {
                                    selectedIds.add(prod.id)
                                    SoundHapticManager.performTapHaptic()
                                    if (prod.isTrap) {
                                        SoundHapticManager.playCoinSound()
                                    }
                                }
                                feedbackMessage = null
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(prod.emoji, fontSize = 24.sp)
                                if (prod.tag != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (prod.isTrap) BrandPinkNeon.copy(alpha = 0.2f) else FinGoldAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = prod.tag,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (prod.isTrap) BrandPinkNeon else FinGoldDark,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (prod.isOnList) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatGreenEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "В списке ✅",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatGreenEmerald,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = prod.name,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${prod.price} ₽",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinGoldDark
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = if (isInCart) BrandVioletPrimary else SurfaceSubtle,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isInCart) Icons.Rounded.Check else Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = if (isInCart) Color.White else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Нижняя плашка корзины и кнопка оплаты на кассе
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                border = BorderStroke(1.5.dp, if (totalSpent > budget) BrandPinkNeon else OutlineLight),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛒 В корзине: ${selectedIds.size} шт",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )

                        Text(
                            text = "$totalSpent ₽ / $budget ₽",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (totalSpent > budget) BrandPinkNeon else StatGreenEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (totalSpent.toFloat() / budget).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (totalSpent > budget) BrandPinkNeon else StatGreenEmerald,
                        trackColor = OutlineLight.copy(alpha = 0.35f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val missing = requiredIds.filter { it !in selectedIds }
                            val hasTraps = selectedIds.any { id -> allProducts.find { it.id == id }?.isTrap == true }

                            if (totalSpent > budget) {
                                feedbackMessage = "Бюджет превышен ($totalSpent ₽ из $budget ₽)! Выложи лишнее из корзины."
                                isFeedbackError = true
                                SoundHapticManager.performErrorHaptic()
                            } else if (missing.isNotEmpty()) {
                                val missingNames = missing.mapNotNull { id -> allProducts.find { it.id == id }?.name }.joinToString(", ")
                                feedbackMessage = "Ты забыл купить по списку: $missingNames!"
                                isFeedbackError = true
                                SoundHapticManager.performErrorHaptic()
                            } else if (hasTraps) {
                                feedbackMessage = "В корзине маркетинговые уловки у кассы! Яркие акции заставляют тратить лишнее. Убери их."
                                isFeedbackError = true
                                SoundHapticManager.performErrorHaptic()
                            } else {
                                coinsAwarded = 3
                                GameRepository.addMiniGameReward(3)
                                SoundHapticManager.playSuccessSound()
                                SoundHapticManager.performSuccessHaptic()
                                isSuccess = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (totalSpent > budget) Color(0xFFEF4444) else StatGreenEmerald
                        )
                    ) {
                        Text(
                            text = "Оплатить на кассе 💳",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Мини-игра 2: «Нужно или Хочу»
 * Быстрая сортировка потребностей и желаний на время
 */
@Composable
private fun NeedsWantsGame(
    onBack: () -> Unit
) {
    val itemsPool = remember {
        listOf(
            SortingItem("1", "Хлеб и молоко", "🥛", true, "Базовые продукты питания — это обязательная потребность!"),
            SortingItem("2", "Новая видеоигра", "🎮", false, "Игры приносят радость, но это категория 'Хочу'."),
            SortingItem("3", "Тетради и ручки для школы", "📚", true, "Учеба и канцелярские товары необходимы для школы."),
            SortingItem("4", "Чипсы и лимонад", "🥤", false, "Вкусняшки и снеки — это приятные 'Хочу'."),
            SortingItem("5", "Лекарство от простуды", "💊", true, "Здоровье и лекарства — важнейшая жизненная потребность!"),
            SortingItem("6", "Коллекционная фигурка", "🧸", false, "Игрушки для коллекции — это развлечения ('Хочу')."),
            SortingItem("7", "Проездной на автобус", "🚌", true, "Поездки в школу и кружки — обязательные расходы."),
            SortingItem("8", "Золотой чехол для телефона", "✨", false, "Красивый аксессуар — это желание, старый чехол тоже работает."),
            SortingItem("9", "Теплая зимняя куртка", "🧥", true, "Одежда по сезону защищает от холода и простуды."),
            SortingItem("10", "Пятая пара кроссовок", "👟", false, "Лишняя пара обуви для стиля — это категория 'Хочу'."),
            SortingItem("11", "Обед в школьной столовой", "🍲", true, "Полноценный горячий обед необходим для сил и здоровья."),
            SortingItem("12", "Сладкая вата в парке", "🍭", false, "Сладости на празднике — это лакомство ('Хочу')."),
            SortingItem("13", "Оплата интернета для дома", "🌐", true, "Связь и интернет нужны для учебы и общения семьи."),
            SortingItem("14", "Платные стикеры в чате", "💬", false, "Картинки в переписке делают общение ярче, но это 'Хочу'."),
            SortingItem("15", "Зубная паста и щетка", "🪥", true, "Гигиена и уход за зубами спасают от походов к стоматологу!"),
            SortingItem("16", "Радиоуправляемый дрон", "🛸", false, "Увлекательный гаджет для игр — классическое 'Хочу'.")
        ).shuffled()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(45) }
    var isGameOver by remember { mutableStateOf(false) }
    var lastFeedback by remember { mutableStateOf<String?>(null) }
    var isLastCorrect by remember { mutableStateOf<Boolean?>(null) }
    var coinsAwarded by remember { mutableIntStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }
    var userAnsweredNeed by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (timeLeft > 0 && !isGameOver) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft == 0 && !isGameOver) {
                isGameOver = true
                val reward = (score / 40).coerceIn(0, 3)
                coinsAwarded = reward
                GameRepository.addMiniGameReward(reward)
                SoundHapticManager.playSuccessSound()
                SoundHapticManager.performSuccessHaptic()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isGameOver) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.5.dp, BrandVioletPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Text(
                        text = "Время вышло!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandVioletPrimary
                    )
                    Text(
                        text = "Ты набрал $score очков и потренировал финансовую интуицию!",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FinGoldAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, FinGoldAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🪙", fontSize = 22.sp)
                            Text(
                                text = "+$coinsAwarded монет в кошелек!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinGoldDark
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                SoundHapticManager.performClickHaptic()
                                onBack()
                            },
                            modifier = Modifier.weight(1f).height(46.dp).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Text("В меню", color = BrandVioletPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                SoundHapticManager.performClickHaptic()
                                currentIndex = 0
                                score = 0
                                streak = 0
                                timeLeft = 45
                                lastFeedback = null
                                isLastCorrect = null
                                isAnswered = false
                                userAnsweredNeed = null
                                isGameOver = false
                            },
                            modifier = Modifier.weight(1.2f).height(46.dp).bounceClick(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary)
                        ) {
                            Text("Еще раз 🔄", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            val currentItem = itemsPool[currentIndex % itemsPool.size]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Панель статуса
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, OutlineLight)
                    ) {
                        Text(
                            text = "Вопрос ${(currentIndex % itemsPool.size) + 1} / ${itemsPool.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⏱️", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${timeLeft}с",
                                    fontWeight = FontWeight.Bold,
                                    color = if (timeLeft <= 7) BrandPinkNeon else BrandVioletPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏆 ", fontSize = 11.sp)
                                Text("$score", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = BrandVioletPrimary)
                            }
                        }

                        if (streak > 1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FinAmberWarning.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FinAmberWarning.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "🔥 x$streak",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { ((currentIndex % itemsPool.size) + 1).toFloat() / itemsPool.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = BrandVioletPrimary,
                    trackColor = OutlineLight.copy(alpha = 0.35f)
                )

                // Карточка предмета
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(currentItem.emoji, fontSize = 56.sp)
                        Text(
                            text = currentItem.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Пояснение после ответа
                if (isAnswered && lastFeedback != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isLastCorrect == true) StatGreenEmerald.copy(alpha = 0.15f) else BrandPinkNeon.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isLastCorrect == true) StatGreenEmerald else BrandPinkNeon),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(if (isLastCorrect == true) "✅" else "💡", fontSize = 22.sp)
                            Text(
                                text = lastFeedback!!,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Кнопки ответа
                if (!isAnswered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val isCorrect = currentItem.isNeed
                                isAnswered = true
                                userAnsweredNeed = true
                                isLastCorrect = isCorrect
                                lastFeedback = currentItem.explanation
                                if (isCorrect) {
                                    score += 20 + streak * 5
                                    streak++
                                    SoundHapticManager.playCoinSound()
                                    SoundHapticManager.performSuccessHaptic()
                                } else {
                                    streak = 0
                                    SoundHapticManager.performErrorHaptic()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .bounceClick(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatGreenEmerald)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛡️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("НУЖНО", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("Потребность", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val isCorrect = !currentItem.isNeed
                                isAnswered = true
                                userAnsweredNeed = false
                                isLastCorrect = isCorrect
                                lastFeedback = currentItem.explanation
                                if (isCorrect) {
                                    score += 20 + streak * 5
                                    streak++
                                    SoundHapticManager.playCoinSound()
                                    SoundHapticManager.performSuccessHaptic()
                                } else {
                                    streak = 0
                                    SoundHapticManager.performErrorHaptic()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .bounceClick(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎮", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("ХОЧУ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("Развлечения", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            if (currentIndex + 1 >= itemsPool.size) {
                                isGameOver = true
                                val reward = (score / 40).coerceIn(0, 3)
                                coinsAwarded = reward
                                GameRepository.addMiniGameReward(reward)
                                SoundHapticManager.playSuccessSound()
                                SoundHapticManager.performSuccessHaptic()
                            } else {
                                currentIndex++
                                isAnswered = false
                                userAnsweredNeed = null
                                isLastCorrect = null
                                lastFeedback = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastCorrect == true) StatGreenEmerald else BrandButtonPrimary
                        )
                    ) {
                        Text(
                            text = if (currentIndex + 1 >= itemsPool.size) "Завершить игру 🏆" else "Следующий вопрос →",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
