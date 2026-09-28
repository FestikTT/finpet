package ru.finpet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*

enum class FraudChannel(val title: String, val icon: String, val color: Color) {
    SMS("СМС сообщение", "💬", Color(0xFF10B981)),
    MESSENGER("Telegram / Звонок", "📞", Color(0xFF0091FF)),
    SOCIAL("ВКонтакте / Друг", "👥", Color(0xFF8B5CF6)),
    GAME("Игры / Робуксы", "🎮", Color(0xFFF59E0B)),
    MARKETPLACE("Авито / Доставка", "📦", Color(0xFFEC4899))
}

data class FraudScenario(
    val id: String,
    val channel: FraudChannel,
    val senderName: String,
    val senderAvatar: String,
    val isSenderVerified: Boolean = false,
    val timeFormatted: String,
    val messageText: String,
    val isFraud: Boolean,
    val threatCategory: String,
    val redFlags: List<String>,
    val explanation: String
)

object FraudScenariosBank {
    val scenarios = listOf(
        FraudScenario(
            id = "f1",
            channel = FraudChannel.SMS,
            senderName = "Sber-Alert",
            senderAvatar = "🏦",
            isSenderVerified = false,
            timeFormatted = "14:22",
            messageText = "ВНИМАНИЕ: Зафиксирована попытка списания 4 500 ₽ на стороннем сайте! Если вы не совершали перевод, срочно отмените его по ссылке: sber-safe-cancel.xyz/verify и введите CVC-код карты.",
            isFraud = true,
            threatCategory = "Фишинг и кража данных карты",
            redFlags = listOf(
                "Фальшивый адрес сайта (.xyz вместо официального sberbank.ru)",
                "Требование ввести CVC-код карты (банк никогда не запрашивает CVC)",
                "Создание паники и спешки ('срочно отмените')"
            ),
            explanation = "Настоящий банк никогда не отправляет ссылки на отмену операций и тем более не просит вводить CVC-код с обратной стороны карты!"
        ),
        FraudScenario(
            id = "f2",
            channel = FraudChannel.MESSENGER,
            senderName = "Следователь майор Громов",
            senderAvatar = "👮",
            isSenderVerified = false,
            timeFormatted = "11:05",
            messageText = "Здравствуйте. Я старший следователь отдела кибербезопасности. На ваше имя мошенники прямо сейчас берут кредит в банке! Не кладите трубку, чтобы спасти средства, переведите все деньги на специальный 'Безопасный счёт Центробанка'!",
            isFraud = true,
            threatCategory = "Социальная инженерия (Лже-полиция)",
            redFlags = listOf(
                "Сотрудники полиции и банков никогда не звонят в мессенджерах (Telegram/WhatsApp)",
                "Понятия 'Безопасный счёт' не существует — это счёт самих мошенников",
                "Психологическое давление и запугивание ('не кладите трубку')"
            ),
            explanation = "Сотрудники силовых ведомств никогда не звонят через Telegram и не предлагают переводить деньги на 'безопасные счета'. Сразу сбрасывайте трубку!"
        ),
        FraudScenario(
            id = "f3",
            channel = FraudChannel.GAME,
            senderName = "RobuxDrop_Bot",
            senderAvatar = "💎",
            isSenderVerified = false,
            timeFormatted = "16:40",
            messageText = "🎁 Ура! Ты выиграл 5 000 Robux в честь фестиваля игр! Чтобы забрать свои монеты, перейди на сайт: roblox-bonus-free.ru, авторизуйся под своим логином и паролем от Roblox, и забери выигрыш.",
            isFraud = true,
            threatCategory = "Кража игровых аккаунтов (Стиллер)",
            redFlags = listOf(
                "Бесплатный сыр только в мышеловке — никто не дарит 5000 робуксов просто так",
                "Фейковый сайт-двойник, ворующий логины и пароли",
                "Никогда не вводите свои пароли на сайтах, полученных в личных сообщениях"
            ),
            explanation = "Мошенники создают точные копии страницы входа в игру. Если ввести туда свой пароль, аккаунт украдут навсегда!"
        ),
        FraudScenario(
            id = "f4",
            channel = FraudChannel.SMS,
            senderName = "900",
            senderAvatar = "🔒",
            isSenderVerified = true,
            timeFormatted = "18:15",
            messageText = "Код подтверждения покупки на OZON: 8492. Сумма: 390 ₽. ВНИМАНИЕ: Не сообщайте этот код никому, даже сотруднику банка!",
            isFraud = false,
            threatCategory = "Легитимная двухфакторная аутентификация (2FA)",
            redFlags = listOf(
                "Это настоящее подтверждение платежа, который вы совершаете сами",
                "Банк прямо предупреждает: 'Никому не сообщайте этот код'",
                "Номер отправителя официальный (900)"
            ),
            explanation = "Это настоящее SMS от банка. Главное правило безопасности: если вы сами делаете покупку — вводите код только на странице магазина, но НИКОГДА не диктуйте его голосом никому другому!"
        ),
        FraudScenario(
            id = "f5",
            channel = FraudChannel.SOCIAL,
            senderName = "Артём (Друг)",
            senderAvatar = "👦",
            isSenderVerified = false,
            timeFormatted = "20:02",
            messageText = "Привет! Слушай, мама в больницу попала, срочно не хватает 1000 рублей на лекарство до завтра! Скинь пожалуйста на карту 2200 4589 1234 5678, телефон сел, пишу с чужого акка, завтра в школе сразу верну!",
            isFraud = true,
            threatCategory = "Взлом аккаунта друга",
            redFlags = listOf(
                "Давление на жалость и срочность (болезнь, беда)",
                "Просьба перевести на незнакомую карту",
                "Отговорка 'телефон сел, пишу с чужого аккаунта'"
            ),
            explanation = "Когда друга взламывают, мошенники пишут всем контактам слезливую историю. ВСЕГДА нужно позвонить другу обычным телефонным звонком и лично спросить, правда ли это!"
        ),
        FraudScenario(
            id = "f6",
            channel = FraudChannel.MESSENGER,
            senderName = "Apple Promo Bot",
            senderAvatar = "📱",
            isSenderVerified = false,
            timeFormatted = "12:30",
            messageText = "🎉 ПОЗДРАВЛЯЕМ! Ваш номер выбран победителем розыгрыша iPhone 16 Pro! Для отправки смартфона курьером оплатите только доставку и страховку 390 ₽ по ссылке: apple-delivery-courier.info",
            isFraud = true,
            threatCategory = "Скам с предоплатой за 'выигрыш'",
            redFlags = listOf(
                "Вы не участвовали ни в каком розыгрыше",
                "Требование оплатить 'доставку' или 'комиссию' перед получением приза",
                "Поддельная ссылка с формой оплаты"
            ),
            explanation = "Если для получения бесплатного приза просят заплатить хотя бы один рубль — это 100% обман. После оплаты мошенники просто исчезнут!"
        ),
        FraudScenario(
            id = "f7",
            channel = FraudChannel.MARKETPLACE,
            senderName = "Покупатель с Авито",
            senderAvatar = "🛒",
            isSenderVerified = false,
            timeFormatted = "15:45",
            messageText = "Здравствуйте! Я хочу купить вашу приставку! Я уже оплатил Авито-Доставку, вот ссылка для подтверждения получения денег: avito-oplata-secure.cc/cashout. Перейдите и введите номер карты и баланс для зачисления.",
            isFraud = true,
            threatCategory = "Увод с официальной площадки",
            redFlags = listOf(
                "Попытка перевести общение из приложения Авито в сторонний мессенджер",
                "Ссылка на получение денег (настоящая доставка переводит деньги внутри самого приложения)",
                "Требование ввести баланс карты и срок действия"
            ),
            explanation = "Настоящие торговые площадки (Авито, Юла) проводят сделки строго внутри своего приложения. Никогда не переходите по ссылкам из личной переписки!"
        ),
        FraudScenario(
            id = "f8",
            channel = FraudChannel.SMS,
            senderName = "Штрафы-ГИБДД",
            senderAvatar = "⚠️",
            isSenderVerified = false,
            timeFormatted = "09:12",
            messageText = "Постановление №4920: Неоплаченный штраф 1 500 ₽. Срок добровольной оплаты истекает через 2 часа! Оплатить без судебных приставов со скидкой 50%: gibdd-fast-pay.net",
            isFraud = true,
            threatCategory = "Фейковые задолженности и штрафы",
            redFlags = listOf(
                "Официальные штрафы приходят на Госуслуги, а не в случайных SMS",
                "Короткий срок 'через 2 часа', чтобы жертва не успела проверить информацию",
                "Фальшивый сайт с приемом платежей"
            ),
            explanation = "Любые штрафы и налоги проверяются ТОЛЬКО через официальный портал Госуслуг или личный кабинет банка, но никогда не оплачиваются по ссылкам из SMS!"
        )
    )
}

