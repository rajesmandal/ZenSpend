package com.android.zenspend.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(Dimensions.SpacingSmall),
    small = RoundedCornerShape(Dimensions.RadiusSmall),
    medium = RoundedCornerShape(Dimensions.RadiusBadge),
    large = RoundedCornerShape(Dimensions.RadiusCard),
    extraLarge = RoundedCornerShape(Dimensions.RadiusTile)
)

/** Big bento tiles (dashboard hero etc.). */
val TileShape = RoundedCornerShape(Dimensions.RadiusTile)
val CardShape = RoundedCornerShape(Dimensions.RadiusCard)
val RowShape = RoundedCornerShape(Dimensions.RadiusRow)
val BadgeShape = RoundedCornerShape(Dimensions.RadiusBadge)

/** Fully rounded "pill" — buttons, chips, dock. */
val PillShape = CircleShape
val ButtonShape = PillShape
val ChipShape = PillShape
val InputShape = RoundedCornerShape(Dimensions.RadiusInput)

/** Curved bottom edge of the screen header. */
val HeaderShape = RoundedCornerShape(bottomStart = Dimensions.RadiusHeader, bottomEnd = Dimensions.RadiusHeader)

/** Leaf silhouette: three rounded corners and one sharp tip. */
val LeafShape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 0, bottomEndPercent = 50, bottomStartPercent = 50)

/** Rounded-square floating action button. */
val FabShape = RoundedCornerShape(Dimensions.NavFabRadius)
