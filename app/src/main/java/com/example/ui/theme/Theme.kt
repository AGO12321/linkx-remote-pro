package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LinkXColorScheme = darkColorScheme(
  primary = PrimaryCyan,
  onPrimary = BackgroundDark,
  primaryContainer = AccentTeal,
  onPrimaryContainer = SecondarySky,
  secondary = SecondarySky,
  onSecondary = BackgroundDark,
  tertiary = AccentTeal,
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondary,
  outline = CardBorder
)

@Composable
fun LinkXTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LinkXColorScheme,
    typography = Typography,
    content = content
  )
}

