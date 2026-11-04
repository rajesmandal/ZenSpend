package com.android.zenspend.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * App-specific colours that Material's ColorScheme has no slot for.
 * Read via `ZenSpendTheme.colors`.
 */
@Immutable
data class CustomColors(
    val income: Color = Color.Unspecified,
    val expense: Color = Color.Unspecified,
    val transfer: Color = Color.Unspecified,
    val creditCard: Color = Color.Unspecified,
    val neutral: Color = Color.Unspecified,
    /** Curved screen header: gradient from [headerStart] to [headerEnd], text/icons in [onHeader]. */
    val headerStart: Color = Color.Unspecified,
    val headerEnd: Color = Color.Unspecified,
    val onHeader: Color = Color.Unspecified,
    /** Decorative leaf shapes drawn on the header. */
    val headerLeaf: Color = Color.Unspecified,
    /** Bento tile palette — see [ZenTone]. */
    val tones: ZenTonePalette = lightTonePalette()
) {
    fun tone(tone: ZenTone): ToneColors = tones.of(tone)
}

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Bone,
    primaryContainer = Lime,
    onPrimaryContainer = Ink,
    secondary = InkSoft,
    onSecondary = Paper,
    secondaryContainer = Mint,
    onSecondaryContainer = Ink,
    tertiary = Coral,
    onTertiary = Ink,
    tertiaryContainer = Sky,
    onTertiaryContainer = Ink,
    error = ErrorRed,
    onError = Paper,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = InkSoft,
    outline = Stone,
    outlineVariant = Sand,
    surfaceContainerLowest = Paper,
    surfaceContainerLow = Paper,
    surfaceContainer = CreamDeep,
    surfaceContainerHigh = CreamDeeper,
    surfaceContainerHighest = CreamDeepest,
    inverseSurface = Ink,
    inverseOnSurface = Bone,
    scrim = ScrimColor
)

private val DarkColorScheme = darkColorScheme(
    primary = Leaf,
    onPrimary = Ink,
    primaryContainer = Lime,
    onPrimaryContainer = Ink,
    secondary = BoneSoft,
    onSecondary = Ink,
    secondaryContainer = Mint,
    onSecondaryContainer = Ink,
    tertiary = Coral,
    onTertiary = Ink,
    tertiaryContainer = Sky,
    onTertiaryContainer = Ink,
    error = ErrorRedDark,
    onError = Ink,
    errorContainer = ErrorRedContainerDark,
    onErrorContainer = Bone,
    background = Night,
    onBackground = Bone,
    surface = Night,
    onSurface = Bone,
    surfaceVariant = NightRaised2,
    onSurfaceVariant = BoneSoft,
    outline = BoneSoft,
    outlineVariant = NightLine,
    surfaceContainerLowest = NightRaised,
    surfaceContainerLow = NightRaised,
    surfaceContainer = NightRaised2,
    surfaceContainerHigh = NightRaised3,
    surfaceContainerHighest = NightRaised4,
    inverseSurface = Bone,
    inverseOnSurface = Ink,
    scrim = ScrimColor
)

private val LightCustomColors = CustomColors(
    income = IncomeGreen,
    expense = ExpenseRed,
    transfer = TransferBlue,
    creditCard = CreditCardGold,
    neutral = NeutralGrey,
    headerStart = Forest,
    headerEnd = ForestLight,
    onHeader = Bone,
    headerLeaf = Leaf,
    tones = lightTonePalette()
)

private val DarkCustomColors = CustomColors(
    income = IncomeGreenDark,
    expense = ExpenseRedDark,
    transfer = TransferBlueDark,
    creditCard = CreditCardGoldDark,
    neutral = NeutralGrey,
    headerStart = ForestDeep,
    headerEnd = ForestDeepLight,
    onHeader = Bone,
    headerLeaf = Leaf,
    tones = darkTonePalette()
)

@Composable
fun ZenSpendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object ZenSpendTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}
