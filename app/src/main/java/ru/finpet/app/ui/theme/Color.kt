package ru.finpet.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ========================================================================
// Дизайн-система FinPet по правилу 60-30-10:
// 60% — Нейтральный фон (чистый минимализм, мягкий светлый / глубокий обсидиан)
// 30% — Структурные элементы (карточки, боттомбар, разделители, рамки)
// 10% — Единственный яркий акцент: Electric Cobalt (#0052FF / #3884FF)
// ========================================================================

// 10% — ЕДИНСТВЕННЫЙ ЯРКИЙ АКЦЕНТ (Sky Azure / Голубой):
val ElectricCobaltPrimary = Color(0xFF0091FF)     // #0091FF - Сочный небесно-голубой для светлой темы
val ElectricCobaltDark = Color(0xFF38BDF8)        // #38BDF8 - Неоновый небесно-голубой для тёмной темы (контраст > 11:1)

// Шкала нейтральных оттенков для структуры (30%):
val BrandObsidian = Color(0xFF09090B)
val BrandCharcoalDark = Color(0xFF18181B)
val BrandCharcoal = Color(0xFF27272A)
val BrandSlateDark = Color(0xFF334155)
val BrandGrayMedium = Color(0xFF475569)
val BrandGraySlate = Color(0xFF334155)
val BrandSilver = Color(0xFF64748B)
val BrandLightGray = Color(0xFFCBD5E1)
val BrandOffWhite = Color(0xFFE2E8F0)
val BrandSoftWhite = Color(0xFFF8FAFC)

// 60% — НЕЙТРАЛЬНЫЕ ФОНЫ (дефолтные значения):
val BackgroundLightVal = Color(0xFF98C5E9)        // Сочный мультяшный небесно-голубой (стиль Bunny Store)
val BackgroundDarkVal = Color(0xFF0F172A)         // #0F172A (Slate-900) - глубокий темный фон

// 30% — СТРУКТУРНЫЕ ЭЛЕМЕНТЫ (Карточки, рамки, текст):
val SurfaceLightVal = Color(0xFFFFFFFF)           // Чистые белые карточки
val SurfaceDarkVal = Color(0xFF1E293B)            // #1E293B (Slate-800) - поднятый слой карточек
val SurfaceSubtleVal = Color(0xFFFFF1F2)          // Внутренние плашки (светлые)
val SurfaceSubtleDarkVal = Color(0xFF1E293B)      // Внутренние плашки (тёмные)
val SurfaceVariantLightVal = Color(0xFFE2E8F0)    // Заливка неактивных чипов
val SurfaceVariantDarkVal = Color(0xFF334155)     // Сегменты темной темы
val OutlineLightVal = Color(0xFFFBCFE8)           // Граница карточек
val OutlineDarkVal = Color(0xFF334155)            // #334155 (Slate-700) - 1dp контурная граница
val DividerColorVal = Color(0xFFE2E8F0)           // Тонкий разделитель списков
val DividerDarkVal = Color(0xFF334155)            // Разделители темной темы

// Высококонтрастная типографика (структура, устранен серый на белом):
val TextPrimaryVal = Color(0xFF09090B)            // Глубокий темный уголь (>18:1 контраст на белом)
val TextPrimaryDarkVal = Color(0xFFF1F5F9)        // #F1F5F9 (Slate-100) - чистый светлый текст
val TextSecondaryVal = Color(0xFF334155)          // Темный сланец - четкий и читаемый (>7.5:1 контраст на белом)
val TextSecondaryDarkVal = Color(0xFF94A3B8)      // #94A3B8 (Slate-400) - светлый сланец для второстепенного текста
val TextLightVal = Color(0xFF475569)              // Подписи и сноски (>5.5:1 контраст на белом)
val TextLightDarkVal = Color(0xFF94A3B8)
val ChipInactiveBgVal = Color(0xFFF1F5F9)         // Неактивные чипы
val ChipInactiveBgDarkVal = Color(0xFF1E293B)
val ChipInactiveTextVal = Color(0xFF334155)       // Текст неактивных чипов (высокий контраст)
val ChipInactiveTextDarkVal = Color(0xFF94A3B8)

