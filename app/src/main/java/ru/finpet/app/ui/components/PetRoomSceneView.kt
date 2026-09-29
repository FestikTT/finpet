package ru.finpet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameState
import ru.finpet.app.model.RoomSlotType
import ru.finpet.app.model.WallAwardsRepository
import java.util.Calendar

/**
 * Интерактивная 2.5D сцена комнаты питомца:
 * - Стены в перспективе с обоями из каталога
 * - Окно со временем суток и лучом света
 * - Дверь с золотой ручкой
 * - Плинтуса и настенный постер (диплом / арт)
 * - Изометрический пол и мягкий дизайнерский ковер
 * - Миска с едой и интерактивная игрушка питомца
 * - Анимация радости и игры при нажатии на игрушку или питомца
 */
@Composable
fun PetRoomSceneView(
    state: GameState,
    modifier: Modifier = Modifier,
    cornerRadius: Float = 24f,
    showDayNightBadge: Boolean = true,
    onAwardsClick: () -> Unit = {},
    onLampClick: () -> Unit = {},
    onWindowClick: () -> Unit = {},
    petContent: @Composable BoxScope.() -> Unit = {}
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isNight = currentHour in 21..23 || currentHour in 0..6 || state.isRoomLightOff
    val isSunset = (currentHour in 18..20) && !state.isRoomLightOff

    val infiniteTransition = rememberInfiniteTransition(label = "roomSceneAnim")
    val weatherProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weatherLoop"
    )
    val lampGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lampPulse"
    )

    var componentWidth by remember { mutableStateOf(1f) }
    var componentHeight by remember { mutableStateOf(1f) }

    val tapModifier = Modifier.pointerInput(state.isRoomLightOff, state.roomWeather) {
        detectTapGestures(
            onTap = { offset ->
                val w = componentWidth
                val h = componentHeight
                if (w > 0 && h > 0) {
                    val leftWallX = w * 0.14f
                    val rightWallX = w * 0.86f
                    val topWallY = h * 0.03f
                    val floorBackY = h * 0.44f
                    val floorFrontY = h * 0.58f

                    // 1. Проверка клика по настенной доске наград
                    val boardW = (rightWallX - leftWallX) * 0.32f
                    val boardH = (floorBackY - topWallY) * 0.82f
                    val boardLeft = leftWallX + (rightWallX - leftWallX) * 0.63f
                    val boardTop = topWallY + (floorBackY - topWallY) * 0.10f

                    if (offset.x in (boardLeft - 10f)..(boardLeft + boardW + 10f) &&
                        offset.y in (boardTop - 10f)..(boardTop + boardH + 10f)) {
                        SoundHapticManager.performClickHaptic()
                        onAwardsClick()
                        return@detectTapGestures
                    }

                    // 2. Проверка клика по настольной лампе (включение/выключение света)
                    val tableX = leftWallX * 0.78f
                    val tableW = (leftWallX * 0.75f).coerceIn(44f, 80f)
                    val tableY = floorBackY + (floorFrontY - floorBackY) * 0.18f
                    val tableDepth = tableW * 0.38f
                    val lampX = tableX + tableW * 0.5f + 3f
                    val lampBaseY = tableY + tableDepth * 0.18f

                    if (offset.x in (lampX - 35f)..(lampX + 35f) &&
                        offset.y in (lampBaseY - 55f)..(lampBaseY + 25f)) {
                        onLampClick()
                        return@detectTapGestures
                    }

                    // 3. Проверка клика по окну (смена погоды за окном)
                    val winW = (rightWallX - leftWallX) * 0.24f
                    val winH = (floorBackY - topWallY) * 0.42f
                    val winLeft = leftWallX + (rightWallX - leftWallX) * 0.06f
                    val winTop = topWallY + (floorBackY - topWallY) * 0.08f

                    if (offset.x in (winLeft - 12f)..(winLeft + winW + 12f) &&
                        offset.y in (winTop - 12f)..(winTop + winH + 12f)) {
                        onWindowClick()
                        return@detectTapGestures
                    }
                }
            }
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .onGloballyPositioned { coordinates ->
                componentWidth = coordinates.size.width.toFloat()
                componentHeight = coordinates.size.height.toFloat()
            }
            .then(tapModifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Геометрия 2.5D перспективы комнаты
            val leftWallX = w * 0.14f
            val rightWallX = w * 0.86f
            val topWallY = h * 0.03f
            val floorBackY = h * 0.44f
            val floorFrontY = h * 0.58f

            // 1. Центральная стена (задник)
            drawBackWall(
                state = state,
                leftX = leftWallX,
                rightX = rightWallX,
                topY = topWallY,
                bottomY = floorBackY
            )

            // 2. Левая стена (в перспективе под углом)
            drawLeftWall(
                state = state,
                leftX = 0f,
                rightX = leftWallX,
                topOuterY = 0f,
                topInnerY = topWallY,
                bottomOuterY = floorFrontY,
                bottomInnerY = floorBackY
            )

            // 3. Правая стена (в перспективе под углом)
            drawRightWall(
                state = state,
                leftX = rightWallX,
                rightX = w,
                topInnerY = topWallY,
                topOuterY = 0f,
                bottomInnerY = floorBackY,
                bottomOuterY = floorFrontY
            )

            // 4. Окно на центральной стене с динамической погодой
            val winW = (rightWallX - leftWallX) * 0.24f
            val winH = (floorBackY - topWallY) * 0.42f
            val winLeft = leftWallX + (rightWallX - leftWallX) * 0.06f
            val winTop = topWallY + (floorBackY - topWallY) * 0.08f

            drawWindow(
                isNight = isNight,
                isSunset = isSunset,
                winLeft = winLeft,
                winTop = winTop,
                winW = winW,
                winH = winH,
                weather = state.roomWeather,
                weatherProgress = weatherProgress
            )

            // 5. Дверь по центру задней стены
            val doorW = (rightWallX - leftWallX) * 0.23f
            val doorH = (floorBackY - topWallY) * 0.84f
            val doorLeft = leftWallX + (rightWallX - leftWallX - doorW) / 2f
            val doorTop = floorBackY - doorH

            drawDoor(
                doorLeft = doorLeft,
                doorTop = doorTop,
                doorW = doorW,
                doorH = doorH
            )

            // 6. Постер / Настенный декор
            if (state.equippedPoster != null) {
                val posterW = (rightWallX - leftWallX) * 0.22f
                val posterH = (floorBackY - topWallY) * 0.42f
                val posterLeft = w * state.posterX
                val posterTop = h * state.posterY

                drawPoster(
                    posterId = state.equippedPoster,
                    posterLeft = posterLeft,
                    posterTop = posterTop,
                    posterW = posterW,
                    posterH = posterH
                )
            }

            // 7. Плинтуса по контуру пола и внутренние углы стен
            drawSkirtingAndCorners(
                w = w,
                leftWallX = leftWallX,
                rightWallX = rightWallX,
                topWallY = topWallY,
                floorBackY = floorBackY,
                floorFrontY = floorFrontY
            )

            // 8. Изометрический пол
            drawFloor(
                state = state,
                w = w,
                h = h,
                leftWallX = leftWallX,
                rightWallX = rightWallX,
                floorBackY = floorBackY,
                floorFrontY = floorFrontY
            )

            // 9. Динамический световой луч из окна на пол (днем и на закате)
            if (!isNight) {
                drawWindowSunbeam(
                    winLeft = winLeft,
                    winTop = winTop,
                    winW = winW,
                    winH = winH,
                    isSunset = isSunset,
                    h = h
                )
            }

            // 10. Уютная скандинавская мебель: настенные часы, полка с книгами, тумбочка с лампой и комнатная монстера
            drawCozyFurniture(
                w = w,
                leftWallX = leftWallX,
                rightWallX = rightWallX,
                topWallY = topWallY,
                floorBackY = floorBackY,
                floorFrontY = floorFrontY,
                isNight = isNight
            )

            // 11. Миска с едой в левой зоне комнаты
            drawPetFoodCorner(
                w = w,
                h = h,
                hunger = state.pet.hunger
            )

            // 11. Настенная доска наград (дипломы, грамоты, сертификаты)
            val boardW = (rightWallX - leftWallX) * 0.32f
            val boardH = (floorBackY - topWallY) * 0.82f
            val boardLeft = leftWallX + (rightWallX - leftWallX) * 0.63f
            val boardTop = topWallY + (floorBackY - topWallY) * 0.10f

            drawWallAwardsBoard(
                state = state,
                boardLeft = boardLeft,
                boardTop = boardTop,
                boardW = boardW,
                boardH = boardH
            )

            // 12. Атмосферное затемнение комнаты в режиме сна (Выключен свет)
            if (state.isRoomLightOff) {
                // Ночная уютная сине-фиолетовая полутень
                drawRect(Color(0x9E0B1120))

                // Мягкое янтарное свечение вокруг настольной лампы-ночника
                val tableX = leftWallX * 0.78f
                val tableW = (leftWallX * 0.75f).coerceIn(44f, 80f)
                val tableY = floorBackY + (floorFrontY - floorBackY) * 0.18f
                val tableDepth = tableW * 0.38f
                val lampX = tableX + tableW * 0.5f + 3f
                val lampBaseY = tableY + tableDepth * 0.18f

                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xAAF59E0B),
                            Color(0x40F59E0B),
                            Color.Transparent
                        ),
                        center = Offset(lampX, lampBaseY - 26f),
                        radius = 160f * lampGlowPulse
                    ),
                    radius = 160f * lampGlowPulse,
                    center = Offset(lampX, lampBaseY - 26f)
                )

                // Лунный свет сквозь окно
                val moonbeamPath = Path().apply {
                    moveTo(winLeft, winTop + winH)
                    lineTo(winLeft + winW, winTop + winH)
                    lineTo(winLeft + winW * 1.8f, h * 0.85f)
                    lineTo(winLeft + winW * 0.4f, h * 0.85f)
                    close()
                }
                drawPath(
                    moonbeamPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x2860A5FA), Color.Transparent),
                        startY = winTop + winH,
                        endY = h * 0.85f
                    )
                )
            }
        }

        // Индикатор времени суток и погоды
        if (showDayNightBadge) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xDD1E293B),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when {
                            state.isRoomLightOff -> "🌙 Сон (ночник) • ${state.roomWeather.icon}"
                            isNight -> "🌙 Ночь ($currentHour:00) • ${state.roomWeather.icon}"
                            isSunset -> "🌅 Закат ($currentHour:00) • ${state.roomWeather.icon}"
                            else -> "☀️ День ($currentHour:00) • ${state.roomWeather.icon}"
                        },
                        fontSize = 10.5.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Контент питомца в комнате (располагается поверх пола и ковра)
        petContent()
    }
}

