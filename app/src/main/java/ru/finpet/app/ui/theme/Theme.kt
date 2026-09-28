package ru.finpet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandVioletPrimaryVal,          // #0091FF - Сочный небесно-голубой
    onPrimary = BrandWhite,                   // #FFFFFF - Белый текст на кнопках
    primaryContainer = SurfaceSubtleVal,      // #F4F4F5 - Мягкая плашка
    onPrimaryContainer = TextPrimaryVal,      // #09090B

    secondary = BrandRoseWarm,                // #F43F5E - Теплый розовый
    onSecondary = BrandWhite,
    secondaryContainer = SurfaceSubtleVal,
    onSecondaryContainer = TextPrimaryVal,

    tertiary = BrandLavender,                 // #8B5CF6 - Лавандово-фиолетовый
    onTertiary = BrandWhite,
    tertiaryContainer = SurfaceSubtleVal,
    onTertiaryContainer = TextPrimaryVal,

    background = BackgroundLightVal,
    onBackground = TextPrimaryVal,

    surface = SurfaceLightVal,
    onSurface = TextPrimaryVal,
    surfaceVariant = SurfaceVariantLightVal,
    onSurfaceVariant = TextSecondaryVal,

    error = DangerRed,                        // #EF4444 - Четкий красный
    onError = BrandWhite,
    errorContainer = DangerRedSubtleLight,
    onErrorContainer = DangerRed,

    outline = OutlineLightVal,
    outlineVariant = DividerColorVal
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandVioletPrimaryDarkVal,      // #38BDF8 - Неоновый небесно-голубой
    onPrimary = BrandWhite,                   // #FFFFFF - Белый текст на ярких кнопках
    primaryContainer = SurfaceSubtleDarkVal,  // #1A1F2C
    onPrimaryContainer = TextPrimaryDarkVal,  // #FAFAFA

    secondary = BrandLavender,                // #8B5CF6
    onSecondary = BrandWhite,
    secondaryContainer = SurfaceVariantDarkVal,
    onSecondaryContainer = TextPrimaryDarkVal,

    tertiary = FinGreenEmerald,               // #10B981
    onTertiary = BrandWhite,
    tertiaryContainer = SurfaceVariantDarkVal,
    onTertiaryContainer = TextPrimaryDarkVal,

    background = BackgroundDarkVal,
    onBackground = TextPrimaryDarkVal,

    surface = SurfaceDarkVal,
    onSurface = TextPrimaryDarkVal,
    surfaceVariant = SurfaceVariantDarkVal,
    onSurfaceVariant = TextSecondaryDarkVal,

    error = Color(0xFFF87171),                // Высококонтрастный мягкий красный
    onError = BrandWhite,
    errorContainer = DangerRedSubtleDark,
    onErrorContainer = Color(0xFFF87171),

    outline = OutlineDarkVal,
    outlineVariant = DividerDarkVal
)

@Composable
fun FinPetTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    appTheme: AppTheme = AppTheme.CYBER_BLUE,
    content: @Composable () -> Unit
) {
    // Темная тема (Dark Mode) — строго и исключительно для темы «Черный» (MONOCHROME_MINIMAL)
    val isDark = appTheme == AppTheme.MONOCHROME_MINIMAL

    val dynamicLightScheme = LightColorScheme.copy(
        primary = appTheme.primaryLight,
        secondary = appTheme.gradientEnd,
        tertiary = appTheme.gradientStart,
        background = appTheme.bgLight,
        surface = appTheme.surfaceLight,
        onBackground = when (appTheme) {
            AppTheme.CYBER_BLUE -> Color(0xFF102A43)
            AppTheme.SAKURA_BERRY -> Color(0xFF2D151E)
            AppTheme.MONOCHROME_MINIMAL -> Color(0xFFF0F4F8)
        },
        onSurface = when (appTheme) {
            AppTheme.CYBER_BLUE -> Color(0xFF102A43)
            AppTheme.SAKURA_BERRY -> Color(0xFF2D151E)
            AppTheme.MONOCHROME_MINIMAL -> Color(0xFFF0F4F8)
        },
        onSurfaceVariant = when (appTheme) {
            AppTheme.CYBER_BLUE -> Color(0xFF334E68)
            AppTheme.SAKURA_BERRY -> Color(0xFF5C3342)
            AppTheme.MONOCHROME_MINIMAL -> Color(0xFF94A3B8)
        },
        outline = appTheme.cardOutlineLight,
        outlineVariant = DividerColorVal
    )
    val dynamicDarkScheme = DarkColorScheme.copy(
        primary = appTheme.primaryDark,
        secondary = appTheme.gradientEnd,
        tertiary = appTheme.gradientStart,
        background = appTheme.bgDark,
        surface = appTheme.surfaceDark,
        onBackground = Color(0xFFF0F4F8),
        onSurface = Color(0xFFF0F4F8),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = appTheme.cardOutlineDark,
        outlineVariant = DividerDarkVal
    )

    val colorScheme = if (isDark) dynamicDarkScheme else dynamicLightScheme
    CompositionLocalProvider(
        LocalDarkTheme provides isDark,
        LocalAppTheme provides appTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
