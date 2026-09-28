package ru.finpet.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.ui.theme.LocalAppTheme
import ru.finpet.app.ui.theme.UnboundedFamily
import ru.finpet.app.ui.theme.primaryColor

/**
 * Приятная мультяшная 3D кнопка в стиле лучших анимационных мобильных игр (Brawl Stars, Duolingo, Bunny Store):
 * - Объемный нижний 3D скос/тень (chunky cartoon bevel)
 * - Пружинное продавливание вниз при нажатии (squash & push)
 * - Четкий белый шрифт Unbounded
 * - Интегрированная тактильная отдача
 */
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CartoonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalAppTheme.current.primaryColor,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    cornerRadius: Dp = 14.dp,
    fontSize: TextUnit = 13.5.sp,
    depth: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) depth else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cartoonButtonOffset"
    )

    // Автоматическая адаптация размера шрифта под длину текста и размер кнопки
    val effectiveFontSize = remember(text, fontSize, height, icon) {
        val baseSp = fontSize.value
        val iconDeduction = if (icon != null) 1.5f else 0f
        val lengthDeduction = when {
            text.length > 20 -> 3.5f
            text.length > 14 -> 2.5f
            text.length > 10 -> 1.5f
            else -> 0f
        }
        val heightDeduction = if (height <= 42.dp) 1.5f else 0f
        val calculated = baseSp - iconDeduction - lengthDeduction - heightDeduction
        calculated.coerceAtLeast(8.5f).sp
    }

    // Адаптивные отступы для компактных кнопок
    val horizontalPadding = if (height <= 42.dp) 8.dp else 12.dp
    val iconSpacing = if (height <= 42.dp) 4.dp else 6.dp
    val iconSize = if (height <= 42.dp) 15.dp else 18.dp

    // Вычисляем оптимальный контрастный цвет текста
    val effectiveContentColor = remember(containerColor, contentColor, enabled) {
        if (!enabled) {
            Color(0xFFCBD5E1)
        } else if (contentColor == Color.White) {
            val luminance = (0.299f * containerColor.red + 0.587f * containerColor.green + 0.114f * containerColor.blue)
            if (luminance > 0.65f) Color(0xFF09090B) else Color.White
        } else {
            contentColor
        }
    }

    // Мягкий темный оттенок для объемного 3D скоса/тени
    val darkerColor = remember(containerColor) {
        val luminance = (0.299f * containerColor.red + 0.587f * containerColor.green + 0.114f * containerColor.blue)
        if (luminance > 0.8f) {
            Color(0xFFCBD5E1)
        } else {
            containerColor.copy(
                red = (containerColor.red * 0.72f).coerceIn(0f, 1f),
                green = (containerColor.green * 0.72f).coerceIn(0f, 1f),
                blue = (containerColor.blue * 0.72f).coerceIn(0f, 1f)
            )
        }
    }

    val disabledColor = Color(0xFF94A3B8)
    val actualBg = if (enabled) containerColor else disabledColor
    val actualBevel = if (enabled) darkerColor else Color(0xFF64748B)

    Box(
        modifier = modifier
            .height(height + depth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                SoundHapticManager.performClickHaptic()
                onClick()
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Нижний объемный слой 3D кнопки (тень/скос)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = depth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualBevel)
        )

        // 2. Лицевой слой кнопки с пружинным продавливанием и легким бликом
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = offsetY)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            actualBg,
                            actualBg.copy(
                                red = (actualBg.red * 0.94f).coerceIn(0f, 1f),
                                green = (actualBg.green * 0.94f).coerceIn(0f, 1f),
                                blue = (actualBg.blue * 0.94f).coerceIn(0f, 1f)
                            )
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Верхняя полупрозрачная полоска-блик для мультяшного объема
            if (enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height * 0.40f)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
            ) {
                if (icon != null) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = effectiveContentColor,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(iconSpacing))
                }

                Text(
                    text = text,
                    fontFamily = UnboundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = effectiveFontSize,
                    color = effectiveContentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