// Токены темной темы для навигации и акцентов:
val ChevronDarkVal = Color(0xFF38BDF8)            // #38BDF8 - голубой акцент шевронов
val TabInactiveDarkVal = Color(0xFF64748B)        // #64748B - неактивные табы
val TabActiveDarkVal = Color(0xFF38BDF8)          // #38BDF8 - активный таб с неоновым свечением

// Алиасы токенов с высокой контрастностью:
val BrandPinkNeon = Color(0xFFF43F5E) // Яркий кораллово-розовый
val BrandPinkPastel = Color(0xFFFCE7F3)
val BrandLavender = Color(0xFF8B5CF6) // Фиолетово-лавандовый
val BrandRoseWarm = Color(0xFFF43F5E) // Теплый розовый
val BrandDarkPurple = Color(0xFF18181B)
val BrandVioletPrimaryVal = ElectricCobaltPrimary
val BrandVioletPrimaryDarkVal = ElectricCobaltDark
val BrandWhite = Color(0xFFFFFFFF)
val BrandDarkGraphite = Color(0xFF18181B)

val FinIndigoPrimary = BrandVioletPrimaryVal
val FinIndigoDark = BrandDarkPurple
val FinIndigoLight = Color(0xFFF4F4F5)
val FinPinkPrimary = BrandPinkNeon
val FinPinkLight = BrandPinkPastel
val FinRoseWarm = BrandRoseWarm
val FinLavenderSoft = BrandLavender
val FinGreenSecondary = Color(0xFF10B981)
val FinGreenEmerald = Color(0xFF10B981)
val FinGreenLight = Color(0xFFD1FAE5)
val FinGoldAccent = Color(0xFFF59E0B)
val FinAmberWarning = Color(0xFFF59E0B)
val FinGoldLight = Color(0xFFFEF3C7)
val FinCoralAlert = BrandPinkNeon
val FinCoralAccent = BrandRoseWarm
val FinCoralLight = BrandPinkPastel
val FinBlueSky = ElectricCobaltPrimary
val FinPurpleJoy = Color(0xFF8B5CF6)

val FinGoldDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current) Color(0xFFFCD34D) else Color(0xFFB45309)

