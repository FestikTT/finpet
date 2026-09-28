package ru.finpet.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.R
import ru.finpet.app.ui.theme.*

/**
 * Анимированный счетчик с эффектом вертикального перекатывания цифр (Coin Counter Roll)
 */
@Composable
fun RollingCounterText(
    value: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = TextPrimary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val valueStr = value.toString()
        for (char in valueStr) {
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut()
                        )
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut()
                        )
                    }.using(SizeTransform(clip = false))
                },
                label = "rollDigit"
            ) { digit ->
                Text(
                    text = digit.toString(),
                    fontFamily = UnboundedFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    color = color,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CoinBadge(
    coins: Int,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalAppTheme.current
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(coins) {
        scaleAnim.animateTo(1.18f, animationSpec = tween(110, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
    }

    Surface(
        modifier = modifier.scale(scaleAnim.value),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, currentTheme.borderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_coin_vector),
                contentDescription = "Монеты",
                modifier = Modifier.size(16.dp)
            )
            RollingCounterText(
                value = coins,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun BalanceChips(
    coins: Int,
    savings: Int? = null,
    onSavingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalAppTheme.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (savings != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceLight,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, currentTheme.borderColor),
                modifier = if (onSavingsClick != null) Modifier.clickable { onSavingsClick() } else Modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Savings,
                        contentDescription = "Копилка",
                        tint = currentTheme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    RollingCounterText(
                        value = savings,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.primaryColor
                    )
                }
            }
        }
        CoinBadge(coins = coins)
    }
}
