package com.android.zenspend.core.ui.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.ui.theme.CardShape
import com.android.zenspend.core.ui.theme.ChipShape
import com.android.zenspend.core.ui.theme.Dimensions

/** Standard screen header: optional back button, subtitle + title, and action icons. */
@Composable
fun ZenTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    ZenHeader(
        // The gap keeps list chips / fields from touching the header's curved edge.
        modifier = modifier.padding(bottom = Dimensions.SpacingMedium),
        contentPadding = PaddingValues(
            start = if (showBack) Dimensions.SpacingSmall else Dimensions.ScreenPadding,
            end = Dimensions.SpacingSmall,
            top = Dimensions.SpacingXSmall,
            bottom = Dimensions.SpacingMedium
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBack && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = 0.75f)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            actions()
        }
    }
}

@Composable
fun ZenListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .heightIn(min = Dimensions.MinTouchTarget)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Row(
        modifier = rowModifier
            .padding(horizontal = Dimensions.ScreenPadding, vertical = Dimensions.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
            if (icon != null) {
                ZenBadge(
                    label = title,
                    icon = icon,
                    tone = com.android.zenspend.core.ui.theme.ZenTone.forKey(title),
                    size = Dimensions.BadgeSize
                )
                Spacer(modifier = Modifier.width(Dimensions.SpacingLarge))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showChevron && onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
    }
}

@Composable
fun ZenSettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZenListRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun ZenSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
    val border = BorderStroke(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CardElevation)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.SpacingLarge),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CardElevation)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.SpacingLarge),
                content = content
            )
        }
    }
}

@Composable
fun ZenSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(com.android.zenspend.core.ui.theme.PillShape)
            .background(scheme.surfaceContainerLow)
            .border(Dimensions.Hairline, scheme.outlineVariant, com.android.zenspend.core.ui.theme.PillShape)
            .padding(Dimensions.SpacingXSmall)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(com.android.zenspend.core.ui.theme.PillShape)
                    .background(if (selected) scheme.onSurface else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable(role = Role.Tab) { onSelected(index) }
                    .padding(vertical = Dimensions.SpacingMedium - 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) scheme.surface else scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ZenGoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.ButtonHeight),
        shape = ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
        }
    }
}

enum class AmountType { INCOME, EXPENSE, NEUTRAL, TRANSFER }

@Composable
fun AmountText(
    amount: String,
    type: AmountType = AmountType.NEUTRAL,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = com.android.zenspend.core.ui.theme.AmountMedium
) {
    val color = when (type) {
        AmountType.INCOME -> com.android.zenspend.core.ui.theme.ZenSpendTheme.colors.income
        AmountType.EXPENSE -> com.android.zenspend.core.ui.theme.ZenSpendTheme.colors.expense
        AmountType.TRANSFER -> com.android.zenspend.core.ui.theme.ZenSpendTheme.colors.transfer
        AmountType.NEUTRAL -> MaterialTheme.colorScheme.onSurface
    }
    Text(text = amount, style = style, color = color, modifier = modifier)
}

@Composable
fun ZenStatusBanner(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ChipShape,
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(Dimensions.SpacingMedium, Dimensions.SpacingSmall),
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}

@Composable
fun ZenMemberChip(
    displayName: String,
    role: String,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = if (isCurrentUser) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(Dimensions.SpacingLarge))
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    role.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val ButtonShape = com.android.zenspend.core.ui.theme.ButtonShape
