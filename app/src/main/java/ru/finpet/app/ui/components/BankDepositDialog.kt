package ru.finpet.app.ui.components

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.ui.theme.*

@Composable
fun BankDepositDialog(
    state: GameState,
    onDismiss: () -> Unit
) {
    val currentTheme = LocalAppTheme.current
    val totalCoins = state.totalCoins
    val hasActiveDeposit = state.hasActiveBankDeposit

    // Параметры для открытия нового вклада
    var depositAmount by remember { mutableIntStateOf((totalCoins / 2).coerceIn(10, 100.coerceAtMost(totalCoins.coerceAtLeast(10)))) }
    var depositPeriods by remember { mutableIntStateOf(2) } // 1 или 2 периода

    // Расчёт сложного процента
    val rate = state.bankDepositRate // 0.20f = 20%
    val period1Profit = (depositAmount * rate).toInt()
    val period1Total = depositAmount + period1Profit
    val period2Profit = (period1Total * rate).toInt()
    val period2Total = period1Total + period2Profit

    val projectedReturn = if (depositPeriods == 1) period1Total else period2Total
    val projectedProfit = projectedReturn - depositAmount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = SurfaceLight,
            border = BorderStroke(2.dp, currentTheme.borderColor),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Шапка с кнопкой закрытия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FinGoldAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏦", fontSize = 24.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Банковский сейф",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Вклад под сложный процент",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary
                        )
                    }
                }

                // Блок обучения: Что такое сложный процент
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = currentTheme.primaryColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💡", fontSize = 26.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Магия сложного процента",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = currentTheme.primaryColor
                            )
                            Text(
                                text = "Деньги работают на тебя! Каждый период процент начисляется не только на твой вклад, но и на уже накопленные проценты.",
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }

                if (hasActiveDeposit) {
                    // Карточка активного вклада
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceVariantLight,
                        border = BorderStroke(1.5.dp, StatGreenEmerald)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔒", fontSize = 20.sp)
                                    Text(
                                        text = "Твой вклад работает",
                                        fontFamily = UnboundedFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatGreenEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "+20% / период",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatGreenEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Сумма на вкладе:", fontSize = 11.5.sp, color = TextSecondary)
                                    Text("${state.bankDepositAmount} 🪙", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = FinGoldDark)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Осталось периодов:", fontSize = 11.5.sp, color = TextSecondary)
                                    Text("${state.bankDepositPeriodsLeft} из ${state.bankDepositPeriodsLeft}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                }
                            }

                            Text(
                                text = "По завершении периода вклад будет автоматически выплачен в твой кошелек с начисленной прибылью!",
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            CartoonButton(
                                text = "Забрать вклад досрочно",
                                onClick = {
                                    SoundHapticManager.performClickHaptic()
                                    GameRepository.withdrawBankDepositEarly()
                                },
                                containerColor = Color(0xFFEF4444),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Конструктор нового вклада
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceVariantLight,
                        border = BorderStroke(1.5.dp, currentTheme.borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Открыть новый вклад",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )

                            // Выбор суммы
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Сумма вклада:", fontSize = 12.sp, color = TextSecondary)
                                    Text("$depositAmount 🪙", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FinGoldDark)
                                }

                                if (totalCoins >= 10) {
                                    Slider(
                                        value = depositAmount.toFloat(),
                                        onValueChange = {
                                            depositAmount = (it / 5).toInt() * 5
                                            SoundHapticManager.performTickHaptic()
                                        },
                                        valueRange = 10f..totalCoins.toFloat(),
                                        steps = ((totalCoins - 10) / 5).coerceAtLeast(0),
                                        colors = SliderDefaults.colors(
                                            thumbColor = currentTheme.primaryColor,
                                            activeTrackColor = currentTheme.primaryColor
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Для открытия вклада требуется минимум 10 🪙. Выполни квесты или задания!",
                                        fontSize = 11.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }

                                // Быстрые кнопки суммы
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(15, 30, 50, totalCoins).filter { it <= totalCoins && it >= 10 }.distinct().forEach { amt ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (depositAmount == amt) currentTheme.primaryColor else SurfaceLight,
                                            border = BorderStroke(1.dp, currentTheme.borderColor),
                                            modifier = Modifier
                                                .weight(1f)
                                                .bounceClick {
                                                    SoundHapticManager.performClickHaptic()
                                                    depositAmount = amt
                                                }
                                        ) {
                                            Text(
                                                text = if (amt == totalCoins) "Все ($amt)" else "$amt 🪙",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (depositAmount == amt) Color.White else TextPrimary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Выбор срока
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Срок размещения:", fontSize = 12.sp, color = TextSecondary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 1 период
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (depositPeriods == 1) currentTheme.primaryColor.copy(alpha = 0.15f) else SurfaceLight,
                                        border = BorderStroke(
                                            if (depositPeriods == 1) 2.dp else 1.dp,
                                            if (depositPeriods == 1) currentTheme.primaryColor else OutlineLight
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick {
                                                SoundHapticManager.performClickHaptic()
                                                depositPeriods = 1
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("1 период", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            Text("+20% прибыли", fontSize = 11.sp, color = StatGreenEmerald, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    // 2 периода
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (depositPeriods == 2) currentTheme.primaryColor.copy(alpha = 0.15f) else SurfaceLight,
                                        border = BorderStroke(
                                            if (depositPeriods == 2) 2.dp else 1.dp,
                                            if (depositPeriods == 2) currentTheme.primaryColor else OutlineLight
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick {
                                                SoundHapticManager.performClickHaptic()
                                                depositPeriods = 2
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("2 периода 🌟", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            Text("+44% (сложный %)", fontSize = 11.sp, color = StatGreenEmerald, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            // Интерактивный прогноз выплаты
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FinGoldAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FinGoldAccent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Прогноз выплаты:", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            text = "$projectedReturn 🪙",
                                            fontFamily = UnboundedFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = FinGoldDark
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = StatGreenEmerald.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "+$projectedProfit 🪙 прибыли!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = StatGreenEmerald,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Кнопка подтверждения
                            CartoonButton(
                                text = "Положить $depositAmount 🪙 в банк",
                                onClick = {
                                    SoundHapticManager.performClickHaptic()
                                    val ok = GameRepository.openBankDeposit(depositAmount, depositPeriods)
                                    if (ok) {
                                        onDismiss()
                                    }
                                },
                                enabled = totalCoins >= depositAmount && depositAmount >= 10,
                                containerColor = BrandButtonPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Статистика заработанных процентов
                if (state.totalBankInterestEarned > 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceLight,
                        border = BorderStroke(1.dp, OutlineLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("📈", fontSize = 22.sp)
                            Column {
                                Text(
                                    text = "Всего заработано на процентах:",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "+${state.totalBankInterestEarned} 🪙 чистой прибыли",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = StatGreenEmerald
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
