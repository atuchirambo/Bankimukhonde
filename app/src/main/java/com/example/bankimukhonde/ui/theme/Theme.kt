package com.example.bankimukhonde.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BankimukhondePrimary,
    onPrimary = BankimukhondeOnPrimary,
    primaryContainer = BankimukhondePrimaryContainer,
    onPrimaryContainer = BankimukhondeOnPrimaryContainer,
    secondary = BankimukhondeSecondary,
    onSecondary = BankimukhondeOnSecondary,
    secondaryContainer = BankimukhondeSecondaryContainer,
    onSecondaryContainer = BankimukhondeOnSecondaryContainer,
    background = BankimukhondeBackground,
    onBackground = BankimukhondeOnBackground,
    surface = BankimukhondeSurface,
    onSurface = BankimukhondeOnSurface,
    surfaceVariant = BankimukhondeSurfaceVariant,
    onSurfaceVariant = BankimukhondeOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF88D6A1),
    onPrimary = Color(0xFF003916),
    primaryContainer = Color(0xFF005224),
    onPrimaryContainer = Color(0xFFD3EEDB),
    secondary = Color(0xFFB3CBB9),
    onSecondary = Color(0xFF203527),
    secondaryContainer = Color(0xFF364B3C),
    onSecondaryContainer = Color(0xFFCEE9D5),
    background = Color(0xFF111412),
    onBackground = Color(0xFFE2E3DF),
    surface = Color(0xFF111412),
    onSurface = Color(0xFFE2E3DF),
    surfaceVariant = Color(0xFF424942),
    onSurfaceVariant = Color(0xFFC2C8C2)
)

@Composable
fun BankimukhondeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
