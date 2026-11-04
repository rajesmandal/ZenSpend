package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.HeaderShape
import com.android.zenspend.core.ui.theme.LeafShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme

/**
 * The curved gradient banner at the top of every screen. Content is drawn in the theme's
 * `onHeader` colour. Pass [overlap] > 0 to reserve room for a card that floats over the bottom
 * edge (see [floatOverHeader]).
 */
@Composable
fun ZenHeader(
    modifier: Modifier = Modifier,
    overlap: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(
        start = Dimensions.ScreenPadding,
        end = Dimensions.ScreenPadding,
        top = Dimensions.SpacingMedium,
        bottom = Dimensions.SpacingXLarge
    ),
    content: @Composable () -> Unit
) {
    val colors = ZenSpendTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeaderShape)
            .background(Brush.linearGradient(listOf(colors.headerStart, colors.headerEnd)))
    ) {
        // Decorative leaves; matchParentSize so they never make the header taller.
        Box(modifier = Modifier.matchParentSize().clipToBounds()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = Dimensions.HeaderLeafLarge / 4, y = -Dimensions.HeaderLeafLarge / 4)
                    .size(Dimensions.HeaderLeafLarge)
                    .rotate(LEAF_ROTATION)
                    .alpha(LEAF_ALPHA_LARGE)
                    .clip(LeafShape)
                    .background(colors.headerLeaf)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -Dimensions.HeaderLeafLarge / 2, y = Dimensions.HeaderLeafSmall / 2)
                    .size(Dimensions.HeaderLeafSmall)
                    .rotate(LEAF_ROTATION)
                    .alpha(LEAF_ALPHA_SMALL)
                    .clip(LeafShape)
                    .background(colors.headerLeaf)
            )
        }
        CompositionLocalProvider(LocalContentColor provides colors.onHeader) {
            Column(
                modifier = Modifier.padding(
                    start = contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    end = contentPadding.calculateRightPadding(LayoutDirection.Ltr),
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + overlap
                )
            ) { content() }
        }
    }
}

/** Pulls this element up over the previous sibling (the header) without leaving a gap below it. */
fun Modifier.floatOverHeader(amount: Dp = Dimensions.HeaderOverlap): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val pull = amount.roundToPx()
        layout(placeable.width, (placeable.height - pull).coerceAtLeast(0)) { placeable.placeRelative(0, -pull) }
    }

/** Lets a child ignore its parent's horizontal content padding so it spans the full width. */
fun Modifier.fullBleed(horizontalPadding: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontalPadding.roundToPx() * 2
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}

private const val LEAF_ROTATION = -20f
private const val LEAF_ALPHA_LARGE = 0.16f
private const val LEAF_ALPHA_SMALL = 0.10f
