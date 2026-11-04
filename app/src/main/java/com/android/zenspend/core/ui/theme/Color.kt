package com.android.zenspend.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ZenSpend "Canopy" palette — the ONLY place raw hex values live.
 * Brand colours come from the launcher icon (forest Z, emerald wallet, leaf).
 *
 * Rule: UI code never references these directly. It reads colors through
 * `MaterialTheme.colorScheme` or `ZenSpendTheme.colors` (see Theme.kt).
 * To re-skin the whole app, change values here (or swap the mapping in Theme.kt).
 */

// ---- Brand -----------------------------------------------------------------
internal val Forest = Color(0xFF0B5443)         // primary, hero tile, header start
internal val ForestLight = Color(0xFF168062)    // header gradient end
internal val ForestDeep = Color(0xFF0A3F33)     // header start (dark mode)
internal val ForestDeepLight = Color(0xFF0F5A47) // header end (dark mode)
internal val Emerald = Color(0xFF2E9B50)

// ---- Neutrals (light) ------------------------------------------------------
internal val Cream = Color(0xFFEAF3E6)          // canvas
internal val CreamDeep = Color(0xFFE0EBDA)
internal val CreamDeeper = Color(0xFFD5E3CD)
internal val CreamDeepest = Color(0xFFC9DBC0)
internal val Paper = Color(0xFFFFFFFF)          // card surface
internal val Ink = Color(0xFF0E241C)            // primary text
internal val InkSoft = Color(0xFF587064)        // secondary text
internal val Stone = Color(0xFF7E9488)          // outline
internal val Sand = Color(0xFFD3E2CA)           // hairlines

// ---- Neutrals (dark) -------------------------------------------------------
internal val Night = Color(0xFF06120D)
internal val NightRaised = Color(0xFF0E1F18)
internal val NightRaised2 = Color(0xFF142A21)
internal val NightRaised3 = Color(0xFF1B3529)
internal val NightRaised4 = Color(0xFF244235)
internal val Bone = Color(0xFFF2FAEF)           // text on dark / on header
internal val BoneSoft = Color(0xFF8FA89B)
internal val NightLine = Color(0xFF1E352B)

// ---- Accent tones (shared by light + dark; always carry Ink text) ----------
internal val Lime = Color(0xFFB8E86B)
internal val Coral = Color(0xFFEE7B5A)
internal val Leaf = Color(0xFF7CC84A)
internal val Sky = Color(0xFF8FCBE6)
internal val Sun = Color(0xFFF0C04F)
internal val Mint = Color(0xFFCDEBD3)

// ---- Status ----------------------------------------------------------------
internal val ErrorRed = Color(0xFFD92D20)
internal val ErrorRedContainer = Color(0xFFFFE1DA)
internal val ErrorRedDark = Color(0xFFFF8A7A)
internal val ErrorRedContainerDark = Color(0xFF5A1D14)

// ---- Semantic financial colors --------------------------------------------
internal val IncomeGreen = Color(0xFF1E9A4E)
internal val ExpenseRed = Color(0xFFDC5A3C)
internal val TransferBlue = Color(0xFF2E7CB8)
internal val CreditCardGold = Color(0xFFD98E04)
internal val NeutralGrey = Color(0xFF7E9488)

internal val IncomeGreenDark = Color(0xFF6EDC8F)
internal val ExpenseRedDark = Color(0xFFFF8A6B)
internal val TransferBlueDark = Color(0xFF7DBDF0)
internal val CreditCardGoldDark = Color(0xFFFFC857)

internal val ScrimColor = Color(0x99000000)
