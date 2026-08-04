package dev.javier.fitbithealth.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Tema "Pulse" — instrumento de salud, no app de marketing ──
// Sujeto: un dispositivo de precisión. Un acento (ámbar), negro profundo,
// cifras tabulares. Cada decisión viene del mundo del dato biométrico.
//
// v0.10: Material You dinámico (Android 12+) + base Hermes.
// - Android 12+: el sistema inyecta la paleta del wallpaper (dynamic color).
// - Android <12: fallback azul eléctrico Hermes (#2E6BFF) + lavanda.

val NeoBackground = Color(0xFF0B0B0D)      // negro profundo
val NeoSurface = Color(0xFF131316)         // tarjetas, +1 paso
val NeoSurfaceVariant = Color(0xFF1B1B20)  // superficies elevadas
val NeoSurfaceHigh = Color(0xFF24242B)
val NeoOnBackground = Color(0xFFF4F2EE)    // blanco cálido (no puro)
val NeoOnSurfaceMuted = Color(0xFF8A888F)  // gris instrumento
val NeoOutline = Color(0xFF232327)         // hairline dividers

// Acento de marca: azul eléctrico Hermes (fallback sin dynamic color)
val Accent = Color(0xFF2E6BFF)
val AccentDim = Color(0xFF1E4FD6)
val AccentSoft = Color(0xFF16234A)         // fondo de selección

val Danger = Color(0xFFFF6B5E)             // solo para errores

// Luz (mínimo)
val NeoLightBackground = Color(0xFFF5F4F1)
val NeoLightSurface = Color(0xFFFFFFFF)
val NeoLightSurfaceVariant = Color(0xFFE9E7E2)
val NeoLightOnBackground = Color(0xFF1C1B19)
val NeoLightOutline = Color(0xFFD8D5CE)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF1C1508),
    primaryContainer = AccentSoft,
    onPrimaryContainer = Color(0xFFEAD9B0),
    secondary = NeoOnBackground,
    onSecondary = Color(0xFF1C1B19),
    background = NeoBackground,
    onBackground = NeoOnBackground,
    surface = NeoSurface,
    onSurface = NeoOnBackground,
    surfaceVariant = NeoSurfaceVariant,
    onSurfaceVariant = NeoOnSurfaceMuted,
    outline = NeoOutline,
    error = Danger,
    onError = Color.White,
)

private val LightColors = lightColorScheme(
    primary = AccentDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0E4C8),
    onPrimaryContainer = Color(0xFF3A2F14),
    background = NeoLightBackground,
    onBackground = NeoLightOnBackground,
    surface = NeoLightSurface,
    onSurface = NeoLightOnBackground,
    surfaceVariant = NeoLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF6E6B63),
    outline = NeoLightOutline,
    error = Danger,
    onError = Color.White,
)

// Tipografía: cifras tabulares para datos — el número nunca baila.
private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Light, fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-2).sp, fontFeatureSettings = "tnum"),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Light, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1.5).sp, fontFeatureSettings = "tnum"),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp, fontFeatureSettings = "tnum"),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 23.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 21.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 19.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 17.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 12.sp, letterSpacing = 1.2.sp),
)

val LightThemeColors = LightColors
val DarkThemeColors = DarkColors
val AppFonts = AppTypography

/** Face de datos: lecturas de instrumento en monoespaciado. */
object DataFace {
    val Value = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.3).sp,
    )
    val ValueSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.2).sp,
    )
}

// Casi todo neutro; el ámbar solo donde el dato importa.
object MetricColors {
    val HeartRate = Accent
    val Sleep = Color(0xFF9A988F)
    val HRV = Color(0xFFC9B98A)
    val Spo2 = Color(0xFF9A988F)
    val Steps = Color(0xFF9A988F)
    val Breathing = Color(0xFF9A988F)
}