enum class AppTheme(
    val id: String,
    val title: String,
    val emoji: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val bgLight: Color,
    val bgDark: Color,
    val surfaceLight: Color,
    val surfaceDark: Color,
    val bubbleSubtleLight: Color,
    val bubbleSubtleDark: Color,
    val bubbleBorderLight: Color,
    val bubbleBorderDark: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val cardOutlineLight: Color,
    val cardOutlineDark: Color
) {
    CYBER_BLUE(
        id = "cyber_blue",
        title = "Синий",
        emoji = "🌊",
        primaryLight = Color(0xFF0088FF),        // #0088FF / #1976D2 — яркий насыщенный синий/голубой
        primaryDark = Color(0xFF38BDF8),
        bgLight = Color(0xFFD8EEFF),             // #D8EEFF — мягкий пастельный небесно-голубой
        bgDark = Color(0xFF0F172A),
        surfaceLight = Color(0xFFFFFFFF),        // #FFFFFF — чисто белые карточки со скругленными углами
        surfaceDark = Color(0xFF1E293B),
        bubbleSubtleLight = Color(0xFFE8F4FD),   // Нежный небесный для чипов
        bubbleSubtleDark = Color(0xFF1E293B),
        bubbleBorderLight = Color(0xFFBAE6FD),
        bubbleBorderDark = Color(0xFF334155),
        gradientStart = Color(0xFF0088FF),
        gradientEnd = Color(0xFF00B0FF),
        cardOutlineLight = Color(0xFFCFE4F7),   // Тонкий мягкий контур
        cardOutlineDark = Color(0xFF334155)
    ),
    SAKURA_BERRY(
        id = "sakura_berry",
        title = "Розовый",
        emoji = "🌸",
        primaryLight = Color(0xFFE91E63),        // #E91E63 — сочный малиново-розовый
        primaryDark = Color(0xFFFB7185),
        bgLight = Color(0xFFFEEBF0),             // #FEEBF0 — нежный пастельный зефирно-розовый
        bgDark = Color(0xFF1C0D16),
        surfaceLight = Color(0xFFFFFFFF),        // #FFFFFF — чисто белые карточки
        surfaceDark = Color(0xFF2E1524),
        bubbleSubtleLight = Color(0xFFFFF0F3),   // Пастельный розовый для чипов
        bubbleSubtleDark = Color(0xFF3D1930),
        bubbleBorderLight = Color(0xFFFECDD3),
        bubbleBorderDark = Color(0xFF4C1E3C),
        gradientStart = Color(0xFFE91E63),
        gradientEnd = Color(0xFFFF4081),
        cardOutlineLight = Color(0xFFFFD6E0),   // Нежная розовая граница
        cardOutlineDark = Color(0xFF4C1E3C)
    ),
    MONOCHROME_MINIMAL(
        id = "monochrome_minimal",
        title = "Черный",
        emoji = "🌙",
        primaryLight = Color(0xFF38BDF8),        // #38BDF8 — неоново-голубой
        primaryDark = Color(0xFF38BDF8),
        bgLight = Color(0xFF12161E),             // #12161E — глубокий графитовый/темно-серый
        bgDark = Color(0xFF12161E),
        surfaceLight = Color(0xFF1B222D),        // #1B222D — темно-серые карточки с аккуратными границами
        surfaceDark = Color(0xFF1B222D),
        bubbleSubtleLight = Color(0xFF212835),
        bubbleSubtleDark = Color(0xFF212835),
        bubbleBorderLight = Color(0xFF2E3849),
        bubbleBorderDark = Color(0xFF2E3849),
        gradientStart = Color(0xFF38BDF8),
        gradientEnd = Color(0xFF818CF8),
        cardOutlineLight = Color(0xFF2E3849),
        cardOutlineDark = Color(0xFF2E3849)
    );

    companion object {
        fun fromId(id: String): AppTheme = values().firstOrNull { it.id == id } ?: CYBER_BLUE
    }
}


val AppTheme.primary: Color
    get() = primaryLight

val AppTheme.primaryColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current || this == AppTheme.MONOCHROME_MINIMAL) primaryDark else primaryLight

val AppTheme.bubbleBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current || this == AppTheme.MONOCHROME_MINIMAL) bubbleSubtleDark else bubbleSubtleLight

val AppTheme.borderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current || this == AppTheme.MONOCHROME_MINIMAL) bubbleBorderDark else bubbleBorderLight

// CompositionLocal для отслеживания текущей темы
val LocalDarkTheme = compositionLocalOf { false }
val LocalAppTheme = compositionLocalOf { AppTheme.CYBER_BLUE }

// Динамические адаптивные цвета (фон экрана всегда соответствует выбранной теме)
val BackgroundLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.bgLight

// Карточки интерфейса — ЧИСТЫЙ БЕЛЫЙ для светлой темы, темный графит для Черного
val SurfaceLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.surfaceLight

// Внутренние нейтральные плашки (не красятся в цвет фона, сохраняют чистоту интерфейса)
val SurfaceSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.bubbleSubtleLight

val SurfaceVariantLight: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        val isDark = LocalDarkTheme.current || theme == AppTheme.MONOCHROME_MINIMAL
        return if (isDark) SurfaceVariantDarkVal else SurfaceVariantLightVal
    }

// Контуры карточек
val OutlineLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current.cardOutlineLight

val DividerColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val isDark = LocalDarkTheme.current || LocalAppTheme.current == AppTheme.MONOCHROME_MINIMAL
        return if (isDark) DividerDarkVal else DividerColorVal
    }

val StatusBarColor: Color
    @Composable
    @ReadOnlyComposable
    get() = BackgroundLight

// Акцентный цвет активной темы (Cyber Blue, Sakura Berry, Monochrome Minimal):
val BrandVioletPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return if (theme == AppTheme.MONOCHROME_MINIMAL) theme.primaryDark else theme.primaryLight
    }

