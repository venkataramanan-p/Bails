package org.example.bails.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark & Modern sports palette
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

/** Semantic colors for ball-type indicators — dark theme optimized */
object BailsColors {
    val wicketBackground = Color(0xFF4A1A1A)
    val wicketText = Color(0xFFFF5252)
    val wideBackground = Color(0xFF3D2A00)
    val wideText = Color(0xFFFF9100)
    val noBallBackground = Color(0xFF3D3200)
    val noBallText = Color(0xFFFFD740)
    val dotBall = Color(0xFF616161)
    val tableHeaderBackground = Color(0xFF2A2A3E)
}

@Composable
fun BailsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BailsDarkColorScheme,
        content = content,
    )
}
