import os

with open(r'app/src/main/java/ru/finpet/app/ui/screens/QuestsScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Add Random import
if 'import java.util.Random' not in code:
    code = code.replace(
        'import kotlin.math.sin',
        'import kotlin.math.sin\nimport java.util.Random'
    )

# 2. Fix DuolingoQuestLessonScreen @Composable annotation
code = code.replace(
    '\nprivate fun DuolingoQuestLessonScreen(',
    '\n@Composable\nprivate fun DuolingoQuestLessonScreen('
)

# 3. Fix CoinFlyCelebration parameter name
code = code.replace(
    'coinsEarned = coins,',
    'rewardCoins = coins,'
)

# 4. Fix OutlineLight inside Canvas drawScope in SagaVerticalNode
old_canvas = '''                drawPath(
                    path = path,
                    color = if (isCompleted) StatGreenEmerald else OutlineLight,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = if (isCompleted) null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )'''

new_canvas = '''                val pathColor = if (isCompleted) StatGreenEmerald else Color(0xFFCBD5E1)
                drawPath(
                    path = path,
                    color = pathColor,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = if (isCompleted) null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )'''

code = code.replace(old_canvas, new_canvas)

# 5. Fix SagaSideQuestNode arrangement
code = code.replace(
    'horizontalArrangement = Alignment.Center,',
    'horizontalArrangement = Arrangement.Center,'
)

with open(r'app/src/main/java/ru/finpet/app/ui/screens/QuestsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("QuestsScreen.kt fixes applied successfully!")
