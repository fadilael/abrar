package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandOrange,
    onPrimary = Color.White,
    primaryContainer = PetrolBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandOrangeLight,
    onSecondary = Color.White,
    tertiary = PetrolBlueLight,
    background = Color(0xFF0D1D23),
    surface = Color(0xFF14272F),
    surfaceVariant = Color(0xFF1B343E),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    outline = Color(0xFF2C4955),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PetrolBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F6),
    onPrimaryContainer = PetrolBlueDark,
    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = BrandOrangeDark,
    tertiary = PetrolBlueLight,
    background = BgMain,
    surface = BgCard,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = TextMain,
    onSurface = TextMain,
    onSurfaceVariant = TextMuted,
    outline = BorderColor,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