// Для главных целевых кнопок (CTA):
val BrandButtonPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return if (theme == AppTheme.MONOCHROME_MINIMAL) theme.primaryDark else theme.primaryLight
    }

// Полупрозрачный баббл-фон активной темы (для бокового дока магазина):
val BubbleSubtleBg: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return if (theme == AppTheme.MONOCHROME_MINIMAL) theme.bubbleSubtleDark else theme.bubbleSubtleLight
    }

val BubbleBorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return if (theme == AppTheme.MONOCHROME_MINIMAL) theme.bubbleBorderDark else theme.bubbleBorderLight
    }

// Монетки (теплое золото для выразительности наград):
val FinCoinGold: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalAppTheme.current == AppTheme.MONOCHROME_MINIMAL) Color(0xFFFBBF24) else Color(0xFFD97706)

// Высококонтрастный текст — глубокий уголь на белых карточках, белый в темной теме
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return when (theme) {
            AppTheme.CYBER_BLUE -> Color(0xFF102A43)      // Контрастный темно-синий / графитовый (#102A43)
            AppTheme.SAKURA_BERRY -> Color(0xFF2D151E)    // Темно-бордовый / графитовый (#2D151E)
            AppTheme.MONOCHROME_MINIMAL -> Color(0xFFF0F4F8) // Светлый / белый (#F0F4F8)
        }
    }

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return when (theme) {
            AppTheme.CYBER_BLUE -> Color(0xFF334E68)      // Мягкий сине-серый
            AppTheme.SAKURA_BERRY -> Color(0xFF5C3342)    // Мягкий бордово-серый
            AppTheme.MONOCHROME_MINIMAL -> Color(0xFF94A3B8) // Светлый сланец
        }
    }

val TextLight: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val theme = LocalAppTheme.current
        return when (theme) {
            AppTheme.CYBER_BLUE -> Color(0xFF486581)
            AppTheme.SAKURA_BERRY -> Color(0xFF754558)
            AppTheme.MONOCHROME_MINIMAL -> Color(0xFF94A3B8)
        }
    }

val ChipInactiveBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current || LocalAppTheme.current == AppTheme.MONOCHROME_MINIMAL) ChipInactiveBgDarkVal else ChipInactiveBgVal

val ChipInactiveText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current || LocalAppTheme.current == AppTheme.MONOCHROME_MINIMAL) ChipInactiveTextDarkVal else ChipInactiveTextVal

// Семантические цвета для шкал и индикаторов:
val StatGreenEmerald = Color(0xFF10B981)
val StatAmberWarm = Color(0xFFF59E0B)
val WarningAmberColor = StatAmberWarm
val StatBlueCobalt = ElectricCobaltPrimary

val DangerRed = Color(0xFFEF4444)
val DangerRedSubtleLight = Color(0xFFFEE2E2)
val DangerRedSubtleDark = Color(0xFF451A1A)

val DangerRedColor: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current) Color(0xFFF87171) else DangerRed

val DangerRedSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current) DangerRedSubtleDark else DangerRedSubtleLight

// ==========================================
// ГРАДИЕНТЫ ДЛЯ ЖИВОГО СВЕТА И НЕОНОВЫХ ШКАЛ
// ==========================================
val BrandCtaGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = Brush.horizontalGradient(
        listOf(
            Color(0xFF0091FF), // Sky Azure
            Color(0xFF06B6D4)  // Cyan Light
        )
    )

val StatNeonGreenGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = Brush.horizontalGradient(
        listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF34D399)  // Mint
        )
    )

val StatNeonAmberGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = Brush.horizontalGradient(
        listOf(
            Color(0xFFF59E0B), // Warm Amber
            Color(0xFFFBBF24)  // Gold
        )
    )

val StatNeonCobaltGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = Brush.horizontalGradient(
        listOf(
            Color(0xFF0091FF), // Sky Azure
            Color(0xFF38BDF8)  // Neon Cyan
        )
    )


