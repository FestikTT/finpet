package ru.finpet.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.data.GameDialog
import ru.finpet.app.model.FinancialGoal
import ru.finpet.app.model.ShopItem
import ru.finpet.app.ui.theme.*

@Composable
fun GameDialogHost(
    activeDialog: GameDialog?,
    onDismiss: () -> Unit,
    onConfirmPurchase: (ShopItem) -> Unit,
    onConfirmWithdrawal: (goalId: String, amount: Int) -> Unit,
    onGoToQuests: () -> Unit,
    onGoToBudget: () -> Unit,
    onGoToRoom: (() -> Unit)? = null
) {
    if (activeDialog == null) return

    when (activeDialog) {
        is GameDialog.DeliveryUnboxing -> {
            DeliveryUnboxingDialog(
                item = activeDialog.item,
                onDismiss = onDismiss,
                onGoToRoom = onGoToRoom
            )
        }

        is GameDialog.InsufficientFunds -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = BrandPinkNeon,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Ой, не хватает монет!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Товар «${activeDialog.itemName}» стоит ${activeDialog.price} монет, а у тебя сейчас ${activeDialog.currentCoins} монет. Не хватает ${activeDialog.needed} монет.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "💡 Что можно сделать?",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandVioletPrimary,
                                    fontSize = 13.sp
                                )
                                Text("1. Выполнить задание в разделе «Квесты» и заработать награду.", fontSize = 12.sp, color = TextPrimary)
                                Text("2. Скорректировать бюджет и перераспределить средства.", fontSize = 12.sp, color = TextPrimary)
                                Text("3. Отложить покупку на следующий период.", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDismiss()
                            onGoToQuests()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp).bounceClick()
                    ) {
                        Text(
                            text = "Заработать в квестах",
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onGoToBudget()
                        },
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text(
                            text = "К бюджету",
                            color = BrandRoseWarm,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }

        is GameDialog.ConfirmPurchase -> {
            val item = activeDialog.item
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Text(text = item.icon, fontSize = 38.sp)
                },
                title = {
                    Text(
                        text = "Купить ${item.name}?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (item.isMandatory) "Это обязательная покупка для здоровья питомца." else "Это приятная вещь для настроения и уюта.",
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
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Стоимость:", fontSize = 13.sp, color = TextSecondary)
                                Text("${item.price} 🪙", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandVioletPrimary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmPurchase(item) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isMandatory) BrandRoseWarm else BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp).bounceClick()
                    ) {
                        Text(
                            text = "Купить за ${item.price} 🪙",
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, modifier = Modifier.height(46.dp)) {
                        Text(
                            text = "Подумать ещё",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }

        is GameDialog.ConfirmWithdrawal -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = BrandPinkNeon,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Точно снять из копилки?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Ты хочешь забрать ${activeDialog.withdrawAmount} монет из цели «${activeDialog.goal.title}».",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandPinkNeon.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BrandPinkNeon.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "⚠️ Обрати внимание:",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandRoseWarm,
                                    fontSize = 12.sp
                                )
                                Text("• В копилке останется: ${activeDialog.goal.currentAmount - activeDialog.withdrawAmount} монет.", fontSize = 12.sp, color = TextPrimary)
                                Text("• До мечты останется: ${activeDialog.newRemaining} монет.", fontSize = 12.sp, color = TextPrimary)
                                Text("• Срок достижения увеличится на ${activeDialog.delayPeriods} период(а)!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandPinkNeon)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmWithdrawal(activeDialog.goal.id, activeDialog.withdrawAmount) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPinkNeon,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text(
                            text = "Да, снять монеты",
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text(
                            text = "Оставить в копилке",
                            color = BrandVioletPrimary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }

        is GameDialog.QuestResult -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Text(
                        text = if (activeDialog.option.isOptimal) "🌟" else "💡",
                        fontSize = 38.sp
                    )
                },
                title = {
                    Text(
                        text = if (activeDialog.option.isOptimal) "Отличное решение!" else "Урок на будущее",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (activeDialog.option.isOptimal) BrandVioletPrimary else BrandRoseWarm
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = activeDialog.option.feedbackExplanation,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceSubtle,
                                border = BorderStroke(1.dp, BrandVioletPrimary.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "+${activeDialog.option.coinReward} монет 🪙",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BrandVioletPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (activeDialog.option.finScoreDelta != 0) {
                                Text(
                                    text = if (activeDialog.option.finScoreDelta > 0) "+${activeDialog.option.finScoreDelta} к фин-индексу" else "${activeDialog.option.finScoreDelta} к фин-индексу",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (activeDialog.option.finScoreDelta > 0) BrandVioletPrimary else BrandPinkNeon
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp).bounceClick()
                    ) {
                        Text(
                            text = "Понятно, спасибо!",
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }

        is GameDialog.PeriodCompleted -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Text(text = "🎉", fontSize = 42.sp)
                },
                title = {
                    Text(
                        text = "Период ${activeDialog.periodNumber} завершен!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BrandVioletPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = activeDialog.summary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, OutlineLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Карманные деньги на новый период:",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "+${activeDialog.nextPocketMoney} 🪙",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandVioletPrimary,
                                    softWrap = false
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp).bounceClick()
                    ) {
                        Text(
                            text = "Начать новый период!",
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }

        is GameDialog.GoalAchieved -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Text(text = "🏆", fontSize = 44.sp)
                },
                title = {
                    Text(
                        text = "Ура! Цель достигнута!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = BrandVioletPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Поздравляем! Ты накопил ${activeDialog.goal.currentAmount} монет на цель «${activeDialog.goal.title}»!",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Регулярные накопления и контроль трат творят настоящие чудеса!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp).bounceClick()
                    ) {
                        Text("Ура! Празднуем!", color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }
                }
            )
        }

        is GameDialog.DailyLimitReached -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Text("🔥", fontSize = 36.sp)
                },
                title = {
                    Text(
                        text = "Дневная норма выполнена!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Ты решил ${activeDialog.completedCount} из 3 заданий на сегодня. Серия дней: ${activeDialog.streakDays} 🔥",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandVioletPrimary
                        )
                        Text(
                            text = "Отличная работа! Чтобы знания надежно усвоились, мозгу нужен отдых. Возвращайся завтра за новыми квестами!",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp).bounceClick()
                    ) {
                        Text("До завтра!", color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }
                }
            )
        }

        is GameDialog.HelpAdvice -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceLight,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = BrandVioletPrimary, modifier = Modifier.size(34.dp))
                },
                title = {
                    Text("3 правила юного финансиста", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandVioletPrimary)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Сначала обязательное: еда, гигиена и здоровье питомца — это основа жизни.", fontSize = 12.sp, color = TextPrimary)
                        Text("2. Копилка на мечту: откладывай хотя бы часть дохода перед тем, как тратить.", fontSize = 12.sp, color = TextPrimary)
                        Text("3. Радости с умом: сладости и игрушки покупай только на оставшиеся свободные средства.", fontSize = 12.sp, color = TextPrimary)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandButtonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp).bounceClick()
                    ) {
                        Text("Всё ясно!", color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }
                }
            )
        }
    }
}
