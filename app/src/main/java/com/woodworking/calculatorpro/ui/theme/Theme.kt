package com.woodworking.calculatorpro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * App theme. Uses brand-driven colours instead of dynamic Material You so the
 * brand identity stays consistent across devices. Automatically follows the
 * system light/dark setting.
 */
@Composable
fun WoodworkingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkScheme else LightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = colors.background.luminance() > 0.5f
                    isAppearanceLightNavigationBars = colors.background.luminance() > 0.5f
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = WoodTypography,
        shapes = WoodShapes,
        content = content
    )
}

private val LightScheme = lightColorScheme(
    primary            = WoodPrimary,
    onPrimary          = WoodOnPrimary,
    primaryContainer   = Color(0xFFF1E1CE),
    onPrimaryContainer = Color(0xFF3A1B05),

    secondary          = WoodSecondary,
    onSecondary        = WoodOnSecondary,
    secondaryContainer = Color(0xFFF6E1C5),
    onSecondaryContainer = Color(0xFF3A220A),

    tertiary           = WoodTertiary,
    onTertiary         = WoodOnTertiary,
    tertiaryContainer  = Color(0xFFCBDABF),
    onTertiaryContainer = Color(0xFF1B2814),

    background         = SurfaceLight,
    onBackground       = OnBackgroundLight,

    surface            = SurfaceLight,
    onSurface          = OnSurfaceLight,
    surfaceVariant     = SurfaceLightAlt,
    onSurfaceVariant   = OnSurfaceVarLight,
    surfaceTint        = WoodPrimary,
    inverseSurface     = Color(0xFF333333),
    inverseOnSurface   = Color(0xFFF5F1EB),

    outline            = OutlineLight,
    outlineVariant     = Color(0xFFEFE9DF),

    error              = Error,
    onError            = OnError,
)

private val DarkScheme = darkColorScheme(
    primary            = WoodPrimaryDark,
    onPrimary          = WoodOnPrimaryDark,
    primaryContainer   = Color(0xFF5A2E10),
    onPrimaryContainer = Color(0xFFF6DCC0),

    secondary          = WoodSecondaryDark,
    onSecondary        = WoodOnSecondaryDark,
    secondaryContainer = Color(0xFF5A3D1F),
    onSecondaryContainer = Color(0xFFF6DCC0),

    tertiary           = WoodTertiaryDark,
    onTertiary         = WoodOnTertiaryDark,
    tertiaryContainer  = Color(0xFF334B2A),
    onTertiaryContainer = Color(0xFFD7E5CC),

    background         = BackgroundDark,
    onBackground       = OnBackgroundDark,

    surface            = SurfaceDark,
    onSurface          = OnSurfaceDark,
    surfaceVariant     = SurfaceDarkAlt,
    onSurfaceVariant   = OnSurfaceVarDark,
    surfaceTint        = WoodPrimaryDark,
    inverseSurface     = Color(0xFFEDEAE5),
    inverseOnSurface   = Color(0xFF1A1A1A),

    outline            = OutlineDark,
    outlineVariant     = Color(0xFF2E2E2E),

    error              = Color(0xFFF2B8B5),
    onError            = Color(0xFF601410),
)