// ==============================================================================
// Вспомогательные функции рендеринга 2.5D сцены
// ==============================================================================

private fun DrawScope.drawBackWall(
    state: GameState,
    leftX: Float,
    rightX: Float,
    topY: Float,
    bottomY: Float
) {
    val wallW = rightX - leftX
    val wallH = bottomY - topY

    val bgBrush = when (state.equippedWallpaper) {
        "sh_wp_brick" -> Brush.verticalGradient(listOf(Color(0xFF8D493A), Color(0xFF6B3326)), topY, bottomY)
        "sh_wp_space" -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311042)), topY, bottomY)
        "sh_wp_clouds" -> Brush.verticalGradient(listOf(Color(0xFFBAE6FD), Color(0xFFDDD6FE), Color(0xFFFBCFE8)), topY, bottomY)
        "sh_wp_cyber" -> Brush.verticalGradient(listOf(Color(0xFF0A0F1D), Color(0xFF0D182E)), topY, bottomY)
        "sh_wp_sunflower" -> Brush.verticalGradient(listOf(Color(0xFFFEF08A), Color(0xFFFDE047), Color(0xFFFACC15)), topY, bottomY)
        "sh_wp_sakura" -> Brush.verticalGradient(listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8), Color(0xFFF472B6)), topY, bottomY)
        "sh_wp_retro" -> Brush.verticalGradient(listOf(Color(0xFF312E81), Color(0xFF4C1D95), Color(0xFF831843)), topY, bottomY)
        "sh_wp_forest" -> Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF047857)), topY, bottomY)
        "sh_wp_city" -> Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4C1D95), Color(0xFF701A75)), topY, bottomY)
        else -> Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8)), topY, bottomY)
    }

    drawRect(
        brush = bgBrush,
        topLeft = Offset(leftX, topY),
        size = Size(wallW, wallH)
    )

    when (state.equippedWallpaper) {
        "sh_wp_brick" -> {
            val rowH = 16f
            var y = topY
            var rowIdx = 0
            while (y < bottomY) {
                drawLine(Color(0x38000000), Offset(leftX, y), Offset(rightX, y), strokeWidth = 1.5f)
                val brickW = 28f
                val shift = if (rowIdx % 2 == 0) 0f else brickW * 0.5f
                var bx = leftX + shift
                while (bx < rightX) {
                    drawLine(Color(0x28000000), Offset(bx, y), Offset(bx, y + rowH), strokeWidth = 1.2f)
                    bx += brickW
                }
                y += rowH
                rowIdx++
            }
        }
        "sh_wp_space" -> {
            // Звезды разного размера и сияние туманности
            drawCircle(Color(0x40A855F7), wallW * 0.35f, Offset(leftX + wallW * 0.4f, topY + wallH * 0.5f))
            drawCircle(Color.White.copy(alpha = 0.9f), 2.8f, Offset(leftX + wallW * 0.15f, topY + wallH * 0.25f))
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.9f), 3.2f, Offset(leftX + wallW * 0.85f, topY + wallH * 0.18f))
            drawCircle(Color.White.copy(alpha = 0.65f), 1.8f, Offset(leftX + wallW * 0.40f, topY + wallH * 0.15f))
            drawCircle(Color(0xFF67E8F9).copy(alpha = 0.85f), 2.4f, Offset(leftX + wallW * 0.70f, topY + wallH * 0.35f))
            drawCircle(Color.White.copy(alpha = 0.75f), 1.5f, Offset(leftX + wallW * 0.25f, topY + wallH * 0.60f))
        }
        "sh_wp_cyber" -> {
            // Кибернетическая матрица с неоновыми дорожками
            var gx = leftX
            while (gx < rightX) {
                drawLine(Color(0x2500D2FF), Offset(gx, topY), Offset(gx, bottomY), 1.5f)
                gx += 24f
            }
            var gy = topY + 12f
            while (gy < bottomY) {
                drawLine(Color(0x1800D2FF), Offset(leftX, gy), Offset(rightX, gy), 1f)
                gy += 20f
            }
            drawCircle(Color(0xFF00E5FF), 3f, Offset(leftX + wallW * 0.35f, topY + wallH * 0.4f))
            drawCircle(Color(0xFFFF007F), 3f, Offset(leftX + wallW * 0.65f, topY + wallH * 0.3f))
        }
        "sh_wp_sakura" -> {
            // Ветки сакуры и нежные лепестки
            drawLine(Color(0xFF5D4037), Offset(leftX, topY + wallH * 0.25f), Offset(leftX + wallW * 0.3f, topY + wallH * 0.18f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawLine(Color(0xFF5D4037), Offset(rightX, topY + wallH * 0.22f), Offset(rightX - wallW * 0.35f, topY + wallH * 0.32f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFFFB6C1), 4.5f, Offset(leftX + wallW * 0.18f, topY + wallH * 0.20f))
            drawCircle(Color(0xFFFFC0CB), 3.5f, Offset(leftX + wallW * 0.28f, topY + wallH * 0.16f))
            drawCircle(Color(0xFFFFB6C1), 4.2f, Offset(rightX - wallW * 0.22f, topY + wallH * 0.28f))
            drawCircle(Color(0xFFF472B6), 3.2f, Offset(rightX - wallW * 0.14f, topY + wallH * 0.22f))
            drawCircle(Color(0xFFFFE4E6), 2.5f, Offset(leftX + wallW * 0.45f, topY + wallH * 0.45f))
        }
        "sh_wp_forest" -> {
            // Силуэты хвойных деревьев и светлячки
            drawCircle(Color(0xFF064E3B), wallW * 0.15f, Offset(leftX + wallW * 0.18f, bottomY))
            drawCircle(Color(0xFF065F46), wallW * 0.18f, Offset(leftX + wallW * 0.82f, bottomY))
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.9f), 2.5f, Offset(leftX + wallW * 0.25f, topY + wallH * 0.35f))
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.8f), 2f, Offset(leftX + wallW * 0.75f, topY + wallH * 0.45f))
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.7f), 2f, Offset(leftX + wallW * 0.50f, topY + wallH * 0.25f))
        }
        "sh_wp_city" -> {
            // Ночной мегаполис: силуэты небоскребов и светящиеся окна
            val b1 = leftX + wallW * 0.10f
            drawRect(Color(0xFF1E1B4B), Offset(b1, bottomY - wallH * 0.45f), Size(wallW * 0.18f, wallH * 0.45f))
            drawRect(Color(0xFFFACC15), Offset(b1 + 6f, bottomY - wallH * 0.38f), Size(5f, 7f))
            drawRect(Color(0xFF38BDF8), Offset(b1 + 16f, bottomY - wallH * 0.38f), Size(5f, 7f))
            drawRect(Color(0xFFFACC15), Offset(b1 + 6f, bottomY - wallH * 0.24f), Size(5f, 7f))

            val b2 = leftX + wallW * 0.32f
            drawRect(Color(0xFF17153B), Offset(b2, bottomY - wallH * 0.60f), Size(wallW * 0.22f, wallH * 0.60f))
            drawRect(Color(0xFFFACC15), Offset(b2 + 8f, bottomY - wallH * 0.52f), Size(6f, 8f))
            drawRect(Color(0xFFFACC15), Offset(b2 + 20f, bottomY - wallH * 0.52f), Size(6f, 8f))
            drawRect(Color(0xFF38BDF8), Offset(b2 + 8f, bottomY - wallH * 0.38f), Size(6f, 8f))
            drawRect(Color(0xFFFACC15), Offset(b2 + 20f, bottomY - wallH * 0.24f), Size(6f, 8f))

            val b3 = leftX + wallW * 0.72f
            drawRect(Color(0xFF1E1B4B), Offset(b3, bottomY - wallH * 0.50f), Size(wallW * 0.20f, wallH * 0.50f))
            drawRect(Color(0xFF38BDF8), Offset(b3 + 7f, bottomY - wallH * 0.42f), Size(5f, 7f))
            drawRect(Color(0xFFFACC15), Offset(b3 + 17f, bottomY - wallH * 0.42f), Size(5f, 7f))
            drawRect(Color(0xFFFACC15), Offset(b3 + 7f, bottomY - wallH * 0.26f), Size(5f, 7f))
        }
        "sh_wp_retro" -> {
            // Ретро-вейв: синтвейв закатное неоновое солнце и полосы
            val sunRadius = wallW * 0.16f
            val sunCenter = Offset(leftX + wallW * 0.50f, bottomY - wallH * 0.15f)
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFACC15), Color(0xFFF43F5E), Color(0xFF8B5CF6)),
                    startY = sunCenter.y - sunRadius,
                    endY = sunCenter.y + sunRadius
                ),
                radius = sunRadius,
                center = sunCenter
            )
            // Горизонтальные ретро-прорези в солнце
            for (i in 0..4) {
                val lineY = sunCenter.y - sunRadius * 0.2f + (i * 7f)
                drawLine(Color(0xFF28256A), Offset(sunCenter.x - sunRadius, lineY), Offset(sunCenter.x + sunRadius, lineY), strokeWidth = 2.5f)
            }
        }
        "sh_wp_clouds" -> {
            // Нежные пастельные пушистые облака
            val cloudColor = Color.White.copy(alpha = 0.85f)
            // Облако 1
            drawCircle(cloudColor, 12f, Offset(leftX + wallW * 0.25f, topY + wallH * 0.22f))
            drawCircle(cloudColor, 16f, Offset(leftX + wallW * 0.30f, topY + wallH * 0.20f))
            drawCircle(cloudColor, 11f, Offset(leftX + wallW * 0.35f, topY + wallH * 0.22f))
            // Облако 2
            drawCircle(cloudColor, 14f, Offset(leftX + wallW * 0.72f, topY + wallH * 0.30f))
            drawCircle(cloudColor, 18f, Offset(leftX + wallW * 0.78f, topY + wallH * 0.27f))
            drawCircle(cloudColor, 12f, Offset(leftX + wallW * 0.84f, topY + wallH * 0.30f))
        }
        "sh_wp_sunflower" -> {
            // Яркие цветы подсолнухов с коричневой сердцевиной
            val f1 = Offset(leftX + wallW * 0.22f, topY + wallH * 0.35f)
            drawCircle(Color(0xFFFACC15), 14f, f1)
            drawCircle(Color(0xFF78350F), 7f, f1)
            val f2 = Offset(leftX + wallW * 0.78f, topY + wallH * 0.25f)
            drawCircle(Color(0xFFFACC15), 16f, f2)
            drawCircle(Color(0xFF78350F), 8f, f2)
            val f3 = Offset(leftX + wallW * 0.50f, topY + wallH * 0.52f)
            drawCircle(Color(0xFFFBBF24), 12f, f3)
            drawCircle(Color(0xFF78350F), 6f, f3)
        }
        else -> {}
    }
}

