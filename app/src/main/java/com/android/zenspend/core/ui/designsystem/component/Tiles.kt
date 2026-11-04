package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import com.android.zenspend.core.ui.theme.BadgeShape
import com.android.zenspend.core.ui.theme.DisplayFontFamily
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.PillShape
import com.android.zenspend.core.ui.theme.TileShape
import com.android.zenspend.core.ui.theme.ToneColors
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone

/** The tone of the tile currently being composed (lets children pick matching colours). */
val LocalToneColors = compositionLocalOf<ToneColors?> { null }

/**
 * Bento tile — the basic building block of every redesigned screen.
 *
 * Pick a [tone] (never a raw colour). Children can use [ZenTileCaption], [ZenArrowBadge],
 * [ZenProgressBar] etc. and they automatically match the tile's tone.
 */
@Composable
fun ZenTile(
    modifier: Modifier = Modifier,
    tone: ZenTone = ZenTone.Surface,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Dimensions.SpacingLarge),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ZenSpendTheme.colors.tone(tone)
    val border = if (tone == ZenTone.Surface) {
        BorderStroke(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant)
    } else null

    val inner: @Composable () -> Unit = {
        CompositionLocalProvider(
            LocalToneColors provides colors,
            LocalContentColor provides colors.content
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = TileShape,
            color = colors.container,
            contentColor = colors.content,
            border = border,
            content = inner
        )
    } else {
        Surface(
            modifier = modifier,
            shape = TileShape,
            color = colors.container,
            contentColor = colors.content,
            border = border,
            content = inner
        )
    }
}

/** Small muted label that sits above a value inside a tile. */
@Composable
fun ZenTileCaption(
    text: String,
    modifier: Modifier = Modifier
) {
    val muted = LocalToneColors.current?.contentMuted ?: MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = muted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Big numeral/value inside a tile. */
@Composable
fun ZenTileValue(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = com.android.zenspend.core.ui.theme.AmountLarge,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = if (color == Color.Unspecified) LocalContentColor.current else color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Round "go" arrow shown in a tile corner. */
@Composable
fun ZenArrowBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    val overlay = LocalToneColors.current?.overlay ?: MaterialTheme.colorScheme.surfaceContainer
    Box(
        modifier = modifier
            .size(Dimensions.ArrowBadgeSize)
            .clip(PillShape)
            .background(overlay),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.IconSizeSmall - 4.dp)
        )
    }
}

/**
 * Square-ish coloured badge with the first letter of [label] (or an [icon]).
 * Used as the leading visual of transactions, wallets and categories.
 */
@Composable
fun ZenBadge(
    label: String,
    modifier: Modifier = Modifier,
    tone: ZenTone = ZenTone.forKey(label),
    icon: ImageVector? = null,
    size: Dp = Dimensions.BadgeSize
) {
    val colors = ZenSpendTheme.colors.tone(tone)
    Box(
        modifier = modifier
            .size(size)
            .clip(BadgeShape)
            .background(colors.container),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(Dimensions.IconSizeMedium)
            )
        } else {
            Text(
                text = label.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = DisplayFontFamily),
                color = colors.content
            )
        }
    }
}

/** Section title with an optional underlined action ("See all"). */
@Composable
fun ZenSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Thick rounded progress bar. Colours default to the surrounding tile's tone. */
@Composable
fun ZenProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    height: Dp = Dimensions.ProgressBarHeight
) {
    val tone = LocalToneColors.current
    val fill = when {
        fillColor != Color.Unspecified -> fillColor
        tone != null -> tone.content
        else -> MaterialTheme.colorScheme.primary
    }
    val track = when {
        trackColor != Color.Unspecified -> trackColor
        tone != null -> tone.track
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "zenProgress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(PillShape)
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(PillShape)
                .background(fill)
        )
    }
}

/** Circular progress ring with optional centre content. */
@Composable
fun ZenRingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = Dimensions.RingSize,
    strokeWidth: Dp = Dimensions.RingStroke,
    progressColor: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    center: @Composable () -> Unit = {}
) {
    val tone = LocalToneColors.current
    val fill = when {
        progressColor != Color.Unspecified -> progressColor
        tone != null -> tone.content
        else -> MaterialTheme.colorScheme.primary
    }
    val track = when {
        trackColor != Color.Unspecified -> trackColor
        tone != null -> tone.track
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "zenRing"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            drawArc(fill, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = stroke)
        }
        center()
    }
}

/** Horizontally scrolling pill filters ("All / Expense / Income"). */
@Composable
fun ZenChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimensions.ScreenPadding)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = spacedBy(Dimensions.SpacingSmall)
    ) {
        options.forEachIndexed { index, label ->
            ZenChip(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelected(index) }
            )
        }
    }
}

@Composable
fun ZenChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val bg = if (selected) scheme.primary else Color.Transparent
    val fg = if (selected) scheme.onPrimary else scheme.onSurface
    Box(
        modifier = modifier
            .clip(PillShape)
            .background(bg)
            .border(
                width = Dimensions.Hairline * 1.5f,
                color = if (selected) bg else scheme.outlineVariant,
                shape = PillShape
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = Dimensions.SpacingLarge, vertical = Dimensions.SpacingMedium - 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
    }
}

/**
 * One row in a transaction / wallet style list.
 * Pass the amount already formatted; colour comes from [amountType].
 */
@Composable
fun ZenTransactionRow(
    title: String,
    subtitle: String,
    amount: String,
    modifier: Modifier = Modifier,
    amountType: AmountType = AmountType.NEUTRAL,
    badgeTone: ZenTone = ZenTone.forKey(title),
    onClick: (() -> Unit)? = null
) {
    val shape = com.android.zenspend.core.ui.theme.RowShape
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerLow)
        .border(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
    Row(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base)
            .padding(horizontal = Dimensions.SpacingMedium + 2.dp, vertical = Dimensions.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ZenBadge(label = title, tone = badgeTone)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimensions.SpacingMedium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        AmountText(
            amount = amount,
            type = amountType,
            style = com.android.zenspend.core.ui.theme.AmountSmall
        )
    }
}

/** Spacer-free helper for a tile's two-line "caption + big value" stack. */
@Composable
fun ZenStat(
    caption: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = com.android.zenspend.core.ui.theme.AmountMedium
) {
    Column(modifier = modifier) {
        ZenTileCaption(caption)
        Spacer4()
        ZenTileValue(value, style = valueStyle)
    }
}

@Composable
private fun Spacer4() = androidx.compose.foundation.layout.Spacer(Modifier.height(Dimensions.SpacingXSmall))
