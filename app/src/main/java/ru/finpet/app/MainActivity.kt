package ru.finpet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.rustore.RuStoreManager
import ru.finpet.app.ui.components.GameDialogHost
import ru.finpet.app.ui.components.RollingCounterText
import ru.finpet.app.ui.components.SpotlightTutorialOverlay
import ru.finpet.app.ui.components.TutorialStep
import ru.finpet.app.ui.screens.*
import ru.finpet.app.ui.theme.*

enum class NavScreen(val title: String, val icon: ImageVector) {
    PET("Питомец", Icons.Rounded.Pets),
    BUDGET("Бюджет", Icons.Rounded.AccountBalanceWallet),
    QUESTS("Квесты", Icons.Rounded.School),
    SHOP("Магазин", Icons.Rounded.ShoppingBag),
    PROFILE("Профиль", Icons.Rounded.Person),
    GOALS("Копилка", Icons.Rounded.EmojiEvents),
    GLOSSARY("Словарь", Icons.AutoMirrored.Rounded.MenuBook),
    PARENT("Родителям", Icons.Rounded.SupervisorAccount),
    PET_ROOM("Комната", Icons.Rounded.MeetingRoom),
    MINI_GAME("Мини-игры", Icons.Rounded.SportsEsports),
    FRAUD_SIMULATOR("Антифрод", Icons.Rounded.Security),
    FINANCE_LAB("Фин-лаб", Icons.Rounded.Analytics)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        // Инициализация базы данных и репозитория
        GameRepository.init(this)
        RuStoreManager.init(this)

        setContent {
            val state by GameRepository.gameState.collectAsState()
            // Светлая тема активна для «Синий» и «Розовый», а темная (Dark Mode) — строго для «Черный»
            val effectiveDark = state.appTheme == AppTheme.MONOCHROME_MINIMAL

            // Синхронизация системных иконок статус-бара с выбранной темой (для светлых фонов - темные иконки, для монохрома/темных - белые)
            LaunchedEffect(effectiveDark) {
                androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !effectiveDark
                    isAppearanceLightNavigationBars = !effectiveDark
                }
            }