private fun DrawScope.drawLeftWall(
    state: GameState,
    leftX: Float,
    rightX: Float,
    topOuterY: Float,
    topInnerY: Float,
    bottomOuterY: Float,
    bottomInnerY: Float
) {
    val wallPath = Path().apply {
        moveTo(leftX, topOuterY)
        lineTo(rightX, topInnerY)
        lineTo(rightX, bottomInnerY)
        lineTo(leftX, bottomOuterY)
        close()
    }

    val baseColor = when (state.equippedWallpaper) {
        "sh_wp_brick" -> Color(0xFF6B3326)
        "sh_wp_space" -> Color(0xFF131131)
        "sh_wp_clouds" -> Color(0xFFBAE6FD)
        "sh_wp_cyber" -> Color(0xFF080D1A)
        "sh_wp_sunflower" -> Color(0xFFEAB308)
        "sh_wp_sakura" -> Color(0xFFF472B6)
        "sh_wp_retro" -> Color(0xFF28256A)
        "sh_wp_forest" -> Color(0xFF047857)
        "sh_wp_city" -> Color(0xFF1E1B4B)
        else -> Color(0xFF94A3B8)
    }

    drawPath(path = wallPath, color = baseColor)
    drawPath(
        path = wallPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0x44000000), Color(0x18000000)),
            startX = leftX,
            endX = rightX
        )
    )
}

private fun DrawScope.drawRightWall(
    state: GameState,
    leftX: Float,
    rightX: Float,
    topInnerY: Float,
    topOuterY: Float,
    bottomInnerY: Float,
    bottomOuterY: Float
) {
    val wallPath = Path().apply {
        moveTo(leftX, topInnerY)
        lineTo(rightX, topOuterY)
        lineTo(rightX, bottomOuterY)
        lineTo(leftX, bottomInnerY)
        close()
    }

    val baseColor = when (state.equippedWallpaper) {
        "sh_wp_brick" -> Color(0xFF7A3C2E)
        "sh_wp_space" -> Color(0xFF18153D)
        "sh_wp_clouds" -> Color(0xFFDDD6FE)
        "sh_wp_cyber" -> Color(0xFF0A1022)
        "sh_wp_sunflower" -> Color(0xFFFACC15)
        "sh_wp_sakura" -> Color(0xFFFBCFE8)
        "sh_wp_retro" -> Color(0xFF381D6E)
        "sh_wp_forest" -> Color(0xFF056A4E)
        "sh_wp_city" -> Color(0xFF282463)
        else -> Color(0xFFAAB8CB)
    }

    drawPath(path = wallPath, color = baseColor)
    drawPath(
        path = wallPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0x15000000), Color(0x35000000)),
            startX = leftX,
            endX = rightX
        )
    )
}

private fun DrawScope.drawSkirtingAndCorners(
    w: Float,
    leftWallX: Float,
    rightWallX: Float,
    topWallY: Float,
    floorBackY: Float,
    floorFrontY: Float
) {
    drawLine(
        color = Color(0x40000000),
        start = Offset(leftWallX, topWallY),
        end = Offset(leftWallX, floorBackY),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0x40000000),
        start = Offset(rightWallX, topWallY),
        end = Offset(rightWallX, floorBackY),
        strokeWidth = 2f
    )

    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(leftWallX, floorBackY - 5f),
        size = Size(rightWallX - leftWallX, 5f)
    )

    val leftSkirting = Path().apply {
        moveTo(0f, floorFrontY - 5f)
        lineTo(leftWallX, floorBackY - 5f)
        lineTo(leftWallX, floorBackY)
        lineTo(0f, floorFrontY)
        close()
    }
    drawPath(leftSkirting, Color(0xFF1E293B))

    val rightSkirting = Path().apply {
        moveTo(rightWallX, floorBackY - 5f)
        lineTo(w, floorFrontY - 5f)
        lineTo(w, floorFrontY)
        lineTo(rightWallX, floorBackY)
        close()
    }
    drawPath(rightSkirting, Color(0xFF1E293B))
}

