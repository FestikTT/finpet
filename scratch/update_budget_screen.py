import os

with open(r'app/src/main/java/ru/finpet/app/ui/screens/BudgetScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Add import
if 'import ru.finpet.app.util.FormatUtils' not in code:
    code = code.replace(
        'import ru.finpet.app.ui.theme.*',
        'import ru.finpet.app.ui.theme.*\nimport ru.finpet.app.util.FormatUtils'
    )

# 2. Replace the tags
old_tags = '''            val categoryTags = when (envelope) {
                EnvelopeType.NEEDS -> listOf("Корм", "Аптека", "Уход")
                EnvelopeType.SAVINGS -> listOf("Мечта", "Подушка", "Сбережения")
                EnvelopeType.WANTS -> listOf("Игрушки", "Вкусняшки", "Развлечения")
            }'''

new_tags = '''            val categoryTags = when (envelope) {
                EnvelopeType.NEEDS -> listOf("Корм", "Аптека", "Уход")
                EnvelopeType.SAVINGS -> listOf("Копилка", "Вклад", "Мечта")
                EnvelopeType.WANTS -> listOf("Игры", "Сладости", "Хобби")
            }'''

if old_tags in code:
    code = code.replace(old_tags, new_tags)
    print("Tags replaced successfully!")
else:
    print("WARNING: old_tags not found exactly, will search partially")

# 3. Replace the button text to prevent clipping
old_btn = '''                    Text(
                        text = if (unallocated == 0) "Зафиксировать бюджет" else "Распределите все монеты ($unallocated монет)",
                        fontFamily = UnboundedFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (unallocated == 0) Color.White else TextSecondary
                    )'''

new_btn = '''                    Text(
                        text = if (unallocated == 0) "Зафиксировать бюджет" else "Осталось: ${FormatUtils.formatCoins(unallocated)}",
                        fontFamily = UnboundedFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (unallocated == 0) Color.White else TextSecondary
                    )'''

if old_btn in code:
    code = code.replace(old_btn, new_btn)
    print("Button text replaced successfully!")
else:
    print("WARNING: old_btn not found exactly")

# 4. Replace unallocated status banner text
old_status = '''                                Text(
                                    text = if (unallocated == 0) "Бюджет идеально сбалансирован!" else if (unallocated > 0) "Осталось распределить: $unallocated монет" else "Превышение лимита дохода: ${-unallocated} монет",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )'''

new_status = '''                                Text(
                                    text = if (unallocated == 0) "Бюджет идеально сбалансирован!" else if (unallocated > 0) "Осталось распределить: ${FormatUtils.formatCoins(unallocated)}" else "Превышение лимита дохода: ${FormatUtils.formatCoins(-unallocated)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )'''

if old_status in code:
    code = code.replace(old_status, new_status)
    print("Status banner replaced successfully!")
else:
    print("WARNING: old_status not found exactly")

# 5. Insert 3 Glass Jars above the segment bar
old_diagram = '''                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "БАЛАНС 50 / 30 / 20",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${((needsRatio + wantsRatio + savingsRatio) * 100).toInt()}% из 100%",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }'''

new_diagram = '''                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3 БАНКИ БЮДЖЕТА (50 / 30 / 20)",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${((needsRatio + wantsRatio + savingsRatio) * 100).toInt()}% из 100%",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            // Метафора 3 стеклянных банок с наполнением монетами
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                GlassBudgetJar(
                                    title = "Надо",
                                    targetPercent = 50,
                                    currentAmount = needsState.toInt(),
                                    ratio = needsRatio,
                                    color = BrandRoseWarm,
                                    emoji = "🥣"
                                )
                                GlassBudgetJar(
                                    title = "Хочу",
                                    targetPercent = 30,
                                    currentAmount = wantsState.toInt(),
                                    ratio = wantsRatio,
                                    color = BrandLavender,
                                    emoji = "🎾"
                                )
                                GlassBudgetJar(
                                    title = "Копилка",
                                    targetPercent = 20,
                                    currentAmount = savingsState.toInt(),
                                    ratio = savingsRatio,
                                    color = currentTheme.primaryColor,
                                    emoji = "🐖"
                                )
                            }'''

if old_diagram in code:
    code = code.replace(old_diagram, new_diagram)
    print("3 Glass Jars diagram inserted successfully!")
else:
    print("WARNING: old_diagram not found exactly")

# 6. Add GlassBudgetJar composable definition at the end of file
jar_composable = '''
/**
 * Стеклянная банка бюджета с визуализацией уровня наполнения
 */
@Composable
private fun GlassBudgetJar(
    title: String,
    targetPercent: Int,
    currentAmount: Int,
    ratio: Float,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Крышка банки
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .width(42.dp)
                .height(5.dp)
        ) {}

        // Корпус банки
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
            color = Color.White.copy(alpha = 0.9f),
            border = BorderStroke(1.5.dp, color.copy(alpha = 0.7f)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .width(68.dp)
                .height(84.dp)
        ) {
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
                // Заполнение монетами по высоте
                val fillFraction = ratio.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fillFraction)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.85f))
                            )
                        )
                )

                // Эмодзи и сумма
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                    Text(
                        text = "$currentAmount 🪙",
                        fontFamily = UnboundedFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            }
        }

        // Подпись под банкой
        Text(
            text = "$title $targetPercent%",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}
'''

code = code + '\n' + jar_composable

with open(r'app/src/main/java/ru/finpet/app/ui/screens/BudgetScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("BudgetScreen.kt updated successfully!")
