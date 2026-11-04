package com.android.zenspend.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.android.zenspend.R

// ---- Font families (swap a family here to re-font the entire app) -----------

/** Body / UI text. */
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

/** Display / numerals (variable font; weight axis driven by FontWeight). */
val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

/** Semantic aliases — screens/styles use these, never a concrete family. */
val BodyFontFamily: FontFamily = InterFontFamily
val DisplayFontFamily: FontFamily = SpaceGroteskFontFamily

private fun displayStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f
) = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

private fun bodyStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f
) = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    displayLarge = displayStyle(FontWeight.Bold, 57, 64, -1.5f),
    displayMedium = displayStyle(FontWeight.Bold, 45, 52, -1.2f),
    displaySmall = displayStyle(FontWeight.Bold, 36, 44, -0.8f),
    headlineLarge = displayStyle(FontWeight.Bold, 32, 38, -0.9f),
    headlineMedium = displayStyle(FontWeight.Bold, 28, 34, -0.7f),
    headlineSmall = displayStyle(FontWeight.Bold, 24, 30, -0.5f),
    titleLarge = displayStyle(FontWeight.Bold, 20, 26, -0.4f),
    titleMedium = bodyStyle(FontWeight.SemiBold, 15, 22, 0f),
    titleSmall = bodyStyle(FontWeight.SemiBold, 14, 20, 0f),
    bodyLarge = bodyStyle(FontWeight.Normal, 16, 24, 0f),
    bodyMedium = bodyStyle(FontWeight.Normal, 14, 20, 0f),
    bodySmall = bodyStyle(FontWeight.Normal, 12, 16, 0.1f),
    labelLarge = bodyStyle(FontWeight.SemiBold, 14, 20, 0f),
    labelMedium = bodyStyle(FontWeight.SemiBold, 12, 16, 0.2f),
    labelSmall = bodyStyle(FontWeight.SemiBold, 11, 16, 0.2f)
)

// ---- Money / numeral styles -------------------------------------------------

private fun amountStyle(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: Float) = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    fontFeatureSettings = "tnum"
)

/** Keypad / add-transaction amount. */
val AmountDisplay = amountStyle(FontWeight.Bold, 56, 60, -2.5f)

/** Hero balance on the dashboard. */
val AmountHero = amountStyle(FontWeight.Bold, 44, 48, -1.8f)

val AmountLarge = amountStyle(FontWeight.Bold, 28, 34, -1f)

val AmountMedium = amountStyle(FontWeight.Bold, 18, 24, -0.3f)

val AmountSmall = amountStyle(FontWeight.Bold, 15, 20, -0.2f)

val AmountCaption = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
    textAlign = TextAlign.End,
    fontFeatureSettings = "tnum"
)
