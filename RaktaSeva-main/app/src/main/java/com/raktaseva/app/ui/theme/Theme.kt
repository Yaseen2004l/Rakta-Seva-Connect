package com.raktaseva.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Blood         = Color(0xFFE53935)
val BloodDark     = Color(0xFFC0392B)
val BloodLight    = Color(0xFFFF6B6B)
val BgDeep        = Color(0xFF0D0D0F)
val BgCard        = Color(0xFF1A1A2E)
val GlassBorder   = Color(0x33FFFFFF)
val GlassWhite    = Color(0x14FFFFFF)
val GlassWhite2   = Color(0x0AFFFFFF)
val TextPrimary   = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0C0)
val TextMuted     = Color(0xFF666680)
val AccentGreen   = Color(0xFF27AE60)
val AccentOrange  = Color(0xFFE67E22)

private val DarkColorScheme = darkColorScheme(
    primary          = Blood,
    onPrimary        = Color.White,
    primaryContainer = BloodDark,
    secondary        = BloodLight,
    background       = BgDeep,
    surface          = BgCard,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    outline          = GlassBorder,
)

@Composable
fun RaktaSevaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography(), content = content)
}
