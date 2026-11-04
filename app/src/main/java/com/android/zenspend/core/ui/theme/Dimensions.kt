package com.android.zenspend.core.ui.theme

import androidx.compose.ui.unit.dp

/** Every spacing / size / radius used by the UI. Change here, the app follows. */
object Dimensions {
    // ---- Spacing scale ----
    val SpacingXSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 16.dp
    val SpacingXLarge = 24.dp
    val SpacingXXLarge = 32.dp

    // ---- Layout ----
    val ScreenPadding = 20.dp
    val SectionGap = 24.dp
    val TileGap = 12.dp
    val ListItemHeight = 72.dp
    /** Bottom list padding so the last item clears an extended FAB. */
    val FabClearance = 96.dp

    // ---- Radii ----
    val RadiusTile = 28.dp
    val RadiusCard = 24.dp
    val RadiusRow = 22.dp
    val RadiusInput = 20.dp
    val RadiusBadge = 16.dp
    val RadiusSmall = 12.dp

    // ---- Elevation / borders ----
    val CardElevation = 0.dp
    val CardElevationSubtle = 1.dp
    val Hairline = 1.dp

    // ---- Components ----
    val ButtonHeight = 56.dp
    val IconSizeSmall = 20.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp
    val BadgeSize = 44.dp
    val BadgeSizeLarge = 52.dp
    val ArrowBadgeSize = 30.dp
    val ProgressBarHeight = 14.dp
    val RingStroke = 12.dp
    val RingSize = 96.dp

    val MinTouchTarget = 48.dp
    val ChatBubbleMaxWidth = 300.dp

    // ---- Dock (bottom navigation) ----
    val DockItemSize = 48.dp
    val DockPadding = 8.dp
    val DockBottomMargin = 12.dp
    val DockElevation = 16.dp
}
