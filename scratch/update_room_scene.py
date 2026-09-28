import os

code = '''package ru.finpet.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.data.GameState
import java.util.Calendar

/**
 * 2.5D псевдо-изометрическая комната питомца:
 * - Стены в перспективе: левая боковая стена, центральная стена, правая боковая стена
 * - Обои из магазина с адаптацией под перспективу
 * - Окно со временем суток и динамическим лучом дневного света на пол
 * - Дверь с наличниками и золотой ручкой
 * - Плинтуса по контуру стен
 * - Изометрический пол с перспективными досками и ковриком
 * - Мебель: шкаф на левой стене, стол и лампа на правой стене, кровать на полу
 * - Атмосферное освещение и ночной свет лампы
 */
@Composable
fun PetRoomSceneView(
    state: GameState,
    modifier: Modifier = Modifier,
    cornerRadius: Float = 24f,
    showDayNightBadge: Boolean = true,
    petContent: @Composable BoxScope.() -> Unit = {}
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isNight = currentHour in 21..23 || currentHour in 0..6
    val isSunset = currentHour in 18..20

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Геометрия 2.5D перспективы комнаты
            val leftWallX = w * 0.14f
            val rightWallX = w * 0.86f
            val topWallY = h * 0.04f
            val floorBackY = h * 0.62f
            val floorFrontY = h * 0.68f

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

            // 4. Окно на центральной стене (слева от двери)
            val winW = (rightWallX - leftWallX) * 0.26f
            val winH = (floorBackY - topWallY) * 0.44f
            val winLeft = leftWallX + (rightWallX - leftWallX) * 0.08f
            val winTop = topWallY + (floorBackY - topWallY) * 0.14f

            drawWindow(
                isNight = isNight,
                isSunset = isSunset,
                winLeft = winLeft,
                winTop = winTop,
                winW = winW,
                winH = winH
            )

            // 5. Дверь по центру задней стены
            val doorW = (rightWallX - leftWallX) * 0.28f
            val doorH = (floorBackY - topWallY) * 0.78f
            val doorLeft = leftWallX + (rightWallX - leftWallX - doorW) / 2f
            val doorTop = floorBackY - doorH

            drawDoor(
                doorLeft = doorLeft,
                doorTop = doorTop,
                doorW = doorW,
                doorH = doorH
            )

            // 6. Постер / Настенный декор (справа от двери)
            if (state.equippedPoster != null) {
                val posterLeft = doorLeft + doorW + (rightWallX - leftWallX) * 0.06f
                val posterTop = doorTop + doorH * 0.14f
                val posterW = (rightWallX - leftWallX) * 0.22f
                val posterH = doorH * 0.38f

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
                h = h,
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
                    floorBackY = floorBackY,
                    floorFrontY = floorFrontY,
                    isSunset = isSunset,
                    h = h
                )
            }

            // 10. Мебель: Шкаф (на левой стене)
            if (state.equippedWardrobe != null) {
                drawWardrobe(
                    leftWallX = leftWallX,
                    floorBackY = floorBackY,
                    floorFrontY = floorFrontY,
                    topWallY = topWallY
                )
            }

            // 11. Мебель: Стол (на правой стене)
            if (state.equippedDesk != null) {
                drawDesk(
                    deskId = state.equippedDesk,
                    rightWallX = rightWallX,
                    floorBackY = floorBackY,
                    floorFrontY = floorFrontY,
                    w = w
                )
            }

            // 12. Мебель: Лампа / Торшер
            if (state.equippedLamp != null) {
                drawLamp(
                    rightWallX = rightWallX,
                    floorBackY = floorBackY,
                    floorFrontY = floorFrontY,
                    isNight = isNight
                )
            }

            // 13. Мебель: Кровать (на полу слева/снизу)
            if (state.equippedBed != null) {
                drawBed(
                    leftWallX = leftWallX,
                    floorBackY = floorBackY,
                    floorFrontY = floorFrontY,
                    h = h
                )
            }

            // 14. Ночное/вечернее освещение и световой конус лампы
            drawAtmosphericLighting(
                w = w,
                h = h,
                isNight = isNight,
                isSunset = isSunset,
                hasLamp = state.equippedLamp != null,
                rightWallX = rightWallX,
                floorBackY = floorBackY
            )
        }

        // Индикатор времени суток
        if (showDayNightBadge) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xDD1E293B),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = when {
                        isNight -> "🌙 Ночь ($currentHour:00)"
                        isSunset -> "🌅 Закат ($currentHour:00)"
                        else -> "☀️ День ($currentHour:00)"
                    },
                    fontSize = 10.5.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Контент питомца в комнате (располагается поверх пола)
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

    // Текстурные детали обоев на центральной стене
    when (state.equippedWallpaper) {
        "sh_wp_brick" -> {
            val rowH = 16f
            var y = topY
            while (y < bottomY) {
                drawLine(Color(0x33000000), Offset(leftX, y), Offset(rightX, y), strokeWidth = 1.5f)
                y += rowH
            }
        }
        "sh_wp_space" -> {
            drawCircle(Color.White.copy(alpha = 0.85f), 2.2f, Offset(leftX + wallW * 0.15f, topY + wallH * 0.25f))
            drawCircle(Color.White.copy(alpha = 0.9f), 2.8f, Offset(leftX + wallW * 0.85f, topY + wallH * 0.18f))
            drawCircle(Color.White.copy(alpha = 0.65f), 1.8f, Offset(leftX + wallW * 0.40f, topY + wallH * 0.15f))
            drawCircle(Color.White.copy(alpha = 0.75f), 2.2f, Offset(leftX + wallW * 0.70f, topY + wallH * 0.35f))
        }
        "sh_wp_cyber" -> {
            var gx = leftX
            while (gx < rightX) {
                drawLine(Color(0x2500D2FF), Offset(gx, topY), Offset(gx, bottomY), 1.5f)
                gx += 24f
            }
        }
        "sh_wp_sakura" -> {
            drawCircle(Color(0xFFFFB6C1), 3.5f, Offset(leftX + wallW * 0.18f, topY + wallH * 0.3f))
            drawCircle(Color(0xFFFFC0CB), 2.8f, Offset(leftX + wallW * 0.80f, topY + wallH * 0.22f))
            drawCircle(Color(0xFFFFB6C1), 3.2f, Offset(leftX + wallW * 0.42f, topY + wallH * 0.5f))
        }
        "sh_wp_forest" -> {
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.85f), 2.5f, Offset(leftX + wallW * 0.2f, topY + wallH * 0.35f))
            drawCircle(Color(0xFFFDE047).copy(alpha = 0.75f), 2f, Offset(leftX + wallW * 0.82f, topY + wallH * 0.45f))
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

    // Затенение левой стены (ambient shadow под углом)
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

    // Затенение правой стены
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
    h: Float,
    leftWallX: Float,
    rightWallX: Float,
    topWallY: Float,
    floorBackY: Float,
    floorFrontY: Float
) {
    // Внутренние вертикальные углы (стыки стен)
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

    // Плинтуса (skirting boards)
    // 1. Центральный плинтус
    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(leftWallX, floorBackY - 5f),
        size = Size(rightWallX - leftWallX, 5f)
    )

    // 2. Левый наклонный плинтус
    val leftSkirting = Path().apply {
        moveTo(0f, floorFrontY - 5f)
        lineTo(leftWallX, floorBackY - 5f)
        lineTo(leftWallX, floorBackY)
        lineTo(0f, floorFrontY)
        close()
    }
    drawPath(leftSkirting, Color(0xFF1E293B))

    // 3. Правый наклонный плинтус
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
    // Многоугольник пола
    val floorPath = Path().apply {
        moveTo(0f, floorFrontY)
        lineTo(leftWallX, floorBackY)
        lineTo(rightWallX, floorBackY)
        lineTo(w, floorFrontY)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    val floorColors = when (state.equippedFloor) {
        "sh_fl_dark" -> listOf(Color(0xFF3E2723), Color(0xFF24140E))
        "sh_fl_tatami" -> listOf(Color(0xFFC4B889), Color(0xFF9E8F5E))
        "sh_fl_marble" -> listOf(Color(0xFFF1F5F9), Color(0xFFB4C2D3))
        "sh_fl_neon" -> listOf(Color(0xFF0F172A), Color(0xFF151D3B))
        "sh_fl_carpet" -> listOf(Color(0xFFF472B6), Color(0xFFBE185D))
        else -> listOf(Color(0xFFD4A373), Color(0xFFA07248)) // Натуральное дерево
    }

    drawPath(
        path = floorPath,
        brush = Brush.verticalGradient(
            colors = floorColors,
            startY = floorBackY,
            endY = h
        )
    )

    // Перспективные доски пола, сходящиеся к центру
    val plankCount = 8
    val backW = rightWallX - leftWallX
    for (i in 0..plankCount) {
        val topX = leftWallX + (backW / plankCount) * i
        val bottomX = (w / plankCount) * i
        drawLine(
            color = Color(0x24000000),
            start = Offset(topX, floorBackY),
            end = Offset(bottomX, h),
            strokeWidth = 1.6f
        )
    }

    // Изометрический коврик по центру под питомцем
    val rugCenter = Offset(w * 0.5f, floorBackY + (h - floorBackY) * 0.52f)
    val rugW = w * 0.46f
    val rugH = (h - floorBackY) * 0.44f

    val rugColor = when (state.equippedFloor) {
        "sh_fl_carpet" -> Color(0x44FFFFFF)
        "sh_fl_neon" -> Color(0x3300D2FF)
        else -> Color(0x35000000)
    }

    // Тень под ковриком
    drawOval(
        color = Color(0x18000000),
        topLeft = Offset(rugCenter.x - rugW * 0.52f, rugCenter.y - rugH * 0.48f),
        size = Size(rugW * 1.04f, rugH * 1.04f)
    )

    // Сам коврик с мягким контуром
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                if (state.equippedFloor == "sh_fl_neon") Color(0x4400E5FF) else Color(0xFFF8FAFC).copy(alpha = 0.45f),
                rugColor
            ),
            center = rugCenter,
            radius = rugW * 0.5f
        ),
        topLeft = Offset(rugCenter.x - rugW * 0.5f, rugCenter.y - rugH * 0.5f),
        size = Size(rugW, rugH)
    )
}

private fun DrawScope.drawWindow(
    isNight: Boolean,
    isSunset: Boolean,
    winLeft: Float,
    winTop: Float,
    winW: Float,
    winH: Float
) {
    val skyColors = when {
        isNight -> listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF2E1065))
        isSunset -> listOf(Color(0xFFEA580C), Color(0xFFDB2777), Color(0xFF6366F1))
        else -> listOf(Color(0xFF38BDF8), Color(0xFF7DD3FC), Color(0xFFBAE6FD))
    }

    // Внешняя рама окна
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(winLeft - 3f, winTop - 3f),
        size = Size(winW + 6f, winH + 6f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Небо в окне
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

    // Солнце / Луна
    if (isNight) {
        // Полумесяц
        drawCircle(Color(0xFFFEF08A), 7f, Offset(winLeft + winW * 0.72f, winTop + winH * 0.32f))
        drawCircle(Color(0xFF1E1B4B), 5.5f, Offset(winLeft + winW * 0.69f, winTop + winH * 0.30f))
        // Звезды
        drawCircle(Color.White.copy(alpha = 0.9f), 1.5f, Offset(winLeft + winW * 0.25f, winTop + winH * 0.28f))
        drawCircle(Color.White.copy(alpha = 0.8f), 1.2f, Offset(winLeft + winW * 0.45f, winTop + winH * 0.48f))
    } else if (isSunset) {
        drawCircle(Color(0xFFFDE047), 9f, Offset(winLeft + winW * 0.5f, winTop + winH * 0.68f))
    } else {
        // Солнце
        drawCircle(Color(0xFFFACC15), 8.5f, Offset(winLeft + winW * 0.75f, winTop + winH * 0.28f))
        // Пушистое облако
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(winLeft + winW * 0.16f, winTop + winH * 0.46f),
            size = Size(winW * 0.46f, 6.5f),
            cornerRadius = CornerRadius(3.5f, 3.5f)
        )
    }

    // Переплет рамы (крестовина)
    drawLine(Color(0xFF334155), Offset(winLeft, winTop + winH * 0.5f), Offset(winLeft + winW, winTop + winH * 0.5f), 1.6f)
    drawLine(Color(0xFF334155), Offset(winLeft + winW * 0.5f, winTop), Offset(winLeft + winW * 0.5f, winTop + winH), 1.6f)

    // Подоконник
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
    floorBackY: Float,
    floorFrontY: Float,
    isSunset: Boolean,
    h: Float
) {
    // 2.5D световой луч, падающий из окна по диагонали через пол
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
    // Внешняя наличная коробка
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

    // Полотно двери
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

    // Внутренние филенки двери
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

    // Золотая круглая ручка
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
    // Рамка постера
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(posterLeft - 2f, posterTop - 2f),
        size = Size(posterW + 4f, posterH + 4f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    val posterBrush = when (posterId) {
        "sh_furn_shelf" -> Brush.verticalGradient(listOf(Color(0xFF8D493A), Color(0xFF5D4037)))
        else -> Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6)))
    }

    drawRoundRect(
        brush = posterBrush,
        topLeft = Offset(posterLeft, posterTop),
        size = Size(posterW, posterH),
        cornerRadius = CornerRadius(3f, 3f)
    )

    if (posterId == "sh_furn_shelf") {
        // Полка с книгами
        drawLine(Color(0xFF3E2723), Offset(posterLeft, posterTop + posterH * 0.7f), Offset(posterLeft + posterW, posterTop + posterH * 0.7f), 3f)
        // Книжки
        drawRect(Color(0xFFEF4444), Offset(posterLeft + 3f, posterTop + posterH * 0.35f), Size(4f, posterH * 0.35f))
        drawRect(Color(0xFF3B82F6), Offset(posterLeft + 9f, posterTop + posterH * 0.30f), Size(5f, posterH * 0.40f))
        drawRect(Color(0xFF10B981), Offset(posterLeft + 16f, posterTop + posterH * 0.38f), Size(4f, posterH * 0.32f))
    }
}

private fun DrawScope.drawWardrobe(
    leftWallX: Float,
    floorBackY: Float,
    floorFrontY: Float,
    topWallY: Float
) {
    // Шкаф в изометрической перспективе у левой стены
    val wW = leftWallX * 1.05f
    val wH = (floorBackY - topWallY) * 0.72f
    val wLeft = leftWallX * 0.12f
    val wTop = floorBackY - wH + 10f

    // Корпус
    drawRoundRect(
        color = Color(0xFF4E342E),
        topLeft = Offset(wLeft, wTop),
        size = Size(wW, wH),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // Фасадные дверцы
    drawRoundRect(
        color = Color(0xFF5D4037),
        topLeft = Offset(wLeft + 2f, wTop + 2f),
        size = Size(wW - 4f, wH - 4f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Разделительная линия створок
    drawLine(
        color = Color(0xFF3E2723),
        start = Offset(wLeft + wW * 0.5f, wTop + 3f),
        end = Offset(wLeft + wW * 0.5f, wTop + wH - 3f),
        strokeWidth = 2f
    )

    // Ручки
    drawCircle(Color(0xFFFFD700), 2.5f, Offset(wLeft + wW * 0.42f, wTop + wH * 0.5f))
    drawCircle(Color(0xFFFFD700), 2.5f, Offset(wLeft + wW * 0.58f, wTop + wH * 0.5f))
}

private fun DrawScope.drawDesk(
    deskId: String?,
    rightWallX: Float,
    floorBackY: Float,
    floorFrontY: Float,
    w: Float
) {
    val deskW = (w - rightWallX) * 1.55f
    val deskH = (floorFrontY - floorBackY) * 0.95f
    val deskLeft = rightWallX - deskW * 0.35f
    val deskTop = floorBackY + 2f

    if (deskId == "sh_furn_tea_table") {
        // Уютный чайный столик (деревянный круглый)
        drawOval(
            color = Color(0xFF5D4037),
            topLeft = Offset(deskLeft, deskTop),
            size = Size(deskW, deskH * 0.55f)
        )
        drawOval(
            color = Color(0xFF8D493A),
            topLeft = Offset(deskLeft + 2f, deskTop + 2f),
            size = Size(deskW - 4f, deskH * 0.55f - 4f)
        )
        // Ножки
        drawLine(Color(0xFF4E342E), Offset(deskLeft + deskW * 0.25f, deskTop + deskH * 0.45f), Offset(deskLeft + deskW * 0.22f, deskTop + deskH), 3f)
        drawLine(Color(0xFF4E342E), Offset(deskLeft + deskW * 0.75f, deskTop + deskH * 0.45f), Offset(deskLeft + deskW * 0.78f, deskTop + deskH), 3f)
    } else if (deskId == "sh_furn_cyber_desk") {
        // Кибер-стол с неоновой подсветкой
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(deskLeft, deskTop),
            size = Size(deskW, 10f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Неоновая кромка
        drawLine(Color(0xFF00D2FF), Offset(deskLeft, deskTop + 10f), Offset(deskLeft + deskW, deskTop + 10f), 2.5f)
        // Ножки
        drawLine(Color(0xFF1E293B), Offset(deskLeft + 4f, deskTop + 10f), Offset(deskLeft + 4f, deskTop + deskH), 4f)
        drawLine(Color(0xFF1E293B), Offset(deskLeft + deskW - 4f, deskTop + 10f), Offset(deskLeft + deskW - 4f, deskTop + deskH), 4f)
    } else {
        // Классический рабочий стол
        drawRoundRect(
            color = Color(0xFF6D4C41),
            topLeft = Offset(deskLeft, deskTop),
            size = Size(deskW, 11f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Ножки
        drawRect(Color(0xFF4E342E), Offset(deskLeft + 4f, deskTop + 11f), Size(6f, deskH))
        drawRect(Color(0xFF4E342E), Offset(deskLeft + deskW - 10f, deskTop + 11f), Size(6f, deskH))
    }
}

private fun DrawScope.drawLamp(
    rightWallX: Float,
    floorBackY: Float,
    floorFrontY: Float,
    isNight: Boolean
) {
    val lampX = rightWallX + 8f
    val lampBaseY = floorBackY + 12f
    val lampHeight = 65f
    val lampTopY = lampBaseY - lampHeight

    // Стойка торшера
    drawLine(Color(0xFF334155), Offset(lampX, lampBaseY), Offset(lampX, lampTopY), 2.5f)
    // Основание
    drawOval(Color(0xFF1E293B), topLeft = Offset(lampX - 7f, lampBaseY - 3f), size = Size(14f, 6f))

    // Абажур
    val shadePath = Path().apply {
        moveTo(lampX - 6f, lampTopY)
        lineTo(lampX + 6f, lampTopY)
        lineTo(lampX + 11f, lampTopY + 14f)
        lineTo(lampX - 11f, lampTopY + 14f)
        close()
    }
    drawPath(shadePath, if (isNight) Color(0xFFFDE047) else Color(0xFFE2E8F0))

    // Свечение абажура
    if (isNight) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x88FDE047), Color(0x00FDE047)),
                center = Offset(lampX, lampTopY + 10f),
                radius = 45f
            ),
            radius = 45f,
            center = Offset(lampX, lampTopY + 10f)
        )
    }
}

private fun DrawScope.drawBed(
    leftWallX: Float,
    floorBackY: Float,
    floorFrontY: Float,
    h: Float
) {
    val bedW = leftWallX * 1.55f
    val bedH = (h - floorBackY) * 0.42f
    val bedLeft = leftWallX * 0.2f
    val bedTop = floorBackY + (h - floorBackY) * 0.20f

    // Каркас кровати
    drawRoundRect(
        color = Color(0xFFCBD5E1),
        topLeft = Offset(bedLeft, bedTop),
        size = Size(bedW, bedH),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Мягкое одеяло
    drawRoundRect(
        color = Color(0xFFF472B6),
        topLeft = Offset(bedLeft + 5f, bedTop + 4f),
        size = Size(bedW * 0.68f, bedH - 8f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Подушка
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(bedLeft + bedW * 0.72f, bedTop + 6f),
        size = Size(bedW * 0.22f, bedH - 12f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

private fun DrawScope.drawAtmosphericLighting(
    w: Float,
    h: Float,
    isNight: Boolean,
    isSunset: Boolean,
    hasLamp: Boolean,
    rightWallX: Float,
    floorBackY: Float
) {
    when {
        isNight -> {
            // Ночной уютный полумрак
            drawRect(color = Color(0x320B132B), topLeft = Offset.Zero, size = Size(w, h))

            // Теплый световой круг от ночника / торшера
            val lampCenterX = rightWallX + 8f
            val lampCenterY = floorBackY - 30f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x60FDE047), Color(0x20FDE047), Color.Transparent),
                    center = Offset(lampCenterX, lampCenterY),
                    radius = 95f
                ),
                radius = 95f,
                center = Offset(lampCenterX, lampCenterY)
            )
        }
        isSunset -> {
            // Теплый закатный золотистый фильтр
            drawRect(color = Color(0x18EA580C), topLeft = Offset.Zero, size = Size(w, h))
        }
        else -> {
            // Дневной чистый свет
            drawRect(color = Color(0x06FACC15), topLeft = Offset.Zero, size = Size(w, h))
        }
    }
}
'''

with open(r'C:\Users\festik\.gemini\antigravity\scratch\finpetrustore\app\src\main\java\ru\finpet\app\ui\components\PetRoomSceneView.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("PetRoomSceneView.kt updated successfully!")