private fun DrawScope.drawFloor(
    state: GameState,
    w: Float,
    h: Float,
    leftWallX: Float,
    rightWallX: Float,
    floorBackY: Float,
    floorFrontY: Float
) {
    val floorPath = Path().apply {
        moveTo(0f, floorFrontY)
        lineTo(leftWallX, floorBackY)
        lineTo(rightWallX, floorBackY)
        lineTo(w, floorFrontY)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    val isWoolCarpet = state.equippedFloor == "sh_dec_carpet"
    val floorColors = when (state.equippedFloor) {
        "sh_fl_dark" -> listOf(Color(0xFF3E2723), Color(0xFF24140E))
        "sh_fl_tatami" -> listOf(Color(0xFFC4B889), Color(0xFF9E8F5E))
        "sh_fl_marble" -> listOf(Color(0xFFF1F5F9), Color(0xFFB4C2D3))
        "sh_fl_neon" -> listOf(Color(0xFF0F172A), Color(0xFF151D3B))
        "sh_fl_carpet" -> listOf(Color(0xFFFCE7F3), Color(0xFFF472B6))
        "sh_dec_carpet" -> listOf(Color(0xFFE2C9A5), Color(0xFFB58A63))
        else -> listOf(Color(0xFFD4A373), Color(0xFFA07248))
    }

    drawPath(
        path = floorPath,
        brush = Brush.verticalGradient(
            colors = floorColors,
            startY = floorBackY,
            endY = h
        )
    )

    // Уникальная текстура пола в зависимости от выбранного покрытия
    val backW = rightWallX - leftWallX
    when (state.equippedFloor) {
        "sh_fl_tatami" -> {
            // Японские татами с темно-зеленой каймой из ткани
            val matCols = 4
            for (i in 0..matCols) {
                val topX = leftWallX + (backW / matCols) * i
                val bottomX = (w / matCols) * i
                drawLine(Color(0xFF285A35), Offset(topX, floorBackY), Offset(bottomX, h), strokeWidth = 3f)
            }
            // Горизонтальные швы татами
            val seams = 3
            for (j in 1..seams) {
                val t = j.toFloat() / (seams + 1)
                val sy = floorBackY + (h - floorBackY) * t
                val sx1 = leftWallX * (1f - t)
                val sx2 = rightWallX + (w - rightWallX) * t
                drawLine(Color(0xFF285A35), Offset(sx1, sy), Offset(sx2, sy), strokeWidth = 2.5f)
            }
        }
        "sh_fl_marble" -> {
            // Мраморная плитка с благородными прожилками
            val tileCount = 5
            for (i in 0..tileCount) {
                val topX = leftWallX + (backW / tileCount) * i
                val bottomX = (w / tileCount) * i
                drawLine(Color(0x4094A3B8), Offset(topX, floorBackY), Offset(bottomX, h), strokeWidth = 1.2f)
            }
            // Мраморные прожилки
            drawLine(Color(0x2864748B), Offset(w * 0.25f, floorBackY + 20f), Offset(w * 0.35f, h * 0.8f), strokeWidth = 1.5f, cap = StrokeCap.Round)
            drawLine(Color(0x20D4AF37), Offset(w * 0.65f, floorBackY + 15f), Offset(w * 0.80f, h * 0.75f), strokeWidth = 1.2f, cap = StrokeCap.Round)
        }
        "sh_fl_neon" -> {
            // Неоновая киберпанк сетка с неоновым свечением
            val gridCols = 6
            for (i in 0..gridCols) {
                val topX = leftWallX + (backW / gridCols) * i
                val bottomX = (w / gridCols) * i
                drawLine(Color(0x7500E5FF), Offset(topX, floorBackY), Offset(bottomX, h), strokeWidth = 1.8f)
            }
            val gridRows = 4
            for (j in 1..gridRows) {
                val t = j.toFloat() / (gridRows + 1)
                val sy = floorBackY + (h - floorBackY) * t
                val sx1 = leftWallX * (1f - t)
                val sx2 = rightWallX + (w - rightWallX) * t
                drawLine(Color(0x6000E5FF), Offset(sx1, sy), Offset(sx2, sy), strokeWidth = 1.5f)
                // Неоновые светящиеся узлы
                drawCircle(Color(0xFFFF007F), 2.5f, Offset((sx1 + sx2) * 0.5f, sy))
            }
        }
        "sh_fl_carpet" -> {
            // Мягкое пушистое ковровое покрытие
            for (stepX in 1..6) {
                val topX = leftWallX + (backW / 7f) * stepX
                val bottomX = (w / 7f) * stepX
                drawLine(Color(0x20F43F5E), Offset(topX, floorBackY), Offset(bottomX, h), strokeWidth = 1f)
            }
        }
        else -> {
            // Классический паркет / деревянные доски
            val plankCount = 8
            for (i in 0..plankCount) {
                val topX = leftWallX + (backW / plankCount) * i
                val bottomX = (w / plankCount) * i
                drawLine(
                    color = Color(0x30000000),
                    start = Offset(topX, floorBackY),
                    end = Offset(bottomX, h),
                    strokeWidth = 1.6f
                )
            }
        }
    }

    // Изометрический коврик по центру под питомцем (сдвинут вниз, чтобы питомец сидел четко по центру коврика)
    val rugCenter = Offset(w * 0.5f, floorBackY + (h - floorBackY) * 0.66f)
    val rugW = w * 0.54f
    val rugH = (h - floorBackY) * 0.52f

    // Тень под ковриком
    drawOval(
        color = Color(0x25000000),
        topLeft = Offset(rugCenter.x - rugW * 0.52f, rugCenter.y - rugH * 0.48f),
        size = Size(rugW * 1.04f, rugH * 1.04f)
    )

    if (isWoolCarpet) {
        val fringeCount = 36
        for (i in 0 until fringeCount) {
            val angle = (i.toFloat() / fringeCount) * 2f * Math.PI.toFloat()
            val cosA = kotlin.math.cos(angle)
            val sinA = kotlin.math.sin(angle)
            val rx1 = rugCenter.x + cosA * (rugW * 0.50f)
            val ry1 = rugCenter.y + sinA * (rugH * 0.50f)
            val rx2 = rugCenter.x + cosA * (rugW * 0.525f)
            val ry2 = rugCenter.y + sinA * (rugH * 0.525f)
            drawLine(
                color = Color(0xFFFEF3C7),
                start = Offset(rx1, ry1),
                end = Offset(rx2, ry2),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF991B1B), Color(0xFF5A0C0C)),
                center = rugCenter,
                radius = rugW * 0.5f
            ),
            topLeft = Offset(rugCenter.x - rugW * 0.5f, rugCenter.y - rugH * 0.5f),
            size = Size(rugW, rugH)
        )

        drawOval(
            color = Color(0xFFF59E0B),
            topLeft = Offset(rugCenter.x - rugW * 0.45f, rugCenter.y - rugH * 0.45f),
            size = Size(rugW * 0.90f, rugH * 0.90f),
            style = Stroke(width = 3.5f)
        )
    } else {
        if (state.equippedFloor == "sh_fl_carpet") {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)),
                    center = rugCenter,
                    radius = rugW * 0.5f
                ),
                topLeft = Offset(rugCenter.x - rugW * 0.5f, rugCenter.y - rugH * 0.5f),
                size = Size(rugW, rugH)
            )
            drawOval(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(rugCenter.x - rugW * 0.45f, rugCenter.y - rugH * 0.45f),
                size = Size(rugW * 0.90f, rugH * 0.90f),
                style = Stroke(width = 2f)
            )
        } else if (state.equippedFloor == "sh_fl_neon") {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4400E5FF), Color(0x2200D2FF)),
                    center = rugCenter,
                    radius = rugW * 0.5f
                ),
                topLeft = Offset(rugCenter.x - rugW * 0.5f, rugCenter.y - rugH * 0.5f),
                size = Size(rugW, rugH)
            )
        } else {
            // Уютный стильный ковер по умолчанию
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFAF7F0), Color(0xFFEFE8DB), Color(0xFFDFD5C2)),
                    center = rugCenter,
                    radius = rugW * 0.5f
                ),
                topLeft = Offset(rugCenter.x - rugW * 0.5f, rugCenter.y - rugH * 0.5f),
                size = Size(rugW, rugH)
            )
            drawOval(
                color = Color(0xFFC4B59D),
                topLeft = Offset(rugCenter.x - rugW * 0.45f, rugCenter.y - rugH * 0.45f),
                size = Size(rugW * 0.90f, rugH * 0.90f),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
            )
            drawOval(
                color = Color(0xFFF5EFE6),
                topLeft = Offset(rugCenter.x - rugW * 0.32f, rugCenter.y - rugH * 0.32f),
                size = Size(rugW * 0.64f, rugH * 0.64f)
            )
        }
    }
}

