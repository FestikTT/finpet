package ru.finpet.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.data.GameState
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    state: GameState,
    onNavigateToGlossary: () -> Unit,
    onNavigateToFinanceLab: () -> Unit = {},
    onNavigateToParentHub: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = LocalAppTheme.current
    val pet = state.pet

    // Лаунчер выбора фото из галереи устройства
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val avatarFile = File(context.filesDir, "player_avatar.png")
                val outputStream = FileOutputStream(avatarFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                GameRepository.setCustomAvatar(avatarFile.absolutePath)
                SoundHapticManager.performSuccessHaptic()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var showFinScoreDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Стильная карточка профиля игрока и питомца
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceLight,
            border = BorderStroke(1.5.dp, currentTheme.borderColor),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Аватар игрока (кастомный из галереи или векторный пресет)
                    Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = currentTheme.bubbleBg,
                            border = BorderStroke(2.dp, currentTheme.primaryColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxSize()
                                .bounceClick {
                                    SoundHapticManager.performClickHaptic()
                                    galleryLauncher.launch("image/*")
                                }
                        ) {
                            val customPath = state.customAvatarPath
                            if (customPath != null && File(customPath).exists()) {
                                val bitmap = remember(customPath) {
                                    BitmapFactory.decodeFile(customPath)?.asImageBitmap()
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Аватар профиля",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = currentTheme.primaryColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = currentTheme.primaryColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        // Маленькая иконка камеры/галереи в углу
                        Surface(
                            shape = CircleShape,
                            color = currentTheme.primaryColor,
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 4.dp, y = 4.dp)
                                .clickable {
                                    SoundHapticManager.performClickHaptic()
                                    galleryLauncher.launch("image/*")
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PhotoCamera,
                                    contentDescription = "Изменить фото",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.playerName,
                                fontFamily = UnboundedFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )

                            // Бейдж возраста
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = currentTheme.primaryColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${state.playerAge} лет",
                                    fontSize = 11.sp,
                                    fontFamily = UnboundedFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = currentTheme.primaryColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Питомец:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "${pet.name} (${pet.type.title})",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = currentTheme.primaryColor
                            )
                        }

                        Text(
                            text = "Уровень: ${pet.level} • ${pet.currentMood.title}",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = OutlineLight, thickness = 1.dp)

                // 4 ключевых показателя с интерактивными тултипами (Аудит п.16)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ProfileStatChip(
                        icon = Icons.Rounded.Star,
                        label = "Фин-рейтинг ℹ️",
                        value = "${pet.finScore}/100",
                        tint = currentTheme.primaryColor,
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            showFinScoreDialog = true
                        }
                    )
                    ProfileStatChip(
                        icon = Icons.Rounded.LocalFireDepartment,
                        label = "Серия дней ℹ️",
                        value = "${state.questStreakDays} дн.",
                        tint = StatAmberWarm,
                        onClick = {
                            SoundHapticManager.performClickHaptic()
                            showStreakDialog = true
                        }
                    )
                    ProfileStatChip(
                        icon = Icons.Rounded.Savings,
                        label = "В кошельке",
                        value = "${state.totalCoins} 🪙",
                        tint = currentTheme.primaryColor
                    )
                    ProfileStatChip(
                        icon = Icons.Rounded.Flag,
                        label = "В копилке",
                        value = "${state.totalSavingsAmount} 🪙",
                        tint = currentTheme.primaryColor
                    )
                }
            }
        }

        // 2. Блок выбора темы оформления
        Text(
            text = "ТЕМА ОФОРМЛЕНИЯ",
            fontFamily = UnboundedFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.8.sp
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceLight,
            border = BorderStroke(1.dp, OutlineLight),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Выберите визуальный стиль интерфейса:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                val themeSwitcher = LocalThemeSwitcher.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.values().forEach { themeOption ->
                        val isSelected = state.appTheme == themeOption
                        val themeColor: Color = themeOption.primaryLight
                        var tapPos by remember { mutableStateOf(Offset.Zero) }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coordinates ->
                                    val pos = coordinates.positionInRoot()
                                    tapPos = Offset(
                                        pos.x + coordinates.size.width / 2f,
                                        pos.y + coordinates.size.height / 2f
                                    )
                                }
                                .bounceClick {
                                    SoundHapticManager.performClickHaptic()
                                    themeSwitcher(themeOption, tapPos)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) themeOption.bubbleBg else SurfaceSubtle,
                            border = BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.5.dp,
                                color = if (isSelected) themeColor else OutlineLight
                            ),
                            shadowElevation = if (isSelected) 3.dp else 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = themeOption.emoji,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = themeOption.title,
                                    fontSize = 11.sp,
                                    fontFamily = UnboundedFamily,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isSelected) themeColor else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Блок "Обучение и азбука финансов" (без 3 правил юного финансиста!)
        Text(
            text = "ОБУЧЕНИЕ И АЗБУКА ФИНАНСОВ",
            fontFamily = UnboundedFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.8.sp
        )

        // Карточка: Азбука юного финансиста (Словарь)
        ProfileNavCard(
            title = "Азбука юного финансиста",
            subtitle = "Понятия и термины: бюджет, инфляция, кешбэк, СБП",
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            accentColor = currentTheme.primaryColor,
            onClick = onNavigateToGlossary
        )

        // Карточка: Фин-лаборатория (Калькуляторы)
        ProfileNavCard(
            title = "Фин-лаборатория и калькуляторы",
            subtitle = "Сложный процент, кофе-эффект, финансовая подушка безопасности",
            icon = Icons.Rounded.Analytics,
            accentColor = currentTheme.primaryColor,
            onClick = onNavigateToFinanceLab
        )

        // Карточка: Раздел для родителей
        ProfileNavCard(
            title = "Раздел для родителей",
            subtitle = "Настройка карманных денег, проверка заданий и пин-код",
            icon = Icons.Rounded.SupervisorAccount,
            accentColor = currentTheme.primaryColor,
            onClick = onNavigateToParentHub
        )


        // 5. Блок "Настройки приложения"
        Text(
            text = "НАСТРОЙКИ",
            fontFamily = UnboundedFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.8.sp
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceLight,
            border = BorderStroke(1.dp, OutlineLight)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Тактильный виброотклик
                var hapticsEnabled by remember { mutableStateOf(SoundHapticManager.isHapticsEnabled) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Тактильный отклик", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(if (hapticsEnabled) "Вибрация включена" else "Вибрация отключена", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { enabled ->
                            hapticsEnabled = enabled
                            SoundHapticManager.isHapticsEnabled = enabled
                            if (enabled) SoundHapticManager.performClickHaptic()
                        }
                    )
                }

                HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f))

                // Фоновая музыка
                val isMusicActive = state.isMusicEnabled
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Фоновая музыка", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(if (isMusicActive) "Уютная мелодия включена" else "Музыка отключена", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = isMusicActive,
                        onCheckedChange = {
                            SoundHapticManager.performClickHaptic()
                            GameRepository.toggleMusic()
                        }
                    )
                }
            }
        }
    }

    if (showFinScoreDialog) {
        AlertDialog(
            onDismissRequest = { showFinScoreDialog = false },
            confirmButton = {
                TextButton(onClick = { showFinScoreDialog = false }) {
                    Text("Понятно", color = currentTheme.primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("⭐ Фин-рейтинг", fontFamily = UnboundedFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Фин-рейтинг отражает финансовую грамотность и заботу о питомце.\n\n" +
                    "• Выполняй квесты (+5-10 баллов)\n" +
                    "• Пополняй копилку (+3 балла)\n" +
                    "• Не забывай кормить и гладить питомца (+1 балл ежедневно)\n\n" +
                    "Максимальный балл — 100! Стань финансовым гуру!",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceLight
        )
    }

    if (showStreakDialog) {
        AlertDialog(
            onDismissRequest = { showStreakDialog = false },
            confirmButton = {
                TextButton(onClick = { showStreakDialog = false }) {
                    Text("Отлично", color = currentTheme.primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("🔥 Серия дней", fontFamily = UnboundedFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Заходи в приложение каждый день и выполняй хотя бы одно действие (покормить питомца или пройти квест), чтобы серия не прервалась.\n\n" +
                    "• За серию в 3 дня: +10 🪙\n" +
                    "• За серию в 7 дней: +30 🪙\n" +
                    "• За серию в 14 дней: редкий сундучок!\n\n" +
                    "Текущая серия: ${state.questStreakDays} дн. подряд!",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceLight
        )
    }
}

@Composable
private fun ProfileStatChip(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = if (onClick != null) Modifier.bounceClick(onClick = onClick) else Modifier
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text = value, fontFamily = UnboundedFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
private fun ProfileNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.5.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
    }
}
