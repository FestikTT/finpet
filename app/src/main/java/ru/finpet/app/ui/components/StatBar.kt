package ru.finpet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.ui.theme.SurfaceVariantLight
import ru.finpet.app.ui.theme.TextPrimary
import ru.finpet.app.ui.theme.TextSecondary

@Composable
fun StatBar(
    label: String,
    value: Float, // 0.0 .. 1.0
    startColor: Color,
    endColor: Color,
    iconEmoji: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "statProgress"
    )

    // Мягкая пульсация светящегося маячка на кончике шкалы
    val infiniteTransition = rememberInfiniteTransition(label = "beaconPulse")
    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconScale"
    )
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(text = iconEmoji, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val barWidth = maxWidth
            // 1. Фоновая колея трека
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceVariantLight)
            )

            // 2. Заполненная часть с градиентом
            if (animatedProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(barWidth * animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(startColor, endColor)))
                )

                // 3. Светящийся пульсирующий маячок-кап на кончике полосы прогресса
                val beaconOffset = (barWidth * animatedProgress) - 4.dp
                Box(
                    modifier = Modifier
                        .offset(x = beaconOffset.coerceAtLeast(0.dp))
                        .size(8.dp)
                        .scale(beaconScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = beaconAlpha),
                                    endColor.copy(alpha = beaconAlpha * 0.85f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun StatBar(
    label: String,
    value: Float,
    color: Color,
    iconEmoji: String,
    modifier: Modifier = Modifier
) {
    StatBar(
        label = label,
        value = value,
        startColor = color,
        endColor = color.copy(alpha = 0.85f),
        iconEmoji = iconEmoji,
        modifier = modifier
    )
}
