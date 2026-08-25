package com.f1watch.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = F1Red,
    onPrimary = F1TextWhite,
    primaryContainer = F1RedDark,
    onPrimaryContainer = F1TextWhite,
    secondary = F1CardSurface,
    onSecondary = F1TextWhite,
    background = F1Dark,
    onBackground = F1TextWhite,
    surface = F1CardBg,
    onSurface = F1TextWhite,
    surfaceVariant = F1CardSurface,
    onSurfaceVariant = F1TextSecondary,
    outline = F1CardBorder
)

@Composable
fun F1WatchTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = F1Dark.toArgb()
            window.navigationBarColor = F1Dark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
