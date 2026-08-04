package dev.javier.fitbithealth.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Paleta "Neo" — inspirada en Revolut / fintech premium ─────────
// Fondo casi negro azulado + acentos eléctricos vibrantes.

// Fondos
val NeoBackground = Color(0xFF0B0F19)     // navy casi negro
val NeoSurface = Color(0xFF131A28)        // tarjetas
val NeoSurfaceVariant = Color(0xFF1B2436) // superficies elevadas
val NeoSurfaceHigh = Color(0xFF232E45)    // elevación alta
val NeoOnBackground = Color(0xFFF2F5FA)
val NeoOnSurfaceMuted = Color(0xFF8B96AB) // texto secundario
val NeoOutline = Color(0xFF2A3549)

// Acentos eléctricos
val NeoElectricBlue = Color(0xFF4C8DFF)
val NeoElectricBlueDark = Color(0xFF3D7BE8)
val NeoMint = Color(0xFF00D68F)
val NeoMintDark = Color(0xFF00B87A)
val NeoCoral = Color(0xFFFF5D5D)
val NeoCoralDark = Color(0xFFE84848)
val NeoPink = Color(0xFFFF4FA3)
val NeoAmber = Color(0xFFFFB020)
val NeoPurple = Color(0xFF8B5CF6)
val NeoCyan = Color(0xFF00B8FF)

// Claro (menos usado; la app brilla en oscuro)
val NeoLightBackground = Color(0xFFF5F7FB)
val NeoLightSurface = Color(0xFFFFFFFF)
val NeoLightSurfaceVariant = Color(0xFFE9EDF4)
val NeoLightOnBackground = Color(0xFF0F1420)
val NeoLightOutline = Color(0xFFD8DEE8)

private val DarkColors = darkColorScheme(
    primary = NeoElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A6E),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = NeoMint,
    onSecondary = Color(0xFF003B26),
    secondaryContainer = Color(0xFF0A4A34),
    onSecondaryContainer = Color(0xFFB8F5DC),
    tertiary = NeoPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B2A6B),
    onTertiaryContainer = Color(0xFFE4D8FF),
    background = NeoBackground,
    onBackground = NeoOnBackground,
    surface = NeoSurface,
    onSurface = NeoOnBackground,
    surfaceVariant = NeoSurfaceVariant,
    onSurfaceVariant = NeoOnSurfaceMuted,
    outline = NeoOutline,
    error = NeoCoral,
    onError = Color.White,
    errorContainer = Color(0xFF5C1F28),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColors = lightColorScheme(
    primary = NeoElectricBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF10305E),
    secondary = NeoMintDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8F5DC),
    onSecondaryContainer = Color(0xFF003B26),
    tertiary = NeoPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE4D8FF),
    onTertiaryContainer = Color(0xFF2A1A55),
    background = NeoLightBackground,
    onBackground = NeoLightOnBackground,
    surface = NeoLightSurface,
    onSurface = NeoLightOnBackground,
    surfaceVariant = NeoLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A6478),
    outline = NeoLightOutline,
    error = NeoCoralDark,
    onError = Color.White,
)

// ── Tipografía: sans geométrica, números grandes y bold ─────────
private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1.2).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp),
)

val LightThemeColors = LightColors
val DarkThemeColors = DarkColors
val AppFonts = AppTypography

// Acentos por métrica — coherentes en light/dark
object MetricColors {
    val Sleep = NeoPurple
    val HeartRate = NeoCoral
    val HRV = NeoMint
    val Spo2 = NeoCyan
    val Steps = NeoAmber
    val Breathing = NeoPink
}
