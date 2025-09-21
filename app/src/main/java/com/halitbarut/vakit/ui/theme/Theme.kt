package com.halitbarut.vakit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = ColorPaletteDark.primary,
    onPrimary = ColorPaletteDark.onPrimary,
    primaryContainer = ColorPaletteDark.primaryContainer,
    onPrimaryContainer = ColorPaletteDark.onPrimaryContainer,
    secondary = ColorPaletteDark.secondary,
    onSecondary = ColorPaletteDark.onSecondary,
    secondaryContainer = ColorPaletteDark.secondaryContainer,
    onSecondaryContainer = ColorPaletteDark.onSecondaryContainer,
    tertiary = ColorPaletteDark.tertiary,
    onTertiary = ColorPaletteDark.onTertiary,
    background = ColorPaletteDark.background,
    onBackground = ColorPaletteDark.onBackground,
    surface = ColorPaletteDark.surface,
    onSurface = ColorPaletteDark.onSurface,
    surfaceVariant = ColorPaletteDark.surfaceVariant,
    onSurfaceVariant = ColorPaletteDark.onSurfaceVariant,
    outline = ColorPaletteDark.outline,
    error = DangerRed,
    onError = ColorPaletteDark.onError,
)

private val LightColorScheme = lightColorScheme(
    primary = ColorPaletteLight.primary,
    onPrimary = ColorPaletteLight.onPrimary,
    primaryContainer = ColorPaletteLight.primaryContainer,
    onPrimaryContainer = ColorPaletteLight.onPrimaryContainer,
    secondary = ColorPaletteLight.secondary,
    onSecondary = ColorPaletteLight.onSecondary,
    secondaryContainer = ColorPaletteLight.secondaryContainer,
    onSecondaryContainer = ColorPaletteLight.onSecondaryContainer,
    tertiary = ColorPaletteLight.tertiary,
    onTertiary = ColorPaletteLight.onTertiary,
    background = ColorPaletteLight.background,
    onBackground = ColorPaletteLight.onBackground,
    surface = ColorPaletteLight.surface,
    onSurface = ColorPaletteLight.onSurface,
    surfaceVariant = ColorPaletteLight.surfaceVariant,
    onSurfaceVariant = ColorPaletteLight.onSurfaceVariant,
    outline = ColorPaletteLight.outline,
    error = DangerRed,
    onError = ColorPaletteLight.onError,
)

@Composable
fun VakitTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

private object ColorPaletteLight {
    val primary = MidnightBlue
    val onPrimary = SnowSurface
    val primaryContainer = Color(0xFF133A63)
    val onPrimaryContainer = SnowSurface
    val secondary = AmberAccent
    val onSecondary = Color(0xFF1F1400)
    val secondaryContainer = Color(0xFFFFE2B3)
    val onSecondaryContainer = Color(0xFF281700)
    val tertiary = Emerald
    val onTertiary = SnowSurface
    val background = ParchmentBackground
    val onBackground = CharcoalText
    val surface = SnowSurface
    val onSurface = CharcoalText
    val surfaceVariant = MistSurfaceVariant
    val onSurfaceVariant = DuskGrey
    val outline = SlateOutline
    val onError = SnowSurface
}

private object ColorPaletteDark {
    val primary = Color(0xFF8CB7FF)
    val onPrimary = Color(0xFF002F5C)
    val primaryContainer = Color(0xFF11406D)
    val onPrimaryContainer = SnowSurface
    val secondary = Color(0xFFFFC861)
    val onSecondary = Color(0xFF2C1A00)
    val secondaryContainer = Color(0xFF4E3500)
    val onSecondaryContainer = SnowSurface
    val tertiary = Color(0xFF74DAA3)
    val onTertiary = Color(0xFF003921)
    val background = Color(0xFF10131A)
    val onBackground = SnowSurface
    val surface = Color(0xFF141821)
    val onSurface = SnowSurface
    val surfaceVariant = Color(0xFF2D313A)
    val onSurfaceVariant = Color(0xFFBDC0C6)
    val outline = Color(0xFF7E828A)
    val onError = Color.Black
}