private fun DrawScope.drawWindow(
    isNight: Boolean,
    isSunset: Boolean,
    winLeft: Float,
    winTop: Float,
    winW: Float,
    winH: Float,
    weather: ru.finpet.app.model.RoomWeather,
    weatherProgress: Float
) {
    val skyColors = when {
        isNight -> listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF2E1065))
        isSunset -> listOf(Color(0xFFEA580C), Color(0xFFDB2777), Color(0xFF6366F1))
        weather == ru.finpet.app.model.RoomWeather.RAIN -> listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B))
        weather == ru.finpet.app.model.RoomWeather.SNOW -> listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569))
        else -> listOf(Color(0xFF38BDF8), Color(0xFF7DD3FC), Color(0xFFBAE6FD))
    }

    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(winLeft - 3f, winTop - 3f),
        size = Size(winW + 6f, winH + 6f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = skyColors,
            startY = winTop,
            endY = winTop + winH
        ),
        topLeft = Offset(winLeft, winTop),
        size = Size(winW, winH),
        cornerRadius = CornerRadius(6f, 6f)
    )

    if (isNight) {
        drawCircle(Color(0xFFFEF08A), 7f, Offset(winLeft + winW * 0.72f, winTop + winH * 0.32f))
        drawCircle(Color(0xFF1E1B4B), 5.5f, Offset(winLeft + winW * 0.69f, winTop + winH * 0.30f))
        drawCircle(Color.White.copy(alpha = 0.9f), 1.5f, Offset(winLeft + winW * 0.25f, winTop + winH * 0.28f))
        drawCircle(Color.White.copy(alpha = 0.8f), 1.2f, Offset(winLeft + winW * 0.45f, winTop + winH * 0.48f))
    } else if (isSunset) {
        drawCircle(Color(0xFFFDE047), 9f, Offset(winLeft + winW * 0.5f, winTop + winH * 0.68f))
    } else if (weather == ru.finpet.app.model.RoomWeather.SUNNY) {
        drawCircle(Color(0xFFFACC15), 8.5f, Offset(winLeft + winW * 0.75f, winTop + winH * 0.28f))
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(winLeft + winW * 0.16f, winTop + winH * 0.46f),
            size = Size(winW * 0.46f, 6.5f),
            cornerRadius = CornerRadius(3.5f, 3.5f)
        )
    }

    // Динамическая погода за стеклом
    when (weather) {
        ru.finpet.app.model.RoomWeather.RAIN -> {
            for (i in 0 until 18) {
                val seedX = ((i * 37) % 91) / 100f
                val rx = winLeft + winW * (0.05f + 0.90f * seedX)
                val speed = 1.0f + (i % 3) * 0.35f
                val ry = winTop + ((i * 17f + weatherProgress * winH * 2.0f * speed) % (winH - 4f))
                val streakLen = 8f + (i % 4) * 3f
                drawLine(
                    color = Color.White.copy(alpha = 0.55f + (i % 3) * 0.15f),
                    start = Offset(rx, ry),
                    end = Offset(rx - 2.5f, ry + streakLen),
                    strokeWidth = 1.4f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(Color.White.copy(alpha = 0.5f), 1.8f, Offset(winLeft + winW * 0.24f, winTop + winH * 0.38f))
            drawCircle(Color.White.copy(alpha = 0.6f), 1.3f, Offset(winLeft + winW * 0.64f, winTop + winH * 0.62f))
            drawCircle(Color.White.copy(alpha = 0.45f), 1.5f, Offset(winLeft + winW * 0.82f, winTop + winH * 0.28f))
        }
        ru.finpet.app.model.RoomWeather.SNOW -> {
            for (i in 0 until 16) {
                val seedX = ((i * 41) % 93) / 100f
                val wobble = kotlin.math.sin((weatherProgress * 2.0 * Math.PI + i).toDouble()).toFloat() * 3.5f
                val sx = (winLeft + winW * (0.05f + 0.90f * seedX) + wobble).coerceIn(winLeft + 2f, winLeft + winW - 2f)
                val sy = winTop + ((i * 23f + weatherProgress * winH * 0.9f) % (winH - 3f))
                val radius = 1.4f + (i % 3) * 0.7f
                drawCircle(
                    color = Color.White.copy(alpha = 0.80f + (i % 3) * 0.10f),
                    radius = radius,
                    center = Offset(sx, sy)
                )
            }
        }
        ru.finpet.app.model.RoomWeather.SUNNY -> {
            drawLine(
                brush = Brush.linearGradient(listOf(Color(0x45FFFFFF), Color.Transparent)),
                start = Offset(winLeft + winW * 0.1f, winTop + winH * 0.1f),
                end = Offset(winLeft + winW * 0.45f, winTop + winH * 0.85f),
                strokeWidth = 2f
            )
        }
    }

    drawLine(Color(0xFF334155), Offset(winLeft, winTop + winH * 0.5f), Offset(winLeft + winW, winTop + winH * 0.5f), 1.6f)
    drawLine(Color(0xFF334155), Offset(winLeft + winW * 0.5f, winTop), Offset(winLeft + winW * 0.5f, winTop + winH), 1.6f)

    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(winLeft - 5f, winTop + winH),
        size = Size(winW + 10f, 4f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

private fun DrawScope.drawWindowSunbeam(
    winLeft: Float,
    winTop: Float,
    winW: Float,
    winH: Float,
    isSunset: Boolean,
    h: Float
) {
    val beamPath = Path().apply {
        moveTo(winLeft, winTop + winH)
        lineTo(winLeft + winW, winTop + winH)
        lineTo(winLeft + winW * 2.8f, h * 0.96f)
        lineTo(winLeft + winW * 0.8f, h * 0.96f)
        close()
    }

    val beamBrush = if (isSunset) {
        Brush.verticalGradient(
            colors = listOf(Color(0x35FB923C), Color(0x18EA580C), Color.Transparent),
            startY = winTop + winH,
            endY = h * 0.96f
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0x35FEF08A), Color(0x14FDE047), Color.Transparent),
            startY = winTop + winH,
            endY = h * 0.96f
        )
    }

    drawPath(path = beamPath, brush = beamBrush)
}

private fun DrawScope.drawDoor(
    doorLeft: Float,
    doorTop: Float,
    doorW: Float,
    doorH: Float
) {
    drawRoundRect(
        color = Color(0x28000000),
        topLeft = Offset(doorLeft - 4f, doorTop - 4f),
        size = Size(doorW + 8f, doorH + 4f),
        cornerRadius = CornerRadius(7f, 7f)
    )
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(doorLeft - 2f, doorTop - 2f),
        size = Size(doorW + 4f, doorH + 2f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
            startY = doorTop,
            endY = doorTop + doorH
        ),
        topLeft = Offset(doorLeft, doorTop),
        size = Size(doorW, doorH),
        cornerRadius = CornerRadius(4f, 4f)
    )

    val panelMargin = doorW * 0.12f
    val panelW = doorW - panelMargin * 2f
    val panelH1 = doorH * 0.38f
    val panelH2 = doorH * 0.44f

    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(doorLeft + panelMargin, doorTop + doorH * 0.08f),
        size = Size(panelW, panelH1),
        cornerRadius = CornerRadius(3f, 3f)
    )

    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(doorLeft + panelMargin, doorTop + doorH * 0.50f),
        size = Size(panelW, panelH2),
        cornerRadius = CornerRadius(3f, 3f)
    )

    drawCircle(
        color = Color(0xFFFFD700),
        radius = 5f,
        center = Offset(doorLeft + doorW * 0.84f, doorTop + doorH * 0.52f)
    )
}