@Composable
fun FraudSimulatorScreen(
    onNavigateBack: () -> Unit
) {
    val scenarios = FraudScenariosBank.scenarios
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var userDecision by remember { mutableStateOf<Boolean?>(null) }
    var isCorrectDecision by remember { mutableStateOf<Boolean?>(null) }

    val currentScenario = scenarios[currentIndex % scenarios.size]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Индикатор прогресса и очки тренажера
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
                }
                Text(
                    text = "Ситуация ${(currentIndex % scenarios.size) + 1} из ${scenarios.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandVioletPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BrandVioletPrimary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "🛡️ $score",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BrandVioletPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (streak > 1) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceSubtle
                    ) {
                        Text(
                            text = "🔥 x$streak",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        key(currentIndex) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentScenario.channel.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentScenario.channel.color
                )
            }

            // Имитация экрана смартфона (компактная)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceLight,
                border = BorderStroke(1.5.dp, if (isCorrectDecision == true) FinGreenEmerald.copy(alpha = 0.6f) else if (isCorrectDecision == false) BrandPinkNeon.copy(alpha = 0.6f) else OutlineLight),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Верхняя статус-строка смартфона
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentScenario.timeFormatted, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("LTE", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Text("100% 🔋", fontSize = 9.5.sp, color = TextSecondary)
                        }
                    }

                    HorizontalDivider(color = OutlineLight.copy(alpha = 0.6f), thickness = 0.5.dp)

                    // Карточка отправителя
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = currentScenario.channel.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(currentScenario.senderAvatar, fontSize = 18.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = currentScenario.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = TextPrimary
                                )
                                if (currentScenario.isSenderVerified) {
                                    Icon(
                                        Icons.Rounded.Verified,
                                        contentDescription = "Официальный отправитель",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Канал: ${currentScenario.channel.title}",
                                fontSize = 10.5.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Облачко входящего сообщения
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = currentScenario.messageText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = currentScenario.timeFormatted,
                                fontSize = 9.5.sp,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            // Вопрос и кнопки действий
            if (userDecision == null) {
                Text(
                    text = "Что ты сделаешь с этим сообщением?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Кнопка: Заблокировать (Скам!)
                    Button(
                        onClick = {
                            userDecision = true
                            val correct = currentScenario.isFraud
                            isCorrectDecision = correct
                            if (correct) {
                                score += 15 + (streak * 5)
                                streak++
                                GameRepository.addMiniGameReward(3)
                                SoundHapticManager.performSuccessHaptic()
                            } else {
                                streak = 0
                                score = (score - 10).coerceAtLeast(0)
                                GameRepository.applyFraudPenalty(5)
                                SoundHapticManager.performErrorHaptic()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinGreenEmerald),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .bounceClick(),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛡️ Заблокировать", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Это мошенники!", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    // Кнопка: Довериться (Перейти)
                    Button(
                        onClick = {
                            userDecision = false
                            val correct = !currentScenario.isFraud
                            isCorrectDecision = correct
                            if (correct) {
                                score += 15 + (streak * 5)
                                streak++
                                GameRepository.addMiniGameReward(3)
                                SoundHapticManager.performSuccessHaptic()
                            } else {
                                streak = 0
                                score = (score - 10).coerceAtLeast(0)
                                GameRepository.applyFraudPenalty(5)
                                SoundHapticManager.performErrorHaptic()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .bounceClick(),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️ Довериться", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Открыть ссылку / Код", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                // Разбор ситуации после решения игрока
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCorrectDecision == true) FinGreenEmerald.copy(alpha = 0.12f) else BrandPinkNeon.copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, if (isCorrectDecision == true) FinGreenEmerald else BrandPinkNeon),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (isCorrectDecision == true) "✅" else "❌", fontSize = 18.sp)
                            Text(
                                text = if (isCorrectDecision == true) "Верно! Ты раскусил ситуацию (+3 монет)" else "Осторожно! Ты попался на уловку (-5 монет)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = if (isCorrectDecision == true) FinGreenEmerald else BrandPinkNeon
                            )
                        }

                        Text(
                            text = currentScenario.explanation,
                            fontSize = 12.5.sp,
                            lineHeight = 17.5.sp,
                            color = TextPrimary
                        )

                        if (currentScenario.redFlags.isNotEmpty()) {
                            Text(
                                text = "🚩 Признаки опасности:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BrandVioletPrimary
                            )
                            currentScenario.redFlags.forEach { flag ->
                                Text(
                                    text = "• $flag",
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Кнопка следующей ситуации
                Button(
                    onClick = {
                        currentIndex++
                        userDecision = null
                        isCorrectDecision = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandButtonPrimary)
                ) {
                    Text(
                        text = if (currentIndex + 1 < scenarios.size) "Следующая ситуация →" else "Пройти ещё раз 🔄",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
}
