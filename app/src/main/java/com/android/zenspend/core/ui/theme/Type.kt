package com.android.zenspend.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.android.zenspend.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private fun interStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f
) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    displayLarge = interStyle(FontWeight.Bold, 57, 64),
    displayMedium = interStyle(FontWeight.Bold, 45, 52),
    displaySmall = interStyle(FontWeight.Bold, 36, 44),
    headlineLarge = interStyle(FontWeight.Bold, 32, 40),
    headlineMedium = interStyle(FontWeight.SemiBold, 28, 36),
    headlineSmall = interStyle(FontWeight.SemiBold, 24, 32),
    titleLarge = interStyle(FontWeight.SemiBold, 22, 28),
    titleMedium = interStyle(FontWeight.Medium, 16, 24, 0.15f),
    titleSmall = interStyle(FontWeight.Medium, 14, 20, 0.1f),
    bodyLarge = interStyle(FontWeight.Normal, 16, 24, 0.5f),
    bodyMedium = interStyle(FontWeight.Normal, 14, 20, 0.25f),
    bodySmall = interStyle(FontWeight.Normal, 12, 16, 0.4f),
    labelLarge = interStyle(FontWeight.Medium, 14, 20, 0.1f),
    labelMedium = interStyle(FontWeight.Medium, 12, 16, 0.5f),
    labelSmall = interStyle(FontWeight.Medium, 11, 16, 0.5f)
)

val AmountHero = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontFeatureSettings = "tnum"
)

val AmountLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    fontFeatureSettings = "tnum"
)

val AmountMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    fontFeatureSettings = "tnum"
)

val AmountCaption = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
    textAlign = TextAlign.End,
    fontFeatureSettings = "tnum"
)