private fun DrawScope.drawPoster(
    posterId: String?,
    posterLeft: Float,
    posterTop: Float,
    posterW: Float,
    posterH: Float
) {
    if (posterId == "sh_dec_poster") {
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(posterLeft - 3f, posterTop - 3f),
            size = Size(posterW + 6f, posterH + 6f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(posterLeft - 1.5f, posterTop - 1.5f),
            size = Size(posterW + 3f, posterH + 3f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF312E81), Color(0xFF9333EA), Color(0xFFF97316), Color(0xFFFDE047)),
                startY = posterTop,
                endY = posterTop + posterH
            ),
            topLeft = Offset(posterLeft, posterTop),
            size = Size(posterW, posterH),
            cornerRadius = CornerRadius(2f, 2f)
        )

        val sunCenter = Offset(posterLeft + posterW * 0.65f, posterTop + posterH * 0.35f)
        drawCircle(Color(0xFFFEF08A).copy(alpha = 0.5f), radius = posterW * 0.20f, center = sunCenter)
        drawCircle(Color(0xFFFDE047), radius = posterW * 0.12f, center = sunCenter)

        val mtn1 = Path().apply {
            moveTo(posterLeft, posterTop + posterH)
            lineTo(posterLeft, posterTop + posterH * 0.65f)
            lineTo(posterLeft + posterW * 0.35f, posterTop + posterH * 0.38f)
            lineTo(posterLeft + posterW * 0.70f, posterTop + posterH * 0.75f)
            lineTo(posterLeft + posterW, posterTop + posterH * 0.60f)
            lineTo(posterLeft + posterW, posterTop + posterH)
            close()
        }
        drawPath(mtn1, Color(0xFF4338CA))

        val glassPath = Path().apply {
            moveTo(posterLeft, posterTop)
            lineTo(posterLeft + posterW * 0.45f, posterTop)
            lineTo(posterLeft + posterW * 0.05f, posterTop + posterH * 0.60f)
            close()
        }
        drawPath(glassPath, Color.White.copy(alpha = 0.20f))
    } else {
        // Диплом инвестора
        drawRoundRect(
            color = Color(0xFF3E2723),
            topLeft = Offset(posterLeft - 3f, posterTop - 3f),
            size = Size(posterW + 6f, posterH + 6f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(posterLeft - 1.5f, posterTop - 1.5f),
            size = Size(posterW + 3f, posterH + 3f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                startY = posterTop,
                endY = posterTop + posterH
            ),
            topLeft = Offset(posterLeft, posterTop),
            size = Size(posterW, posterH),
            cornerRadius = CornerRadius(2f, 2f)
        )

        val lineInset = posterW * 0.14f
        val lineW = posterW - lineInset * 2f
        drawRect(Color(0xFFB45309), Offset(posterLeft + lineInset, posterTop + posterH * 0.20f), Size(lineW, 2.5f))
        drawRect(Color(0xFF94A3B8), Offset(posterLeft + lineInset, posterTop + posterH * 0.35f), Size(lineW * 0.85f, 2f))
        drawRect(Color(0xFF94A3B8), Offset(posterLeft + lineInset, posterTop + posterH * 0.48f), Size(lineW * 0.65f, 2f))

        val sealCenter = Offset(posterLeft + posterW * 0.5f, posterTop + posterH * 0.74f)
        drawCircle(Color(0xFFDC2626), 5f, sealCenter)
        drawCircle(Color(0xFFFFD700), 3.5f, sealCenter)
    }
}

private fun DrawScope.drawCozyFurniture(
    w: Float,
    leftWallX: Float,
    rightWallX: Float,
    topWallY: Float,
    floorBackY: Float,
    floorFrontY: Float,
    isNight: Boolean
) {
    // 1. Настенные круглые часы на левой стене
    val clockX = leftWallX * 0.52f
    val clockY = topWallY + (floorBackY - topWallY) * 0.20f
    val clockR = 14f

    drawCircle(Color(0x28000000), clockR, Offset(clockX + 2f, clockY + 2f))
    drawCircle(
        brush = Brush.linearGradient(
            listOf(Color(0xFFB45309), Color(0xFF78350F)),
            Offset(clockX - clockR, clockY - clockR),
            Offset(clockX + clockR, clockY + clockR)
        ),
        radius = clockR,
        center = Offset(clockX, clockY)
    )
    drawCircle(Color(0xFFFFFBEB), clockR - 2.5f, Offset(clockX, clockY))
    drawLine(Color(0xFF64748B), Offset(clockX, clockY - clockR + 4f), Offset(clockX, clockY - clockR + 6.5f), 1.5f)
    drawLine(Color(0xFF64748B), Offset(clockX, clockY + clockR - 4f), Offset(clockX, clockY + clockR - 6.5f), 1.5f)
    drawLine(Color(0xFF64748B), Offset(clockX - clockR + 4f, clockY), Offset(clockX - clockR + 6.5f, clockY), 1.5f)
    drawLine(Color(0xFF64748B), Offset(clockX + clockR - 4f, clockY), Offset(clockX + clockR - 6.5f, clockY), 1.5f)
    drawLine(Color(0xFF1E293B), Offset(clockX, clockY), Offset(clockX + 4.5f, clockY - 3.5f), 2f, cap = StrokeCap.Round)
    drawLine(Color(0xFF1E293B), Offset(clockX, clockY), Offset(clockX + 1f, clockY - 7f), 1.5f, cap = StrokeCap.Round)
    drawCircle(Color(0xFFEF4444), 1.6f, Offset(clockX, clockY))

    // 2. Настенная парящая деревянная полка с книгами и суккулентом
    val shelfLeft = leftWallX * 0.20f
    val shelfRight = leftWallX * 0.88f
    val shelfY = topWallY + (floorBackY - topWallY) * 0.44f

    drawLine(Color(0x35000000), Offset(shelfLeft, shelfY + 4f), Offset(shelfRight, shelfY + 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFF854D0E), Color(0xFFB45309), Color(0xFFD97706))),
        start = Offset(shelfLeft, shelfY),
        end = Offset(shelfRight, shelfY),
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )

    val bookX = shelfLeft + (shelfRight - shelfLeft) * 0.20f
    drawLine(Color(0xFF0D9488), Offset(bookX, shelfY - 1f), Offset(bookX, shelfY - 15f), 3.5f, cap = StrokeCap.Square)
    drawLine(Color(0xFFF59E0B), Offset(bookX + 4f, shelfY - 1f), Offset(bookX + 4f, shelfY - 13f), 3.5f, cap = StrokeCap.Square)
    drawLine(Color(0xFFF43F5E), Offset(bookX + 8f, shelfY - 1f), Offset(bookX + 8f, shelfY - 16f), 3.5f, cap = StrokeCap.Square)
    drawLine(Color(0xFF6366F1), Offset(bookX + 14f, shelfY - 1f), Offset(bookX + 11.5f, shelfY - 14f), 3.5f, cap = StrokeCap.Square)

    val potX = shelfLeft + (shelfRight - shelfLeft) * 0.75f
    drawRoundRect(Color(0xFFF8FAFC), Offset(potX - 5f, shelfY - 9f), Size(10f, 8f), CornerRadius(2f, 2f))
    drawCircle(Color(0xFF10B981), 4f, Offset(potX, shelfY - 11f))
    drawCircle(Color(0xFF34D399), 2.5f, Offset(potX - 2.5f, shelfY - 13f))
    drawCircle(Color(0xFF059669), 2.5f, Offset(potX + 2.5f, shelfY - 13f))

    // 3. Скандинавская деревянная тумбочка у левой стены (смещена правее для гармоничной композиции)
    val tableX = leftWallX * 0.78f
    val tableY = floorBackY + (floorFrontY - floorBackY) * 0.18f
    val tableW = (leftWallX * 0.75f).coerceIn(44f, 80f)
    val tableH = tableW * 0.88f
    val tableDepth = tableW * 0.38f

    drawOval(
        color = Color(0x35000000),
        topLeft = Offset(tableX - 3f, tableY + tableH - 3f),
        size = Size(tableW + 8f, tableDepth * 0.75f)
    )

    drawLine(Color(0xFF78350F), Offset(tableX + 4f, tableY + tableH * 0.65f), Offset(tableX + 3f, tableY + tableH), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color(0xFF78350F), Offset(tableX + tableW - 4f, tableY + tableH * 0.65f), Offset(tableX + tableW - 3f, tableY + tableH), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color(0xFF9A3412), Offset(tableX + 7f, tableY + tableH * 0.72f), Offset(tableX + 5f, tableY + tableH + 4f), strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(Color(0xFF9A3412), Offset(tableX + tableW - 7f, tableY + tableH * 0.72f), Offset(tableX + tableW - 5f, tableY + tableH + 4f), strokeWidth = 3.5f, cap = StrokeCap.Round)

    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFD4A373), Color(0xFFBC6C25))),
        topLeft = Offset(tableX, tableY + tableDepth * 0.35f),
        size = Size(tableW, tableH * 0.62f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    drawLine(
        color = Color(0xFF854D0E),
        start = Offset(tableX + 4f, tableY + tableDepth * 0.35f + tableH * 0.31f),
        end = Offset(tableX + tableW - 4f, tableY + tableDepth * 0.35f + tableH * 0.31f),
        strokeWidth = 1.5f
    )

    drawCircle(Color(0xFFFBBF24), radius = 2.8f, center = Offset(tableX + tableW * 0.5f, tableY + tableDepth * 0.35f + tableH * 0.15f))
    drawCircle(Color(0xFFFBBF24), radius = 2.8f, center = Offset(tableX + tableW * 0.5f, tableY + tableDepth * 0.35f + tableH * 0.46f))

    val topSurface = Path().apply {
        moveTo(tableX + 6f, tableY)
        lineTo(tableX + tableW + 6f, tableY)
        lineTo(tableX + tableW, tableY + tableDepth * 0.35f)
        lineTo(tableX, tableY + tableDepth * 0.35f)
        close()
    }
    drawPath(topSurface, color = Color(0xFFE2B686))
    drawPath(topSurface, color = Color(0xFFB45309), style = Stroke(width = 1f))

    // 4. Уютная настольная лампа с теплым свечением
    val lampX = tableX + tableW * 0.5f + 3f
    val lampBaseY = tableY + tableDepth * 0.18f

    drawOval(
        brush = Brush.radialGradient(
            listOf(Color(0xFF99F6E4), Color(0xFF0F766E)),
            center = Offset(lampX - 2f, lampBaseY - 8f),
            radius = 10f
        ),
        topLeft = Offset(lampX - 7f, lampBaseY - 14f),
        size = Size(14f, 14f)
    )

    drawLine(Color(0xFFD97706), Offset(lampX, lampBaseY - 14f), Offset(lampX, lampBaseY - 22f), strokeWidth = 2f)

    val shadeTop = lampBaseY - 38f
    val shadeBottom = lampBaseY - 22f
    val shadePath = Path().apply {
        moveTo(lampX - 8f, shadeTop)
        lineTo(lampX + 8f, shadeTop)
        lineTo(lampX + 13f, shadeBottom)
        lineTo(lampX - 13f, shadeBottom)
        close()
    }
    drawPath(shadePath, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))))
    drawPath(shadePath, color = Color(0xFFD97706), style = Stroke(width = 1f))

    val glowRadius = if (isNight) 72f else 46f
    val glowColor = if (isNight) Color(0x60F59E0B) else Color(0x30FDE047)
    drawCircle(
        brush = Brush.radialGradient(listOf(glowColor, Color.Transparent), center = Offset(lampX, shadeBottom - 4f), radius = glowRadius),
        radius = glowRadius,
        center = Offset(lampX, shadeBottom - 4f)
    )

    // 5. Комнатное растение Монстера в кашпо справа
    val plantX = rightWallX + (w - rightWallX) * 0.35f
    val plantY = floorBackY + (floorFrontY - floorBackY) * 0.38f

    drawOval(Color(0x35000000), Offset(plantX - 18f, plantY + 16f), Size(36f, 12f))

    val potPath = Path().apply {
        moveTo(plantX - 15f, plantY - 8f)
        lineTo(plantX + 15f, plantY - 8f)
        lineTo(plantX + 11f, plantY + 18f)
        lineTo(plantX - 11f, plantY + 18f)
        close()
    }
    drawPath(potPath, Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C))))
    drawRoundRect(Color(0xFFDD6B20), Offset(plantX - 17f, plantY - 12f), Size(34f, 7f), CornerRadius(2.5f, 2.5f))
    drawOval(Color(0xFF291A10), Offset(plantX - 14f, plantY - 11f), Size(28f, 5f))

    drawLine(Color(0xFF15803D), Offset(plantX, plantY - 9f), Offset(plantX - 16f, plantY - 26f), strokeWidth = 2f, cap = StrokeCap.Round)
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFF4ADE80), Color(0xFF166534)), Offset(plantX - 16f, plantY - 26f), 18f),
        topLeft = Offset(plantX - 26f, plantY - 36f),
        size = Size(20f, 15f)
    )

    drawLine(Color(0xFF15803D), Offset(plantX, plantY - 9f), Offset(plantX + 1f, plantY - 30f), strokeWidth = 2f, cap = StrokeCap.Round)
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFF86EFAC), Color(0xFF15803D)), Offset(plantX + 2f, plantY - 32f), 18f),
        topLeft = Offset(plantX - 8f, plantY - 44f),
        size = Size(18f, 22f)
    )

    drawLine(Color(0xFF15803D), Offset(plantX, plantY - 9f), Offset(plantX + 16f, plantY - 24f), strokeWidth = 2f, cap = StrokeCap.Round)
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFF4ADE80), Color(0xFF14532D)), Offset(plantX + 18f, plantY - 24f), 18f),
        topLeft = Offset(plantX + 8f, plantY - 34f),
        size = Size(20f, 15f)
    )

    drawLine(Color(0xFF15803D), Offset(plantX, plantY - 9f), Offset(plantX + 14f, plantY - 12f), strokeWidth = 1.8f, cap = StrokeCap.Round)
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFF22C55E), Color(0xFF14532D)), Offset(plantX + 16f, plantY - 12f), 14f),
        topLeft = Offset(plantX + 10f, plantY - 18f),
        size = Size(16f, 11f)
    )
}

