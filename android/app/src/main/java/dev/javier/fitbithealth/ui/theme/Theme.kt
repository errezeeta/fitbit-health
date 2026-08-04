package dev.javier.fitbithealth.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Paleta "Vital" ─────────────────────────────────────────────
// Base: teal oscuro (salud), acentos por métrica

val VitalGreen = Color(0xFF2E7D32)
val VitalGreenDark = Color(0xFF81C784)
val VitalTeal = Color(0xFF00897B)
val VitalTealDark = Color(0xFF4DB6AC)
val VitalCoral = Color(0xFFE57373)
val VitalAmber = Color(0xFFFFB74D)
val VitalIndigo = Color(0xFF5C6BC0)
val VitalPurple = Color(0xFFAB47BC)

val VitalBackground = Color(0xFFF6F8F7)
val VitalSurface = Color(0xFFFFFFFF)
val VitalSurfaceVariant = Color(0xFFEDF1EF)
val VitalOnBackground = Color(0xFF1A1C1B)
val VitalOutline = Color(0xFFD5DAD7)

val VitalBackgroundDark = Color(0xFF101412)
val VitalSurfaceDark = Color(0xFF171C19)
val VitalSurfaceVariantDark = Color(0xFF222824)
val VitalOnBackgroundDark = Color(0xFFE2E7E3)
val VitalOutlineDark = Color(0xFF39403C)

private val LightColors = lightColorScheme(
    primary = VitalTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00332E),
    secondary = VitalIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC5CAE9),
    onSecondaryContainer = Color(0xFF1A237E),
    tertiary = VitalPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE1BEE7),
    onTertiaryContainer = Color(0xFF311B92),
    background = VitalBackground,
    onBackground = VitalOnBackground,
    surface = VitalSurface,
    onSurface = VitalOnBackground,
    surfaceVariant = VitalSurfaceVariant,
    onSurfaceVariant = Color(0xFF4C5450),
    outline = VitalOutline,
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = VitalTealDark,
    onPrimary = Color(0xFF00332E),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFF9FA8DA),
    onSecondary = Color(0xFF1A237E),
    secondaryContainer = Color(0xFF3949AB),
    onSecondaryContainer = Color(0xFFC5CAE9),
    tertiary = Color(0xFFCE93D8),
    onTertiary = Color(0xFF311B92),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFE1BEE7),
    background = VitalBackgroundDark,
    onBackground = VitalOnBackgroundDark,
    surface = VitalSurfaceDark,
    onSurface = VitalOnBackgroundDark,
    surfaceVariant = VitalSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFA7B0AB),
    outline = VitalOutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

// ── Tipografía ─────────────────────────────────────────────────

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp),
)

val LightThemeColors = LightColors
val DarkThemeColors = DarkColors
val AppFonts = AppTypography

// Acentos por métrica (misma paleta en light/dark)
object MetricColors {
    val Sleep = VitalPurple
    val HeartRate = VitalCoral
    val HRV = VitalTeal
    val Spo2 = VitalGreen
    val Steps = VitalAmber
    val Breathing = VitalIndigo
}
