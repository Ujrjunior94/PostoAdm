package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PetrolPrimary,
    secondary = AmberSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color(0xFFF1F5F9),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF1F5F9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PetrolPrimary,
    secondary = AmberSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color(0xFF0F172A),
    onSurface = androidx.compose.ui.graphics.Color(0xFF0F172A)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor to enforce our custom gas-station brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
