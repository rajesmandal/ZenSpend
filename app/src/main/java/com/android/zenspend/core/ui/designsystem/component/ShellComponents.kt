package com.android.zenspend.core.ui.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showBack && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ZenNavigationBar(
    selectedIndex: Int,
    items: List<ZenNavItem>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navShape = RoundedCornerShape(Dimensions.NavBarCornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = Dimensions.NavBarFloatingMargin,
                end = Dimensions.NavBarFloatingMargin,
                bottom = Dimensions.SpacingMedium
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.NavBarHeight)
                .shadow(
                    elevation = Dimensions.NavBarElevation,
                    shape = navShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            shape = navShape,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimensions.SpacingXSmall),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    ZenNavBarItem(
                        item = item,
                        selected = selectedIndex == index,
                        onClick = { onItemSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZenNavBarItem(
    item: ZenNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "navIconColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "navLabelColor"
    )
    val pillColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navPillColor"
    )

    Column(
        modifier = modifier
            .heightIn(min = Dimensions.MinTouchTarget)
            .clip(RoundedCornerShape(Dimensions.NavBarItemPillRadius))
            .background(pillColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                contentDescription = item.label
            }
            .padding(vertical = Dimensions.SpacingSmall, horizontal = Dimensions.SpacingXSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(Dimensions.IconSizeMedium)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

data class ZenNavItem(
    val label: String,
    @DrawableRes val iconRes: Int
)

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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.IconSizeMedium)
                    )
                }
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
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = colors,
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
            elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CardElevation)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.SpacingLarge),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
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
