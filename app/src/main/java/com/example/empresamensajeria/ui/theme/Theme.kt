package com.example.empresamensajeria.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val EsquemaEnviaYa = lightColorScheme(
    primary = AzulEnviaYa,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulEnviaYa,
    secondary = NaranjaEnviaYa,
    onSecondary = Color.White,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = FondoClaro,
    onSurface = TextoOscuro,
    surfaceVariant = Color.White,
    onSurfaceVariant = TextoOscuro
)

private val EsquemaEnviaYaOscuro = darkColorScheme(
    primary = AzulSuaveOscuro,
    onPrimary = Color(0xFF0A2540),
    primaryContainer = AzulContenedorOscuro,
    onPrimaryContainer = AzulClaro,
    secondary = NaranjaSuave,
    onSecondary = Color(0xFF3A1D00),
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = FondoOscuro,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = TextoClaro
)


@Composable
fun EmpresaMensajeriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaEnviaYaOscuro else EsquemaEnviaYa

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
