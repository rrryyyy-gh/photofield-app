package com.photofieldapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFFAFCBAE),
    onSecondary = Color(0xFF2E4B30),
    secondaryContainer = Color(0xFF4A6D4C),
    onSecondaryContainer = Color(0xFFCBE5CD),
    tertiary = Color(0xFFA8CFE8),
    onTertiary = Color(0xFF1D3D5C),
    tertiaryContainer = Color(0xFF3A6D8C),
    onTertiaryContainer = Color(0xFFD0E4F8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF93000A),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C19),
    onBackground = Color(0xFFE3E4E0),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE3E4E0),
    surfaceVariant = Color(0xFF434742),
    onSurfaceVariant = Color(0xFFC3C7C2),
    outline = Color(0xFF8D928C),
    outlineVariant = Color(0xFF434742),
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE3E4E0),
    inverseOnSurface = Color(0xFF2F312E),
    inversePrimary = Color(0xFF2E7D32),
    surfaceTint = Color(0xFF81C784)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF4A6D4C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCBE5CD),
    onSecondaryContainer = Color(0xFF2E4B30),
    tertiary = Color(0xFF3A6D8C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD0E4F8),
    onTertiaryContainer = Color(0xFF1D3D5C),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFCFDFB),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFCFDFB),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFE0E3DD),
    onSurfaceVariant = Color(0xFF434742),
    outline = Color(0xFF737772),
    outlineVariant = Color(0xFFC3C7C2),
    shadow = Color(0xFF000000),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F312E),
    inverseOnSurface = Color(0xFFF1F3EF),
    inversePrimary = Color(0xFF81C784),
    surfaceTint = Color(0xFF2E7D32)
)

@Composable
fun Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}