private fun DrawScope.drawPetFoodCorner(
    w: Float,
    h: Float,
    hunger: Float
) {
    val bowlCenterX = w * 0.18f
    val bowlCenterY = h * 0.78f
    val bowlW = w * 0.13f
    val bowlH = bowlW * 0.50f

    // Тень под миской
    drawOval(
        color = Color(0x30000000),
        topLeft = Offset(bowlCenterX - bowlW * 0.55f, bowlCenterY - bowlH * 0.35f),
        size = Size(bowlW * 1.1f, bowlH * 1.05f)
    )

    // Корпус керамической миски
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
            bowlCenterY - bowlH * 0.5f,
            bowlCenterY + bowlH * 0.5f
        ),
        topLeft = Offset(bowlCenterX - bowlW * 0.5f, bowlCenterY - bowlH * 0.5f),
        size = Size(bowlW, bowlH)
    )

    // Внутренняя выемка миски
    val innerW = bowlW * 0.85f
    val innerH = bowlH * 0.72f
    drawOval(
        color = Color(0xFF0369A1),
        topLeft = Offset(bowlCenterX - innerW * 0.5f, bowlCenterY - innerH * 0.55f),
        size = Size(innerW, innerH)
    )

    // Еда / гранулы в миске (зависит от сытости)
    if (hunger > 0.15f) {
        val foodW = innerW * 0.75f
        val foodH = innerH * 0.70f
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color(0xFFD97706), Color(0xFF92400E)),
                Offset(bowlCenterX, bowlCenterY - innerH * 0.2f),
                foodW * 0.5f
            ),
            topLeft = Offset(bowlCenterX - foodW * 0.5f, bowlCenterY - innerH * 0.45f),
            size = Size(foodW, foodH)
        )
    }
}

