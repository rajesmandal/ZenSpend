package com.android.zenspend.core.ui.designsystem.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.FabShape
import com.android.zenspend.core.ui.theme.PillShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone

data class ZenNavItem(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
)

/** The raised primary action in the middle of the bar (e.g. "Add transaction"). */
data class ZenDockAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * Classic bottom bar: icon + label per tab, a lime pill behind the selected icon, and the
 * rounded-square [action] button sitting between the two halves of the tabs.
 *
 * [selectedIndex] may be -1 when the current screen isn't one of the tabs.
 */
@Composable
fun ZenNavigationBar(
    selectedIndex: Int,
    items: List<ZenNavItem>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    action: ZenDockAction? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(Dimensions.NavBarHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val split = items.size / 2
            items.forEachIndexed { index, item ->
                if (index == split && action != null) {
                    ZenNavActionSlot(action)
                }
                ZenNavItemView(
                    item = item,
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.ZenNavItemView(
    item: ZenNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val lime = ZenSpendTheme.colors.tone(ZenTone.Lime)
    val label = stringResource(item.labelRes)
    val pill by animateColorAsState(
        targetValue = if (selected) lime.container else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "navPill"
    )
    val iconTint = if (selected) lime.content else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .weight(1f)
            .height(Dimensions.NavBarHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                contentDescription = label
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(Dimensions.NavPillWidth)
                .height(Dimensions.NavPillHeight)
                .clip(PillShape)
                .background(pill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(Dimensions.IconSizeMedium - 2.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun RowScope.ZenNavActionSlot(action: ZenDockAction) {
    val colors = ZenSpendTheme.colors.tone(ZenTone.Leaf)
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(Dimensions.NavFabSize)
                .shadow(
                    Dimensions.NavFabElevation,
                    FabShape,
                    ambientColor = colors.container,
                    spotColor = colors.container
                )
                .clip(FabShape)
                .background(colors.container)
                .clickable(role = Role.Button, onClick = action.onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                tint = colors.content,
                modifier = Modifier.size(Dimensions.IconSizeMedium + 2.dp)
            )
        }
    }
}
