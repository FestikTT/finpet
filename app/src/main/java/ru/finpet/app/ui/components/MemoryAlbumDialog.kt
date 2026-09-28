package ru.finpet.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameState
import ru.finpet.app.model.MemoryAlbumRepository
import ru.finpet.app.model.MemoryPhoto
import ru.finpet.app.ui.theme.*

@Composable
fun MemoryAlbumDialog(
    state: GameState,
    onDismiss: () -> Unit
) {
    val initialMemories = remember(state) { MemoryAlbumRepository.getDefaultMemories(state) }
    val memories = remember { mutableStateListOf<MemoryPhoto>().apply { addAll(initialMemories) } }
    var currentIndex by remember { mutableIntStateOf(0) }
    var showFlash by remember { mutableStateOf(false) }

    val currentPhoto = memories.getOrNull(currentIndex) ?: return

    val animatedTilt by animateFloatAsState(
        targetValue = currentPhoto.tiltDegrees,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "polaroidTilt"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Заголовок альбома и кнопка закрытия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📸", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Фотоальбом воспоминаний",
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Памятные моменты пути к фин-грамотности",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Ретро Polaroid Карточка
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .rotate(animatedTilt)
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFDF8))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Верхняя полоска декоративного скотча (Washi tape)
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0x35E2B686))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Внутреннее окно фотографии
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(currentPhoto.cardColorHex),
                                            Color(0xFFE2E8F0)
                                        )
                                    )
                                )
                                .border(1.dp, Color(0x22000000), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Категория
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, Color(0x15000000))
                                ) {
                                    Text(
                                        text = currentPhoto.category.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = currentPhoto.emoji,
                                    fontSize = 64.sp
                                )

                                Text(
                                    text = currentPhoto.title,
                                    fontFamily = UnboundedFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Значок реакции питомца в углу фото
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(currentPhoto.petReactionEmoji, fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Нижнее широкое поле Polaroid с «рукописной» подписью
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentPhoto.dateFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "№${currentIndex + 1} / ${memories.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Text(
                                text = currentPhoto.caption,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // Элементы перелистывания фотокарточек
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentIndex > 0) {
                                SoundHapticManager.performTickHaptic()
                                currentIndex--
                            }
                        },
                        enabled = currentIndex > 0,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = "Предыдущее",
                            tint = if (currentIndex > 0) Color(0xFF0F172A) else Color.White.copy(alpha = 0.4f)
                        )
                    }

                    // Кнопка быстрого фотоснимка питомца
                    CartoonButton(
                        text = "Сделать снимок 📸",
                        onClick = {
                            SoundHapticManager.playClickSound()
                            SoundHapticManager.performSuccessHaptic()
                            val newPhoto = MemoryPhoto(
                                id = "mem_${System.currentTimeMillis()}",
                                title = "День с ${state.pet.name.ifBlank { "питомцем" }}",
                                dateFormatted = "Сегодня",
                                caption = "Уровень ${state.pet.level} • ${state.pet.evolutionStage.title}! Монеты: ${state.totalCoins} 🪙.",
                                emoji = "✨",
                                category = "Питомец",
                                petReactionEmoji = "🦊💖",
                                tiltDegrees = ((Math.random() * 4) - 2).toFloat(),
                                cardColorHex = 0xFFFFFBEB
                            )
                            memories.add(newPhoto)
                            currentIndex = memories.lastIndex
                        },
                        containerColor = FinGoldAccent,
                        modifier = Modifier.height(42.dp)
                    )

                    IconButton(
                        onClick = {
                            if (currentIndex < memories.lastIndex) {
                                SoundHapticManager.performTickHaptic()
                                currentIndex++
                            }
                        },
                        enabled = currentIndex < memories.lastIndex,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (currentIndex < memories.lastIndex) Color.White else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Следующее",
                            tint = if (currentIndex < memories.lastIndex) Color(0xFF0F172A) else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
