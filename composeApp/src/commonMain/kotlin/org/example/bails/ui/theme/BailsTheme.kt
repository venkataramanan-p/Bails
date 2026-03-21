package org.example.bails.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Dark palette ──
private val DarkBackground = Color(0xFF121218)
private val DarkSurface = Color(0xFF1E1E2E)
private val DarkSurfaceVariant = Color(0xFF2A2A3E)
private val AccentCyan = Color(0xFF00E5FF)
private val AccentCyanDark = Color(0xFF008BA3)
private val AccentCyanContainer = Color(0xFF003544)
private val AccentAmber = Color(0xFFFFD740)
private val ErrorRed = Color(0xFFFF5252)
private val TextWhite = Color(0xFFEAEAEA)
private val TextGray = Color(0xFF9E9EAE)
private val OutlineDark = Color(0xFF3A3A4A)
private val OutlineSubtle = Color(0xFF2E2E3E)

private val BailsDarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.Black,
    primaryContainer = AccentCyanContainer,
    onPrimaryContainer = AccentCyan,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    tertiary = Color(0xFF69F0AE),
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = OutlineDark,
    outlineVariant = OutlineSubtle,
    error = ErrorRed,
    onError = Color.White,
)

// ── Light palette ──
private val LightBackground = Color(0xFFF2F2F7)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFE6E6EE)
private val LightPrimary = Color(0xFF00838F)
private val LightPrimaryContainer = Color(0xFFB2EBF2)
private val LightOnPrimaryContainer = Color(0xFF004D56)
private val LightSecondary = Color(0xFFC67C00)
private val LightTertiary = Color(0xFF2E7D32)
private val LightTextPrimary = Color(0xFF1A1A1A)
private val LightTextSecondary = Color(0xFF5A5A6A)
private val LightOutline = Color(0xFFBDBDC7)
private val LightOutlineVariant = Color(0xFFD6D6DE)

private val BailsLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = Color.White,
    tertiary = LightTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFD32F2F),
    onError = Color.White,
)

// ── Semantic ball-type colors ──
data class BailsSemanticColors(
    val wicketBackground: Color,
    val wicketText: Color,
    val wideBackground: Color,
    val wideText: Color,
    val noBallBackground: Color,
    val noBallText: Color,
    val dotBall: Color,
    val tableHeaderBackground: Color,
)

private val DarkBailsColors = BailsSemanticColors(
    wicketBackground = Color(0xFF4A1A1A),
    wicketText = Color(0xFFFF5252),
    wideBackground = Color(0xFF3D2A00),
    wideText = Color(0xFFFF9100),
    noBallBackground = Color(0xFF3D3200),
    noBallText = Color(0xFFFFD740),
    dotBall = Color(0xFF616161),
    tableHeaderBackground = Color(0xFF2A2A3E),
)

private val LightBailsColors = BailsSemanticColors(
    wicketBackground = Color(0xFFFFCDD2),
    wicketText = Color(0xFFC62828),
    wideBackground = Color(0xFFFFE0B2),
    wideText = Color(0xFFE65100),
    noBallBackground = Color(0xFFFFF9C4),
    noBallText = Color(0xFFF57F17),
    dotBall = Color(0xFFBDBDBD),
    tableHeaderBackground = Color(0xFFE6E6EE),
)

val LocalBailsColors = staticCompositionLocalOf { DarkBailsColors }

object BailsColors {
    val current: BailsSemanticColors
        @Composable get() = LocalBailsColors.current
}

@Composable
fun BailsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BailsDarkColorScheme else BailsLightColorScheme
    val bailsColors = if (darkTheme) DarkBailsColors else LightBailsColors

    CompositionLocalProvider(LocalBailsColors provides bailsColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
