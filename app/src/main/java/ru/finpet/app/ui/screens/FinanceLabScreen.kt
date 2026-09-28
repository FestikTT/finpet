package ru.finpet.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.model.*
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinanceLabScreen(
    onBack: () -> Unit
) {
    val currentTheme = LocalAppTheme.current
    var selectedLabTab by remember { mutableIntStateOf(0) } // 0: Сложный процент, 1: Кофе-эффект, 2: Подушка безопасности, 3: Лайфхаки
    var isCoinMode by remember { mutableStateOf(false) } // Переключатель валюты (Аудит п.9)

    val rubFormatter = remember {
        NumberFormat.getNumberInstance(Locale("ru")).apply {
            maximumFractionDigits = 0
        }
    }

    val currencyUnit = if (isCoinMode) "🪙" else "₽"
    fun formatMoney(rubAmount: Long): String {
        val displayAmount = if (isCoinMode) (rubAmount / 10).coerceAtLeast(1L) else rubAmount
        return "${rubFormatter.format(displayAmount)} $currencyUnit"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Шапка раздела с переключателем валют (Аудит п.9)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = {
                        SoundHapticManager.performClickHaptic()
                        onBack()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Назад",
                        tint = currentTheme.primaryColor
                    )
                }
                Column {
                    Text(
                        text = "Фин-лаб 🔬",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = currentTheme.primaryColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Калькуляторы",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Переключатель валюты (₽ Рубли / 🪙 Монеты)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceSubtle,
                border = BorderStroke(1.dp, OutlineLight)
            ) {
                Row(
                    modifier = Modifier.padding(2.5.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = if (!isCoinMode) currentTheme.primaryColor else Color.Transparent,
                        modifier = Modifier
                            .bounceClick {
                                SoundHapticManager.performClickHaptic()
                                isCoinMode = false
                            }
                    ) {
                        Text(
                            text = "₽ Рубли",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isCoinMode) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = if (isCoinMode) currentTheme.primaryColor else Color.Transparent,
                        modifier = Modifier
                            .bounceClick {
                                SoundHapticManager.performClickHaptic()
                                isCoinMode = true
                            }
                    ) {
                        Text(
                            text = "🪙 Монеты",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCoinMode) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp)
                        )
                    }
                }
            }
        }

        // Сегментированный переключатель калькуляторов
        ScrollableTabRow(
            selectedTabIndex = selectedLabTab,
            edgePadding = 8.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf("📈 Сложный процент", "☕ Кофе-эффект", "🛡️ Подушка", "💡 Лайфхаки")
            tabs.forEachIndexed { idx, title ->
                val isSel = selectedLabTab == idx
                Tab(
                    selected = isSel,
                    onClick = {
                        SoundHapticManager.performClickHaptic()
                        selectedLabTab = idx
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) currentTheme.primaryColor else SurfaceLight,
                        border = BorderStroke(1.dp, if (isSel) currentTheme.primaryColor else OutlineLight),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        when (selectedLabTab) {
            0 -> CompoundInterestSection(::formatMoney)
            1 -> CoffeeEffectSection(::formatMoney)
            2 -> EmergencyFundSection(::formatMoney)
            3 -> PracticalTipsSection()
        }
    }
}

@Composable
private fun CompoundInterestSection(
    formatMoney: (Long) -> String
) {
    val currentTheme = LocalAppTheme.current
    var monthlyAmount by remember { mutableFloatStateOf(3000f) }
    var years by remember { mutableFloatStateOf(3f) }
    var rate by remember { mutableDoubleStateOf(16.0) }

    val result = remember(monthlyAmount, years, rate) {
        CompoundInterestCalculator.calculate(
            monthlyDepositRub = monthlyAmount.toInt(),
            interestRatePercent = rate,
            years = years.toInt()
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, OutlineLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Магия сложного процента (Вклады)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = currentTheme.primaryColor
            )
            Text(
                text = "Посмотри, как регулярные пополнения и проценты от банка превращают небольшие суммы в серьезный капитал.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            // Слайдер ежемесячного пополнения
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ежемесячно откладывать:", fontSize = 12.sp, color = TextPrimary)
                    Text(
                        "${formatMoney(monthlyAmount.toLong())} / мес",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = currentTheme.primaryColor
                    )
                }
                Slider(
                    value = monthlyAmount,
                    onValueChange = { monthlyAmount = it },
                    valueRange = 500f..25000f,
                    steps = 48,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primaryColor,
                        activeTrackColor = currentTheme.primaryColor
                    )
                )
            }

            // Слайдер срока накопления
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Срок накопления:", fontSize = 12.sp, color = TextPrimary)
                    val yearsInt = years.toInt()
                    val word = when {
                        yearsInt == 1 -> "год"
                        yearsInt in 2..4 -> "года"
                        else -> "лет"
                    }
                    Text(
                        "$yearsInt $word",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = currentTheme.primaryColor
                    )
                }
                Slider(
                    value = years,
                    onValueChange = { years = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primaryColor,
                        activeTrackColor = currentTheme.primaryColor
                    )
                )
            }

            // Слайдер процентной ставки
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ставка по вкладу:", fontSize = 12.sp, color = TextPrimary)
                    Text(
                        "${rate.toInt()}% годовых",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = currentTheme.primaryColor
                    )
                }
                Slider(
                    value = rate.toFloat(),
                    onValueChange = { rate = it.toDouble() },
                    valueRange = 8f..24f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primaryColor,
                        activeTrackColor = currentTheme.primaryColor
                    )
                )
            }

            HorizontalDivider(color = OutlineLight)

            // Результаты расчета
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceSubtle,
                    border = BorderStroke(1.dp, OutlineLight)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Вложено своих", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = formatMoney(result.totalInvestedRub),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = TextPrimary
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = StatGreenEmerald.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StatGreenEmerald.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Бонус процентов", fontSize = 11.sp, color = StatGreenEmerald)
                        Text(
                            text = "+${formatMoney(result.interestEarnedRub)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = StatGreenEmerald
                        )
                    }
                }
            }

            // Итоговая плашка
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = currentTheme.primaryColor.copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, currentTheme.primaryColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Итого на накопительном счете:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = formatMoney(result.totalAccumulatedRub),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = currentTheme.primaryColor
                        )
                    }
                    Text("🚀", fontSize = 26.sp)
                }
            }

            // Практический вывод
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceSubtle,
                border = BorderStroke(1.dp, OutlineLight)
            ) {
                Text(
                    text = "💡 За ${result.years} г. банк начислит тебе ${formatMoney(result.interestEarnedRub)} чистой прибыли за счет реинвестирования процентов. Время — лучший друг инвестора!",
                    fontSize = 11.5.sp,
                    color = TextPrimary,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun CoffeeEffectSection(
    formatMoney: (Long) -> String
) {
    val currentTheme = LocalAppTheme.current
    var selectedPreset by remember { mutableStateOf(CoffeeEffectCalculator.presets.first()) }
    var customDailyPrice by remember { mutableIntStateOf(selectedPreset.defaultPriceRub) }

    val impact = remember(customDailyPrice) {
        CoffeeEffectCalculator.calculate(customDailyPrice)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, OutlineLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "«Кофе-эффект» (Ловушка мелких трат)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = currentTheme.primaryColor
            )
            Text(
                text = "Маленькие ежедневные расходы по 100-300 ₽ кажутся незаметными, но за год превращаются в стоимость смартфона или поездки.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            // Выбор привычки
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Выбери привычку:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoffeeEffectCalculator.presets.forEach { preset ->
                        val isSel = selectedPreset == preset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick {
                                    selectedPreset = preset
                                    customDailyPrice = preset.defaultPriceRub
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) currentTheme.primaryColor.copy(alpha = 0.15f) else SurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSel) currentTheme.primaryColor else OutlineLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(preset.icon, fontSize = 20.sp)
                                Text(
                                    text = preset.title,
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) currentTheme.primaryColor else TextPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Сумма в день
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Стоимость в день:", fontSize = 12.sp, color = TextPrimary)
                    Text(
                        "${formatMoney(customDailyPrice.toLong())} / день",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = currentTheme.primaryColor
                    )
                }
                Slider(
                    value = customDailyPrice.toFloat(),
                    onValueChange = { customDailyPrice = it.toInt() },
                    valueRange = 50f..1000f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primaryColor,
                        activeTrackColor = currentTheme.primaryColor
                    )
                )
            }

            // Разбор экономии
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceSubtle,
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("В месяц уходит:", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            formatMoney(impact.monthlyRub),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("За 1 год (чистая экономия):", fontSize = 12.sp, color = TextSecondary)
                            Text(selectedPreset.equivalent1Year, fontSize = 11.sp, color = StatGreenEmerald, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            formatMoney(impact.yearRub),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = StatGreenEmerald
                        )
                    }
                    HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("За 3 года (с вкладом под 16%):", fontSize = 12.sp, color = TextSecondary)
                            Text(selectedPreset.equivalent3Years, fontSize = 11.sp, color = currentTheme.primaryColor, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            formatMoney(impact.threeYearsWithInterestRub),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = currentTheme.primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyFundSection(
    formatMoney: (Long) -> String
) {
    val currentTheme = LocalAppTheme.current
    var monthlyNeeds by remember { mutableFloatStateOf(20000f) }

    val fund3Months = (monthlyNeeds * 3).toLong()
    val fund6Months = (monthlyNeeds * 6).toLong()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, OutlineLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Финансовая подушка безопасности",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = currentTheme.primaryColor
            )
            Text(
                text = "Резерв денег на случай непредвиденных ситуаций (поломка техники, болезнь, смена работы). Избавляет от необходимости брать микрозаймы и кредиты.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Базовые расходы в месяц:", fontSize = 12.sp, color = TextPrimary)
                    Text(
                        formatMoney(monthlyNeeds.toLong()),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = currentTheme.primaryColor
                    )
                }
                Slider(
                    value = monthlyNeeds,
                    onValueChange = { monthlyNeeds = it },
                    valueRange = 5000f..80000f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primaryColor,
                        activeTrackColor = currentTheme.primaryColor
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceSubtle,
                    border = BorderStroke(1.dp, OutlineLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("🛡️ На 3 месяца", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = formatMoney(fund3Months),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = StatAmberWarm
                        )
                        Text("Базовый уровень", fontSize = 10.sp, color = TextSecondary)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = StatGreenEmerald.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StatGreenEmerald.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("🏰 На 6 месяцев", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatGreenEmerald)
                        Text(
                            text = formatMoney(fund6Months),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = StatGreenEmerald
                        )
                        Text("Полная безопасность", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceSubtle,
                border = BorderStroke(1.dp, OutlineLight)
            ) {
                Text(
                    text = "📌 Золотое правило: храни подушку безопасности на накопительном счете с ежедневным процентом, откуда деньги можно снять без потери процентов в любой момент.",
                    fontSize = 11.5.sp,
                    color = TextPrimary,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun PracticalTipsSection() {
    val currentTheme = LocalAppTheme.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PracticalMoneyKnowledge.tips.forEach { tip ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(tip.icon, fontSize = 22.sp)
                        Column {
                            Text(
                                text = tip.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = currentTheme.primaryColor
                            )
                            Text(tip.topic, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Text(
                        text = tip.shortSummary,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, OutlineLight)
                    ) {
                        Text(
                            text = "👉 ${tip.actionableRule}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.primaryColor,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
