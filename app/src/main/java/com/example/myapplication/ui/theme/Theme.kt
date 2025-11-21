package com.example.rag.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = BlueDark,
    onPrimaryContainer = BlueTint,

    secondary = Coral400,
    onSecondary = Color.White,
    secondaryContainer = Coral500,
    onSecondaryContainer = CoralLight,

    background = BackgroundDark,
    onBackground = TextOnDarkPrimary,

    surface = SurfaceDark,
    onSurface = TextOnDarkPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextOnDarkSecondary,
    outlineVariant = OutlineVariantDark,

    error = BizEngError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = BlueTint,
    onPrimaryContainer = Blue600,

    secondary = Coral400,
    onSecondary = Color.White,
    secondaryContainer = CoralLight,
    onSecondaryContainer = Coral500,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outlineVariant = OutlineVariantLight,

    error = BizEngError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Prefer dark; can wire to system later
    dynamicColor: Boolean = false, // Keep stable brand colors
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}