            FinPetTheme(darkTheme = effectiveDark, appTheme = state.appTheme) {
                var revealBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                var tapCenter by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                val revealAnim = remember { androidx.compose.animation.core.Animatable(0f) }
                val coroutineScope = rememberCoroutineScope()
                val currentView = androidx.compose.ui.platform.LocalView.current

                val onSwitchThemeWithReveal: (AppTheme, androidx.compose.ui.geometry.Offset) -> Unit = { newTheme, offset ->
                    if (newTheme != state.appTheme) {
                        try {
                            val w = currentView.width
                            val h = currentView.height
                            if (w > 0 && h > 0) {
                                val bmp = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bmp)
                                currentView.draw(canvas)
                                revealBitmap = bmp.asImageBitmap()
                                tapCenter = offset
                            }
                        } catch (e: Exception) {
                            revealBitmap = null
                        }

                        GameRepository.updateAppTheme(newTheme)

                        if (revealBitmap != null) {
                            coroutineScope.launch {
                                revealAnim.snapTo(0f)
                                revealAnim.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = 480,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    )
                                )
                                revealBitmap = null
                            }
                        }
                    }
                }

                CompositionLocalProvider(
                    LocalThemeSwitcher provides onSwitchThemeWithReveal
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                val currentTheme = LocalAppTheme.current
                val animatedBgColor by animateColorAsState(
                    targetValue = currentTheme.bgLight,
                    animationSpec = tween(350),
                    label = "mainBg"
                )
                val backStack = remember { mutableStateListOf<NavScreen>() }
                var activeMainTab by rememberSaveable { mutableStateOf(NavScreen.PET) }
                val currentScreen = backStack.lastOrNull() ?: activeMainTab
                val isSubScreen = currentScreen in listOf(
                    NavScreen.PET_ROOM,
                    NavScreen.MINI_GAME,
                    NavScreen.FRAUD_SIMULATOR,
                    NavScreen.FINANCE_LAB,
                    NavScreen.GLOSSARY,
                    NavScreen.PARENT,
                    NavScreen.GOALS
                )

                val navigateTo: (NavScreen) -> Unit = { screen ->
                    ru.finpet.app.audio.SoundHapticManager.performClickHaptic()
                    if (screen in listOf(NavScreen.PET, NavScreen.BUDGET, NavScreen.QUESTS, NavScreen.SHOP, NavScreen.PROFILE)) {
                        activeMainTab = screen
                        backStack.clear()
                    } else {
                        backStack.add(screen)
                    }
                }

                val navigateBack: () -> Unit = {
                    ru.finpet.app.audio.SoundHapticManager.performClickHaptic()
                    if (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    } else {
                        activeMainTab = NavScreen.PET
                    }
                }

                BackHandler(enabled = isSubScreen || backStack.isNotEmpty()) {
                    navigateBack()
                }

                var showFinScoreInfo by remember { mutableStateOf(false) }
                var showBalanceInfo by remember { mutableStateOf(false) }

                // Автоматический запуск интерактивного обучения
                LaunchedEffect(state.isOnboardingCompleted, state.isTutorialCompleted) {
                    if (state.isOnboardingCompleted && !state.isTutorialCompleted && state.activeTutorialStep == null) {
                        GameRepository.startInteractiveTutorial()
                    }
                }

                // Синхронизация текущей вкладки с шагом интерактивного обучения
                LaunchedEffect(state.activeTutorialStep) {
                    state.activeTutorialStep?.let { step ->
                        if (currentScreen != step.tab) {
                            navigateTo(step.tab)
                        }
                    }
                }

                // Синхронизация шага обучения при переключении вкладок в нижнем баре
                LaunchedEffect(currentScreen) {
                    if (state.activeTutorialStep != null) {
                        val matchingStep = TutorialStep.values().find { it.tab == currentScreen }
                        if (matchingStep != null && matchingStep != state.activeTutorialStep) {
                            GameRepository.setTutorialStep(matchingStep)
                        }
                    }
                }

                // Сквозной сценарий: если онбординг не пройден, показываем онбординг и создание питомца
                if (!state.isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = { playerName, playerAge, playerAvatar, appTheme, petName, petType, petColor, petAccessory ->
                            GameRepository.completeOnboardingAndCreatePet(
                                playerName = playerName,
                                playerAge = playerAge,
                                playerAvatar = playerAvatar,
                                appTheme = appTheme,
                                petName = petName,
                                petType = petType,
                                petColor = petColor,
                                petAccessory = petAccessory
                            )
                        }
                    )
                } else {
                    Scaffold(
                        containerColor = animatedBgColor,
                        topBar = {
                            if (false) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                                        color = SurfaceLight,
                                        border = BorderStroke(1.5.dp, OutlineLight.copy(alpha = 0.5f)),
                                        shadowElevation = 3.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .statusBarsPadding()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Левый блок: Название текущего экрана и кнопка "Назад" для вложенных экранов
                                        Row(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isSubScreen) {
                                                IconButton(
                                                    onClick = { navigateBack() },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                        contentDescription = "Назад",
                                                        tint = BrandVioletPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            val customPath = state.customAvatarPath
                                            if (customPath != null && java.io.File(customPath).exists()) {
                                                val bitmap = remember(customPath) {
                                                    android.graphics.BitmapFactory.decodeFile(customPath)?.asImageBitmap()
                                                }
                                                if (bitmap != null) {
                                                    androidx.compose.foundation.Image(
                                                        bitmap = bitmap,
                                                        contentDescription = "Аватар",
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .clickable { navigateTo(NavScreen.PROFILE) }
                                                    )
                                                }
                                            }
                                            Text(
                                                text = currentScreen.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(if (isSubScreen) 3.dp else 4.dp)
                                        ) {
                                            if (!isSubScreen) {
                                                IconButton(
                                                    onClick = {
                                                        navigateTo(if (currentScreen == NavScreen.PARENT) NavScreen.PET else NavScreen.PARENT)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.SupervisorAccount,
                                                        contentDescription = "Родительский раздел",
                                                        tint = if (currentScreen == NavScreen.PARENT) BrandVioletPrimary else TextSecondary,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                            }

                                            // Чип финансового рейтинга
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = SurfaceSubtle,
                                                border = BorderStroke(1.dp, OutlineLight),
                                                modifier = Modifier.clickable { showFinScoreInfo = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = if (isSubScreen) 6.dp else 8.dp,
                                                        vertical = 4.dp
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Star,
                                                        contentDescription = null,
                                                        tint = FinCoinGold,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "${state.pet.finScore}/100",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }

                                            // Чип кошелька (монеты)
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = SurfaceSubtle,
                                                border = BorderStroke(1.dp, OutlineLight),
                                                modifier = Modifier.clickable { showBalanceInfo = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = if (isSubScreen) 6.dp else 8.dp,
                                                        vertical = 4.dp
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Savings,
                                                        contentDescription = null,
                                                        tint = currentTheme.primaryColor,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    RollingCounterText(
                                                        value = state.totalCoins,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (state.activeTutorialStep != null) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                // Блокируем клики по шапке во время обучения
                                            }
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                            val isDark = state.appTheme == AppTheme.MONOCHROME_MINIMAL
                            
                            val navBarBg = if (isDark) currentTheme.surfaceDark else Color.White
                            val navBarBorder = if (isDark) currentTheme.cardOutlineDark else OutlineLight
                            val navBarActiveColor = if (isDark) currentTheme.primaryDark else currentTheme.primaryLight
                            val navBarInactiveColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

                            Surface(
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                color = navBarBg,
                                border = BorderStroke(1.dp, navBarBorder.copy(alpha = 0.35f)),
                                shadowElevation = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val mainTabs = listOf(
                                                NavScreen.PET,
                                                NavScreen.BUDGET,
                                                NavScreen.QUESTS,
                                                NavScreen.SHOP,
                                                NavScreen.PROFILE
                                            )
                                            mainTabs.forEach { screen ->
                                                val isSelected = when (currentScreen) {
                                                    NavScreen.GLOSSARY, NavScreen.FINANCE_LAB, NavScreen.FRAUD_SIMULATOR -> screen == NavScreen.QUESTS
                                                    NavScreen.PARENT -> screen == NavScreen.PROFILE
                                                    NavScreen.PET_ROOM, NavScreen.MINI_GAME -> screen == NavScreen.PET
                                                    NavScreen.GOALS -> screen == NavScreen.BUDGET
                                                    else -> (currentScreen == screen) || (isSubScreen && activeMainTab == screen)
                                                }

                                                val pillAlpha by animateFloatAsState(
                                                    targetValue = if (isSelected) 1.0f else 0.0f,
                                                    animationSpec = tween(durationMillis = 180),
                                                    label = "tabPillAlpha"
                                                )

                                                val activePillColor = if (isDark) {
                                                    navBarActiveColor.copy(alpha = 0.20f)
                                                } else {
                                                    navBarActiveColor.copy(alpha = 0.12f)
                                                }

                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clickable(
                                                            enabled = state.activeTutorialStep == null,
                                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            if (!isSelected) {
                                                                SoundHapticManager.performClickHaptic()
                                                            }
                                                            navigateTo(screen)
                                                        },
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(52.dp)
                                                            .height(30.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (pillAlpha > 0f) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .clip(RoundedCornerShape(15.dp))
                                                                    .background(activePillColor.copy(alpha = activePillColor.alpha * pillAlpha))
                                                            )
                                                        }

                                                        Icon(
                                                            imageVector = screen.icon,
                                                            contentDescription = screen.title,
                                                            tint = if (isSelected) navBarActiveColor else navBarInactiveColor,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = screen.title,
                                                        fontSize = 10.5.sp,
                                                        fontFamily = UnboundedFamily,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) navBarActiveColor else navBarInactiveColor,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Защитный оверлей: во время обучения нижнее меню полностью блокируется для нажатий
                                    if (state.activeTutorialStep != null) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    // Поглощаем любые клики по навигации во время показа туториала
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).background(BackgroundLight).clipToBounds()) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(160)) + scaleIn(initialScale = 0.98f, animationSpec = tween(160)))
                                        .togetherWith(fadeOut(animationSpec = tween(100)))
                                },
                                label = "screenTransition"
                            ) { screen ->
                                when (screen) {
                                    NavScreen.PET -> PetMainScreen(
                                        state = state,
                                        onNavigateToBudget = { navigateTo(NavScreen.BUDGET) },
                                        onNavigateToShop = { navigateTo(NavScreen.SHOP) },
                                        onNavigateToGoals = { navigateTo(NavScreen.GOALS) },
                                        onNavigateToQuests = { navigateTo(NavScreen.QUESTS) },
                                        onNavigateToGlossary = { navigateTo(NavScreen.GLOSSARY) },
                                        onNavigateToMiniGame = { navigateTo(NavScreen.MINI_GAME) },
                                        onNavigateToFinanceLab = { navigateTo(NavScreen.FINANCE_LAB) },
                                        onNavigateToRoom = { navigateTo(NavScreen.PET_ROOM) },
                                        onPlayClick = { GameRepository.playWithPet() },
                                        onSleepClick = { GameRepository.sleepPet() },
                                        onAdvancePeriod = { GameRepository.advanceToNextPeriod() },
                                        onHelpClick = { GameRepository.showHelpAdvice() }
                                    )
                                    NavScreen.PET_ROOM -> PetRoomScreen(
                                        state = state,
                                        onBack = { navigateBack() },
                                        onNavigateToShop = { navigateTo(NavScreen.SHOP) }
                                    )
                                    NavScreen.BUDGET -> BudgetScreen(
                                        state = state,
                                        onUpdatePlan = { needs, wants, savings ->
                                             GameRepository.updateBudgetPlan(needs, wants, savings)
                                        },
                                        onConfirmPlan = { GameRepository.confirmBudgetPlan() }
                                    )
                                    NavScreen.QUESTS -> QuestsScreen(
                                        state = state,
                                        onAnswerQuest = { qId, optIdx -> GameRepository.answerQuest(qId, optIdx) }
                                    )
                                    NavScreen.SHOP -> ShopScreen(
                                        state = state,
                                        onBuyItem = { item -> GameRepository.confirmPurchase(item) }
                                    )
                                    NavScreen.PROFILE -> ProfileScreen(
                                        state = state,
                                        onNavigateToGlossary = { navigateTo(NavScreen.GLOSSARY) },
                                        onNavigateToFinanceLab = { navigateTo(NavScreen.FINANCE_LAB) },
                                        onNavigateToParentHub = { navigateTo(NavScreen.PARENT) }
                                    )
                                    NavScreen.GOALS -> GoalsScreen(
                                        state = state,
                                        onSelectGoal = { goalId -> GameRepository.selectGoal(goalId) },
                                        onDeposit = { amount -> GameRepository.depositToActiveGoal(amount) },
                                        onRequestWithdraw = { amount -> GameRepository.requestWithdrawFromGoal(amount) },
                                        onCreateGoal = { title, amount, icon ->
                                             GameRepository.createNewGoal(title, amount, icon)
                                        },
                                        onBack = { navigateBack() }
                                    )
                                    NavScreen.GLOSSARY -> GlossaryScreen(
                                        state = state,
                                        onBack = { navigateBack() }
                                    )
                                    NavScreen.PARENT -> ParentHubScreen(
                                        state = state,
                                        onUnlock = { unlocked -> GameRepository.setParentUnlocked(unlocked) },
                                        onApproveTask = { taskId -> GameRepository.approveParentTask(taskId) },
                                        onAddNewTask = { title, reward -> GameRepository.addParentTask(title, reward) },
                                        onAwardDirectCoins = { amount, reason ->
                                             GameRepository.awardParentCoins(amount, reason)
                                        },
                                        onToggleDemoMode = { enabled -> GameRepository.toggleDemoMode(enabled) },
                                        onResetTestProfile = {
                                            GameRepository.resetTestProfile()
                                            navigateTo(NavScreen.PET)
                                        },
                                        onBack = { navigateBack() }
                                    )
                                    NavScreen.MINI_GAME -> MiniGameScreen(
                                        onBack = { navigateBack() }
                                    )
                                    NavScreen.FRAUD_SIMULATOR -> FraudSimulatorScreen(
                                        onNavigateBack = { navigateBack() }
                                    )
                                    NavScreen.FINANCE_LAB -> FinanceLabScreen(
                                        onBack = { navigateBack() }
                                    )
                                }
                            }

                            // Модальный диалог пояснения финансового статуса
                            if (showFinScoreInfo) {
                                val pet = state.pet
                                val titleRating = when {
                                    pet.finScore >= 90 -> "Финансовый гуру 👑"
                                    pet.finScore >= 75 -> "Мастер бюджета 🎓"
                                    pet.finScore >= 50 -> "Осознанный финансист ⭐"
                                    else -> "Начинающий исследователь 🌱"
                                }
                                AlertDialog(
                                    onDismissRequest = { showFinScoreInfo = false },
                                    icon = { Icon(Icons.Rounded.Star, contentDescription = null, tint = currentTheme.primaryColor, modifier = Modifier.size(34.dp)) },
                                    title = {
                                        Text(
                                            text = "Фин-статус: ${pet.finScore} из 100",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = BrandVioletPrimary.copy(alpha = 0.1f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Звание: $titleRating",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = BrandVioletPrimary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                            Text(
                                                text = "Фин-статус показывает уровень твоей финансовой грамотности и финансовой дисциплины.",
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "💡 Как повышать:\n• Решай ежедневные квесты\n• Не превышай бюджет периода\n• Откладывай монетки в копилку\n• Проходи фрод-тренажер без ошибок",
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = { showFinScoreInfo = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandVioletPrimary)
                                        ) {
                                            Text("Понятно")
                                        }
                                    }
                                )
                            }

                            // Модальный диалог пояснения баланса монет
                            if (showBalanceInfo) {
                                AlertDialog(
                                    onDismissRequest = { showBalanceInfo = false },
                                    icon = { Icon(Icons.Rounded.Savings, contentDescription = null, tint = currentTheme.primaryColor, modifier = Modifier.size(34.dp)) },
                                    title = {
                                        Text(
                                            text = "Твой капитал",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = SurfaceSubtle,
                                                border = BorderStroke(1.dp, OutlineLight),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text("В кошельке:", fontSize = 13.sp, color = TextSecondary)
                                                        Text("${state.totalCoins} монет", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                    }
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text("В копилках:", fontSize = 13.sp, color = TextSecondary)
                                                        Text("${state.totalSavingsAmount} монет", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandVioletPrimary)
                                                    }
                                                    HorizontalDivider(color = OutlineLight)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text("Всего капитала:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                        Text("${state.totalCoins + state.totalSavingsAmount} монет", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = BrandVioletPrimary)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "Карманные деньги текущего периода: ${state.budgetPlan.availableAmount} 🪙",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = { showBalanceInfo = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandVioletPrimary)
                                        ) {
                                            Text("Отлично")
                                        }
                                    }
                                )
                            }

                            // Хост всех обучающих диалогов
                            GameDialogHost(
                                activeDialog = state.activeDialog,
                                onDismiss = { GameRepository.dismissDialog() },
                                onConfirmPurchase = { item -> GameRepository.confirmPurchase(item) },
                                onConfirmWithdrawal = { goalId, amount ->
                                    GameRepository.confirmWithdrawal(goalId, amount)
                                },
                                onGoToQuests = {
                                    navigateTo(NavScreen.QUESTS)
                                },
                                onGoToBudget = {
                                    navigateTo(NavScreen.BUDGET)
                                },
                                onGoToRoom = {
                                    navigateTo(NavScreen.PET_ROOM)
                                }
                            )
                        }
                    }
                }

                // Интерактивное обучение (Spotlight Tutorial Overlay) на уровне всего экрана
                SpotlightTutorialOverlay(
                    currentStep = state.activeTutorialStep,
                    pet = state.pet,
                    onStepCompleted = { step ->
                        val nextStep = when (step) {
                            TutorialStep.PET -> TutorialStep.BUDGET
                            TutorialStep.BUDGET -> TutorialStep.QUESTS
                            TutorialStep.QUESTS -> TutorialStep.SHOP
                            TutorialStep.SHOP -> TutorialStep.PROFILE
                            TutorialStep.PROFILE -> null
                        }
                        if (nextStep != null) {
                            navigateTo(nextStep.tab)
                        } else {
                            navigateTo(NavScreen.PET)
                        }
                        GameRepository.advanceTutorialStep(step)
                    },
                    onSkip = {
                        GameRepository.skipInteractiveTutorial()
                        navigateTo(NavScreen.PET)
                    }
                )

                ThemeRevealOverlay(
                    revealBitmap = revealBitmap,
                    tapCenter = tapCenter,
                    progress = revealAnim.value
                )
                } // Box
            } // Provider
            } // FinPetTheme
        }
    }
}
