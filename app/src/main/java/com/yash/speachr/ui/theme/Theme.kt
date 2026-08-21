package com.yash.speachr.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Coral80,
    onPrimary = Coral20,
    primaryContainer = Coral30,
    onPrimaryContainer = Coral90,
    secondary = Taupe80,
    onSecondary = Taupe20,
    secondaryContainer = Taupe30,
    onSecondaryContainer = Taupe90,
    tertiary = Gold80,
    onTertiary = Gold20,
    tertiaryContainer = Gold30,
    onTertiaryContainer = Gold90,
    background = Neutral17,
    onBackground = Neutral90,
    surface = Neutral17,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,
    outline = Neutral60,
    outlineVariant = Neutral30,
    error = Error80,
    onError = Error20,
    errorContainer = Error10,
    onErrorContainer = Error90,
    scrim = Color.Black,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral25,
    inversePrimary = Coral40,
    surfaceTint = Coral80
)

private val LightColorScheme = lightColorScheme(
    primary = Coral40,
    onPrimary = Color.White,
    primaryContainer = Coral90,
    onPrimaryContainer = Coral10,
    secondary = Taupe40,
    onSecondary = Color.White,
    secondaryContainer = Taupe90,
    onSecondaryContainer = Taupe10,
    tertiary = Gold40,
    onTertiary = Color.White,
    tertiaryContainer = Gold90,
    onTertiaryContainer = Gold10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Color(0xFFF5DED5),
    onSurfaceVariant = Neutral30,
    outline = Neutral60,
    outlineVariant = Color(0xFFD8C2BA),
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error10,
    scrim = Color.Black,
    inverseSurface = Neutral25,
    inverseOnSurface = Neutral87,
    inversePrimary = Coral80,
    surfaceTint = Coral40
)

// ---------------------------------------------------------------------------------------------
// Glass tokens exposed alongside MaterialTheme so screens can build frosted cards/bars without
// hardcoding alpha values everywhere: AppTheme.glassColors.surface, etc.
// ---------------------------------------------------------------------------------------------

data class GlassColors(
    val surface: Color,
    val surfaceSubtle: Color,
    val border: Color,
    val tint: Color
)

private val LightGlassColors = GlassColors(
    surface = GlassSurfaceLight,
    surfaceSubtle = GlassSurfaceLightSubtle,
    border = GlassBorderLight,
    tint = GlassTintLight
)

private val DarkGlassColors = GlassColors(
    surface = GlassSurfaceDark,
    surfaceSubtle = GlassSurfaceDarkSubtle,
    border = GlassBorderDark,
    tint = GlassTintDark
)

private val LocalGlassColors = staticCompositionLocalOf { LightGlassColors }

object AppTheme {
    val glassColors: GlassColors
        @Composable get() = LocalGlassColors.current
}

@Composable
fun SpeachrTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
    darkTheme: Boolean = false,
    // Off by default: dynamic color pulls from the user's wallpaper and would silently
    // override the brand palette above on Android 12+. Flip to true only if you actively
    // want each user's phone theme to reskin the app.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassColors = if (darkTheme) DarkGlassColors else LightGlassColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? Activity
        activity?.window?.let { window ->
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                !darkTheme
        }
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}