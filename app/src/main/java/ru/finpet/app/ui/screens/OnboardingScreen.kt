package ru.finpet.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.model.*
import ru.finpet.app.ui.components.CartoonButton
import ru.finpet.app.ui.components.PetAvatarView
import ru.finpet.app.ui.components.bounceClick
import ru.finpet.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun OnboardingScreen(
    onComplete: (
        playerName: String,
        playerAge: Int,
        playerAvatar: String,
        appTheme: AppTheme,
        petName: String,
        petType: PetType,
        petColor: PetColor,
        petAccessory: PetAccessory
    ) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) } // 0: Профиль + Галерея + Тема, 1: Создание питомца

    var playerName by remember { mutableStateOf("Саша") }
    var selectedAge by remember { mutableIntStateOf(10) }
    var selectedAvatar by remember { mutableStateOf("fox_hero") }
    var customAvatarPath by remember { mutableStateOf<String?>(null) }
    var selectedTheme by remember { mutableStateOf(AppTheme.CYBER_BLUE) }

    var petName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PetType.FOX) }
    var selectedColor by remember { mutableStateOf(PetColor.ORANGE) }
    var selectedAccessory by remember { mutableStateOf(PetAccessory.NONE) }

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
                customAvatarPath = avatarFile.absolutePath
                GameRepository.setCustomAvatar(avatarFile.absolutePath)
                SoundHapticManager.performSuccessHaptic()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val animatedBgColor by animateColorAsState(
        targetValue = selectedTheme.bgLight,
        animationSpec = tween(250),
        label = "onboardingBgColor"
    )
    val animatedPrimaryColor by animateColorAsState(
        targetValue = selectedTheme.primaryLight,
        animationSpec = tween(250),
        label = "onboardingPrimaryColor"
    )

    CompositionLocalProvider(LocalAppTheme provides selectedTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = animatedBgColor
        ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
            },
            label = "onboardingStepTransition"
        ) { currentStep ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                when (currentStep) {
                    0 -> {
                        // Шаг 1: Профиль игрока, галерея и тема оформления
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Давай познакомимся!",
                                fontFamily = UnboundedFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = "Настройте профиль и стиль приложения перед стартом игры.",
                                fontSize = 12.5.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = playerName,
                                onValueChange = { playerName = it },
                                label = { Text("Как тебя зовут?") },
                                placeholder = { Text("Твой никнейм или имя (напр. Саша)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 1. Аватарка профиля: выбор из пресетов или фото из галереи
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "АВАТАР ПРОФИЛЯ",
                                        fontFamily = UnboundedFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextSecondary,
                                        letterSpacing = 0.5.sp
                                    )

                                    // Кнопка загрузки из галереи
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = selectedTheme.bubbleBg,
                                        border = BorderStroke(1.dp, selectedTheme.primaryColor.copy(alpha = 0.4f)),
                                        modifier = Modifier.bounceClick {
                                            SoundHapticManager.performClickHaptic()
                                            galleryLauncher.launch("image/*")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.PhotoLibrary,
                                                contentDescription = null,
                                                tint = selectedTheme.primaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (customAvatarPath != null) "Изменить фото" else "Из галереи",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = selectedTheme.primaryColor
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Если выбрано фото из галереи - отображаем его первым
                                    if (customAvatarPath != null) {
                                        val customBitmap = remember(customAvatarPath) {
                                            BitmapFactory.decodeFile(customAvatarPath)?.asImageBitmap()
                                        }
                                        if (customBitmap != null) {
                                            Surface(
                                                shape = CircleShape,
                                                border = BorderStroke(2.5.dp, selectedTheme.primaryColor),
                                                modifier = Modifier.size(52.dp)
                                            ) {
                                                Image(
                                                    bitmap = customBitmap,
                                                    contentDescription = "Мое фото",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }

                                    val avatars = listOf(
                                        Pair("fox_hero", Icons.Rounded.Pets),
                                        Pair("cat_money", Icons.Rounded.Savings),
                                        Pair("panda_zen", Icons.Rounded.Shield),
                                        Pair("bot_future", Icons.Rounded.SmartToy),
                                        Pair("lion_leader", Icons.Rounded.Star)
                                    )

                                    avatars.forEach { (id, icon) ->
                                        val isSel = (selectedAvatar == id) && (customAvatarPath == null)
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSel) selectedTheme.bubbleBg else SurfaceLight,
                                            border = BorderStroke(
                                                if (isSel) 2.dp else 1.dp,
                                                if (isSel) selectedTheme.primaryColor else OutlineLight
                                            ),
                                            modifier = Modifier
                                                .size(50.dp)
                                                .bounceClick {
                                                    SoundHapticManager.performClickHaptic()
                                                    selectedAvatar = id
                                                    customAvatarPath = null
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    icon,
                                                    contentDescription = null,
                                                    tint = if (isSel) selectedTheme.primaryColor else TextSecondary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ВОЗРАСТ (7–11 ЛЕТ)",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )

                                val ageList = listOf(
                                    Pair(7, "🎒"),
                                    Pair(8, "🎨"),
                                    Pair(9, "🚀"),
                                    Pair(10, "🎮"),
                                    Pair(11, "🏆")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ageList.forEach { (age, emoji) ->
                                        val isSel = selectedAge == age
                                        val chipOffset by animateDpAsState(
                                            targetValue = if (isSel) (-2).dp else 0.dp,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                            label = "ageChipOffset"
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) animatedPrimaryColor else SurfaceLight,
                                            border = BorderStroke(
                                                if (isSel) 2.dp else 1.dp,
                                                if (isSel) animatedPrimaryColor else OutlineLight
                                            ),
                                            shadowElevation = if (isSel) 4.dp else 1.dp,
                                            modifier = Modifier
                                                .offset(y = chipOffset)
                                                .bounceClick {
                                                    SoundHapticManager.performTapHaptic()
                                                    selectedAge = age
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(emoji, fontSize = 14.sp)
                                                Text(
                                                    text = "$age лет",
                                                    fontSize = 12.sp,
                                                    fontFamily = UnboundedFamily,
                                                    fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSel) Color.White else TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Выбор визуальной темы с микроиконками и интерактивным предпросмотром
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ТЕМА ОФОРМЛЕНИЯ",
                                    fontFamily = UnboundedFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val themeSwitcher = LocalThemeSwitcher.current
                                    AppTheme.values().forEach { theme ->
                                        val isSel = selectedTheme == theme
                                        val themeOffset by animateDpAsState(
                                            targetValue = if (isSel) (-2).dp else 0.dp,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                            label = "themeChipOffset"
                                        )
                                        val themeEmoji = when (theme) {
                                            AppTheme.CYBER_BLUE -> "🌊"
                                            AppTheme.SAKURA_BERRY -> "🌸"
                                            AppTheme.MONOCHROME_MINIMAL -> "🌙"
                                        }
                                        var tapPos by remember { mutableStateOf(Offset.Zero) }
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) theme.bubbleBg else SurfaceLight,
                                            border = BorderStroke(
                                                if (isSel) 2.5.dp else 1.dp,
                                                if (isSel) theme.primaryLight else OutlineLight
                                            ),
                                            shadowElevation = if (isSel) 4.dp else 1.dp,
                                            modifier = Modifier
                                                .offset(y = themeOffset)
                                                .onGloballyPositioned { coordinates ->
                                                    val pos = coordinates.positionInRoot()
                                                    tapPos = Offset(
                                                        pos.x + coordinates.size.width / 2f,
                                                        pos.y + coordinates.size.height / 2f
                                                    )
                                                }
                                                .bounceClick {
                                                    SoundHapticManager.performTapHaptic()
                                                    selectedTheme = theme
                                                    themeSwitcher(theme, tapPos)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(themeEmoji, fontSize = 14.sp)
                                                val circleColor = if (theme == AppTheme.MONOCHROME_MINIMAL) Color(0xFF18181B) else theme.primaryLight
                                                val circleBorder = if (theme == AppTheme.MONOCHROME_MINIMAL) BorderStroke(1.dp, Color(0xFF71717A)) else null
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(circleColor)
                                                        .then(if (circleBorder != null) Modifier.border(circleBorder.width, circleBorder.brush, CircleShape) else Modifier)
                                                )
                                                Text(
                                                    text = theme.title,
                                                    fontSize = 11.5.sp,
                                                    fontFamily = UnboundedFamily,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Кнопка перехода к питомцу с динамическим акцентным цветом
                        CartoonButton(
                            text = "Выбрать питомца →",
                            onClick = {
                                SoundHapticManager.performClickHaptic()
                                step = 1
                            },
                            enabled = playerName.isNotBlank(),
                            containerColor = animatedPrimaryColor,
                            height = 50.dp,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }

                    1 -> {
                        // Шаг 2: Создание и кастомизация питомца
                        val previewPet = Pet(
                            name = petName,
                            type = selectedType,
                            color = selectedColor,
                            accessory = selectedAccessory
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Создай своего питомца!",
                                fontFamily = UnboundedFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // Превью персонажа
                            PetAvatarView(pet = previewPet)

                            OutlinedTextField(
                                value = petName,
                                onValueChange = { petName = it },
                                label = { Text("Имя питомца") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text(
                                text = "1. Вид питомца:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = TextPrimary,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PetType.values().forEach { pType ->
                                    val isSelected = selectedType == pType
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick {
                                                SoundHapticManager.performClickHaptic()
                                                selectedType = pType
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) selectedTheme.bubbleBg else SurfaceLight,
                                        border = if (isSelected) BorderStroke(1.5.dp, selectedTheme.primaryColor) else BorderStroke(1.dp, OutlineLight)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Rounded.Pets,
                                                contentDescription = null,
                                                tint = if (isSelected) selectedTheme.primaryColor else TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                pType.title.split(" ")[0],
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) selectedTheme.primaryColor else TextPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "2. Окрас:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = TextPrimary,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PetColor.values().forEach { pColor ->
                                    val isSelected = selectedColor == pColor
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick {
                                                SoundHapticManager.performClickHaptic()
                                                selectedColor = pColor
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) selectedTheme.bubbleBg else SurfaceLight,
                                        border = if (isSelected) BorderStroke(1.5.dp, selectedTheme.primaryColor) else BorderStroke(1.dp, OutlineLight)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(pColor.primaryColorHex))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (pColor) {
                                                    PetColor.ORANGE -> "Рыжий"
                                                    PetColor.GOLDEN -> "Золотой"
                                                    PetColor.SNOW -> "Белый"
                                                },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) selectedTheme.primaryColor else TextPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "3. Стартовый аксессуар:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = TextPrimary,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PetAccessory.starterAccessories.forEach { pAcc ->
                                    val isSelected = selectedAccessory == pAcc
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .bounceClick {
                                                SoundHapticManager.performClickHaptic()
                                                selectedAccessory = pAcc
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) selectedTheme.bubbleBg else SurfaceLight,
                                        border = if (isSelected) BorderStroke(1.5.dp, selectedTheme.primaryColor) else BorderStroke(1.dp, OutlineLight)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                if (pAcc == PetAccessory.NONE) Icons.Rounded.Block else Icons.Rounded.Checkroom,
                                                contentDescription = null,
                                                tint = if (isSelected) selectedTheme.primaryColor else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (pAcc == PetAccessory.NONE) "Нет" else pAcc.title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) selectedTheme.primaryColor else TextSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CartoonButton(
                                text = "Назад",
                                onClick = {
                                    SoundHapticManager.performClickHaptic()
                                    step = 0
                                },
                                containerColor = SurfaceVariantLight,
                                contentColor = TextPrimary,
                                height = 48.dp,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )

                            CartoonButton(
                                text = "В игру! ✨",
                                onClick = {
                                    SoundHapticManager.performSuccessHaptic()
                                    onComplete(
                                        playerName,
                                        selectedAge,
                                        selectedAvatar,
                                        selectedTheme,
                                        petName,
                                        selectedType,
                                        selectedColor,
                                        selectedAccessory
                                    )
                                },
                                containerColor = animatedPrimaryColor,
                                height = 48.dp,
                                fontSize = 13.5.sp,
                                modifier = Modifier.weight(1.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
}
