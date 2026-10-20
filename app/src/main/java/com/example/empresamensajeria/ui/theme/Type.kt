package com.example.empresamensajeria.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.empresamensajeria.R

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private val tipografiaBase = Typography()

val Typography = Typography(
    displayLarge = tipografiaBase.displayLarge.copy(fontFamily = Poppins),
    displayMedium = tipografiaBase.displayMedium.copy(fontFamily = Poppins),
    displaySmall = tipografiaBase.displaySmall.copy(fontFamily = Poppins),

    headlineLarge = tipografiaBase.headlineLarge.copy(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = tipografiaBase.headlineMedium.copy(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = tipografiaBase.headlineSmall.copy(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold
    ),

    titleLarge = tipografiaBase.titleLarge.copy(
        fontFamily = Poppins, fontWeight = FontWeight.SemiBold
    ),
    titleMedium = tipografiaBase.titleMedium.copy(
        fontFamily = Poppins, fontWeight = FontWeight.Medium
    ),
    titleSmall = tipografiaBase.titleSmall.copy(
        fontFamily = Poppins, fontWeight = FontWeight.Medium
    ),

    bodyLarge = tipografiaBase.bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = tipografiaBase.bodyMedium.copy(fontFamily = Poppins),
    bodySmall = tipografiaBase.bodySmall.copy(fontFamily = Poppins),

    labelLarge = tipografiaBase.labelLarge.copy(
        fontFamily = Poppins, fontWeight = FontWeight.Medium
    ),
    labelMedium = tipografiaBase.labelMedium.copy(
        fontFamily = Poppins, fontWeight = FontWeight.Medium
    ),
    labelSmall = tipografiaBase.labelSmall.copy(
        fontFamily = Poppins, fontWeight = FontWeight.Medium
    )
)