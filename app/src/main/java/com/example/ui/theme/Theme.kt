package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealPrimaryContainerDark,
    onPrimaryContainer = TealOnPrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnPrimaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    tertiary = TealTertiaryDark,
    background = TealBackgroundDark,
    surface = TealSurfaceDark,
    surfaceVariant = TealSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TealOnPrimaryLight,
    primaryContainer = TealPrimaryContainerLight,
    onPrimaryContainer = TealOnPrimaryContainerLight,
    secondary = TealSecondaryLight,
    secondaryContainer = TealSecondaryContainerLight,
    tertiary = TealTertiaryLight,
    background = TealBackgroundLight,
    surface = TealSurfaceLight,
    surfaceVariant = TealSurfaceVariantDark
)

// Liquid Glass: fixed dark instrument palette, mono-labeled console identity.
private val GlassColorScheme = darkColorScheme(
    primary = InstrumentAccent,
    onPrimary = Color(0xFF062B1B),
    primaryContainer = Color(0xFF1C5A3D),
    onPrimaryContainer = Color(0xFFB9F2D4),
    secondary = InstrumentForeground,
    onSecondary = InstrumentBackground,
    secondaryContainer = InstrumentSurfaceHigh,
    onSecondaryContainer = InstrumentForeground,
    background = InstrumentBackground,
    onBackground = InstrumentForeground,
    surface = InstrumentSurface,
    onSurface = InstrumentForeground,
    surfaceVariant = InstrumentSurfaceHigh,
    onSurfaceVariant = InstrumentMuted,
    outline = InstrumentBorder,
    outlineVariant = InstrumentBorder
)

// Material 3 Expressive: same teal family, more saturated, larger containers.
private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealPrimaryContainerDark,
    onPrimaryContainer = TealOnPrimaryContainerDark,
    secondary = TealTertiaryDark,
    onSecondary = TealOnPrimaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = TealOnPrimaryContainerDark,
    tertiary = TealSecondaryDark,
    background = TealBackgroundDark,
    onBackground = TealOnPrimaryContainerDark,
    surface = TealSurfaceDark,
    onSurface = TealOnPrimaryContainerDark,
    surfaceVariant = TealSurfaceVariantDark,
    onSurfaceVariant = TealOnPrimaryContainerDark
)

private val ExpressiveLightColorScheme = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TealOnPrimaryLight,
    primaryContainer = TealPrimaryContainerLight,
    onPrimaryContainer = TealOnPrimaryContainerLight,
    secondary = TealTertiaryLight,
    secondaryContainer = TealSecondaryContainerLight,
    tertiary = TealSecondaryLight,
    background = TealBackgroundLight,
    surface = TealSurfaceLight,
    surfaceVariant = TealSurfaceVariantDark
)

private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    appTheme: AppTheme = AppTheme.MATERIAL,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.LIQUID_GLASS -> GlassColorScheme
        AppTheme.M3_EXPRESSIVE ->
            if (darkTheme) ExpressiveDarkColorScheme else ExpressiveLightColorScheme
        AppTheme.MATERIAL ->
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = if (appTheme == AppTheme.M3_EXPRESSIVE) ExpressiveShapes else MaterialTheme.shapes,
        content = content
    )
}
