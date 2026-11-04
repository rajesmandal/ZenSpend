package com.android.zenspend.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ZenSpend "Bento" palette — the ONLY place raw hex values live.
 *
 * Rule: UI code never references these directly. It reads colors through
 * `MaterialTheme.colorScheme` or `ZenSpendTheme.colors` (see Theme.kt).
 * To re-skin the whole app, change values here (or swap the mapping in Theme.kt).
 */

// ---- Neutrals (light) ------------------------------------------------------
internal val Cream = Color(0xFFF6F1E7)          // canvas
internal val CreamDeep = Color(0xFFEFE9DC)
internal val CreamDeeper = Color(0xFFE8E1D2)
internal val CreamDeepest = Color(0xFFE0D8C8)
internal val Paper = Color(0xFFFFFFFF)          // card surface
internal val Ink = Color(0xFF141414)            // primary text / dark tiles
internal val InkSoft = Color(0xFF6F6A5E)        // secondary text
internal val Stone = Color(0xFF8A857A)          // outline
internal val Sand = Color(0xFFE4DDCE)           // hairlines

// ---- Neutrals (dark) -------------------------------------------------------
internal val Night = Color(0xFF0E0E0E)
internal val NightRaised = Color(0xFF1A1A1A)
internal val NightRaised2 = Color(0xFF222222)
internal val NightRaised3 = Color(0xFF2A2A2A)
internal val NightRaised4 = Color(0xFF333333)
internal val Bone = Color(0xFFF5F2EA)           // text on dark
internal val BoneSoft = Color(0xFF9D998C)
internal val NightLine = Color(0xFF2E2E2B)

// ---- Accent pastels (shared by light + dark; always carry Ink text) --------
internal val Lime = Color(0xFFCDF45C)
internal val Coral = Color(0xFFFF7A59)
internal val Lilac = Color(0xFFC9B8FF)
internal val Sky = Color(0xFFA9D8FF)
internal val Sun = Color(0xFFFFD166)
internal val Mint = Color(0xFF9AE6B4)

// ---- Status ----------------------------------------------------------------
internal val ErrorRed = Color(0xFFD92D20)
internal val ErrorRedContainer = Color(0xFFFFE1DA)
internal val ErrorRedDark = Color(0xFFFF8A7A)
internal val ErrorRedContainerDark = Color(0xFF5A1D14)

// ---- Semantic financial colors --------------------------------------------
internal val IncomeGreen = Color(0xFF1C9B57)
internal val ExpenseRed = Color(0xFFE5484D)
internal val TransferBlue = Color(0xFF2E7CF6)
internal val CreditCardGold = Color(0xFFD98E04)
internal val NeutralGrey = Color(0xFF8A857A)

internal val IncomeGreenDark = Color(0xFF5BE39B)
internal val ExpenseRedDark = Color(0xFFFF7A7F)
internal val TransferBlueDark = Color(0xFF7DB2FF)
internal val CreditCardGoldDark = Color(0xFFFFC857)

internal val ScrimColor = Color(0x99000000)