private fun DrawScope.drawWallAwardsBoard(
    state: GameState,
    boardLeft: Float,
    boardTop: Float,
    boardW: Float,
    boardH: Float
) {
    // 1. Мягкая тень от настенного планшета
    drawRoundRect(
        color = Color(0x35000000),
        topLeft = Offset(boardLeft + 4f, boardTop + 5f),
        size = Size(boardW, boardH),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // 2. Деревянная рамка доски (богатый теплый орех / дуб)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF854D0E), Color(0xFF713F12), Color(0xFF5A2F08)),
            start = Offset(boardLeft, boardTop),
            end = Offset(boardLeft + boardW, boardTop + boardH)
        ),
        topLeft = Offset(boardLeft, boardTop),
        size = Size(boardW, boardH),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Внутренняя фаска деревянной рамки
    drawRoundRect(
        color = Color(0x30FFFFFF),
        topLeft = Offset(boardLeft + 1.5f, boardTop + 1.5f),
        size = Size(boardW - 3f, boardH - 3f),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = 1.2f)
    )

    // 3. Пробковое / войлочное поле доски
    val frameBorder = 5.5f
    val innerLeft = boardLeft + frameBorder
    val innerTop = boardTop + frameBorder
    val innerW = boardW - frameBorder * 2
    val innerH = boardH - frameBorder * 2

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE8C8A2), Color(0xFFDCB78D), Color(0xFFCF9F6A)),
            startY = innerTop,
            endY = innerTop + innerH
        ),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerW, innerH),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Текстурные крапинки пробки
    val corkSeed = 7
    for (i in 0..12) {
        val px = innerLeft + ((i * 37 + corkSeed) % 100) / 100f * innerW
        val py = innerTop + ((i * 53 + corkSeed) % 100) / 100f * innerH
        drawCircle(
            color = Color(0x1878350F),
            radius = 1.2f,
            center = Offset(px, py)
        )
    }

    // 4. Латунная табличка-шильдик сверху: "НАГРАДЫ"
    val plaqueW = innerW * 0.72f
    val plaqueH = innerH * 0.11f
    val plaqueLeft = innerLeft + (innerW - plaqueW) / 2f
    val plaqueTop = innerTop + 3.5f

    // Тень таблички
    drawRoundRect(
        color = Color(0x30000000),
        topLeft = Offset(plaqueLeft + 1f, plaqueTop + 1.5f),
        size = Size(plaqueW, plaqueH),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Золотая латунь
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFEF08A)),
            startX = plaqueLeft,
            endX = plaqueLeft + plaqueW
        ),
        topLeft = Offset(plaqueLeft, plaqueTop),
        size = Size(plaqueW, plaqueH),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Болтики по бокам таблички
    drawCircle(Color(0xFF78350F), radius = 1.2f, center = Offset(plaqueLeft + 3f, plaqueTop + plaqueH / 2f))
    drawCircle(Color(0xFF78350F), radius = 1.2f, center = Offset(plaqueLeft + plaqueW - 3f, plaqueTop + plaqueH / 2f))

    // Звезда / медалька по центру таблички
    val starCenter = Offset(plaqueLeft + plaqueW / 2f, plaqueTop + plaqueH / 2f)
    drawCircle(Color(0xFF92400E), radius = 2.4f, center = starCenter)
    drawCircle(Color(0xFFFEF9C3), radius = 1.2f, center = starCenter)

    // 5. Сетка наградных бумажек (дипломов / сертификатов / грамот) - 6 шт (2 колонки x 3 ряда)
    val gridTop = plaqueTop + plaqueH + 5f
    val gridBottom = innerTop + innerH - 4f
    val gridH = gridBottom - gridTop
    val gridW = innerW - 6f
    val gridLeft = innerLeft + 3f

    val colGap = 4f
    val rowGap = 4.5f
    val cellW = (gridW - colGap) / 2f
    val cellH = (gridH - rowGap * 2) / 3f

    val awards = WallAwardsRepository.allAwards.take(6)

    awards.forEachIndexed { index, award ->
        val row = index / 2
        val col = index % 2
        val sheetLeft = gridLeft + col * (cellW + colGap)
        val sheetTop = gridTop + row * (cellH + rowGap)
        val isUnlocked = award.isUnlocked(state)

        // Небольшой живой наклон каждого листа бумаги для реалистичности
        val tiltAngle = when (index) {
            0 -> -1.5f
            1 -> 1.8f
            2 -> 1.2f
            3 -> -1.4f
            4 -> -1.0f
            else -> 1.5f
        }

        rotate(tiltAngle, pivot = Offset(sheetLeft + cellW / 2f, sheetTop + 4f)) {
            if (isUnlocked) {
                // Тень от листа
                drawRoundRect(
                    color = Color(0x28000000),
                    topLeft = Offset(sheetLeft + 1.5f, sheetTop + 2f),
                    size = Size(cellW, cellH),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Пергаментная бумага грамоты (слоновая кость / благородный бежевый)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFDF5), Color(0xFFFFF9E6)),
                        startY = sheetTop,
                        endY = sheetTop + cellH
                    ),
                    topLeft = Offset(sheetLeft, sheetTop),
                    size = Size(cellW, cellH),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Цветная наградная рамка диплома
                val ribbonColor = Color(award.ribbonColorHex)
                drawRoundRect(
                    color = ribbonColor.copy(alpha = 0.55f),
                    topLeft = Offset(sheetLeft + 2f, sheetTop + 2f),
                    size = Size(cellW - 4f, cellH - 4f),
                    cornerRadius = CornerRadius(2f, 2f),
                    style = Stroke(width = 0.9f)
                )

                // Заголовочная полоска диплома
                drawRect(
                    color = ribbonColor.copy(alpha = 0.75f),
                    topLeft = Offset(sheetLeft + 3.5f, sheetTop + 5.5f),
                    size = Size(cellW - 7f, 1.8f)
                )

                // Текстовые каллиграфические строки на дипломе
                val lineCol = Color(0xFF94A3B8)
                drawLine(
                    color = lineCol,
                    start = Offset(sheetLeft + 3.5f, sheetTop + 9.5f),
                    end = Offset(sheetLeft + cellW - 4.5f, sheetTop + 9.5f),
                    strokeWidth = 0.9f
                )
                drawLine(
                    color = lineCol,
                    start = Offset(sheetLeft + 3.5f, sheetTop + 12.5f),
                    end = Offset(sheetLeft + cellW - 6.5f, sheetTop + 12.5f),
                    strokeWidth = 0.9f
                )
                if (cellH > 18f) {
                    drawLine(
                        color = lineCol,
                        start = Offset(sheetLeft + 3.5f, sheetTop + 15.5f),
                        end = Offset(sheetLeft + cellW - 5.5f, sheetTop + 15.5f),
                        strokeWidth = 0.9f
                    )
                }

                // Сургучная печать с наградной ленточкой в нижнем углу
                val sealCenter = Offset(sheetLeft + cellW - 5.5f, sheetTop + cellH - 5.5f)
                val sealColor = Color(award.sealColorHex)

                // Кончики ленточек печати
                val ribbonPath = Path().apply {
                    moveTo(sealCenter.x - 2.5f, sealCenter.y + 1f)
                    lineTo(sealCenter.x - 4.5f, sealCenter.y + 5f)
                    lineTo(sealCenter.x - 1.5f, sealCenter.y + 4f)
                    close()
                    moveTo(sealCenter.x + 1f, sealCenter.y + 1f)
                    lineTo(sealCenter.x + 3.5f, sealCenter.y + 5.5f)
                    lineTo(sealCenter.x + 1f, sealCenter.y + 4.2f)
                    close()
                }
                drawPath(ribbonPath, color = sealColor)

                // Круг сургучной печати
                drawCircle(color = sealColor, radius = 3.2f, center = sealCenter)
                drawCircle(color = Color(0xFFFFD700), radius = 1.3f, center = sealCenter)

                // Металлическая кнопка-гвоздик (pushpin) сверху по центру
                val pinCenter = Offset(sheetLeft + cellW / 2f, sheetTop + 2.8f)
                drawCircle(color = Color(0x35000000), radius = 2.2f, center = Offset(pinCenter.x + 0.6f, pinCenter.y + 0.8f))
                drawCircle(color = Color(0xFFDC2626), radius = 2.2f, center = pinCenter)
                drawCircle(color = Color.White, radius = 0.8f, center = Offset(pinCenter.x - 0.5f, pinCenter.y - 0.5f))

            } else {
                // Пустое место для ещё не полученной награды (контурная заготовка)
                drawRoundRect(
                    color = Color(0x18000000),
                    topLeft = Offset(sheetLeft, sheetTop),
                    size = Size(cellW, cellH),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Пунктирный/полупрозрачный контур
                drawRoundRect(
                    color = Color(0x3578350F),
                    topLeft = Offset(sheetLeft, sheetTop),
                    size = Size(cellW, cellH),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = Stroke(width = 0.8f)
                )

                // Замочек по центру
                val lockX = sheetLeft + cellW / 2f
                val lockY = sheetTop + cellH / 2f + 1f
                // Дужка замка
                drawCircle(
                    color = Color(0x4078350F),
                    radius = 2.2f,
                    center = Offset(lockX, lockY - 2.2f),
                    style = Stroke(width = 0.9f)
                )
                // Тело замка
                drawRoundRect(
                    color = Color(0x5078350F),
                    topLeft = Offset(lockX - 3f, lockY - 1.2f),
                    size = Size(6f, 4.8f),
                    cornerRadius = CornerRadius(1.2f, 1.2f)
                )

                // Канцелярская кнопка ожидания сверху
                val pinCenter = Offset(sheetLeft + cellW / 2f, sheetTop + 2.8f)
                drawCircle(color = Color(0x6094A3B8), radius = 1.8f, center = pinCenter)
            }
        }
    }
}
