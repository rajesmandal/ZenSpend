package com.android.zenspend.core.ui.designsystem.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.PillShape
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone

data class ZenNavItem(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
)

/** The round primary action at the end of the dock (e.g. "Add transaction"). */
data class ZenDockAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * Floating pill dock. The selected item expands to show its label;
 * [action] renders the round coral "+" button.
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
    val dock = ZenSpendTheme.colors.tone(ZenTone.Ink)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = Dimensions.DockBottomMargin),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.shadow(
                elevation = Dimensions.DockElevation,
                shape = PillShape,
                ambientColor = dock.container.copy(alpha = 0.4f),
                spotColor = dock.container.copy(alpha = 0.4f)
            ),
            shape = PillShape,
            color = dock.container
        ) {
            Row(
                modifier = Modifier.padding(Dimensions.DockPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingXSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    ZenDockItem(
                        item = item,
                        selected = selectedIndex == index,
                        onClick = { onItemSelected(index) }
                    )
                }
                if (action != null) {
                    ZenDockActionButton(action)
                }
            }
        }
    }
}

@Composable
private fun ZenDockItem(
    item: ZenNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val dock = ZenSpendTheme.colors.tone(ZenTone.Ink)
    val active = ZenSpendTheme.colors.tone(ZenTone.Lime)
    val label = stringResource(item.labelRes)

    val background by animateColorAsState(
        targetValue = if (selected) active.container else dock.container,
        animationSpec = tween(durationMillis = 220),
        label = "dockItemBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) active.content else dock.contentMuted,
        animationSpec = tween(durationMillis = 220),
        label = "dockItemFg"
    )

    Row(
        modifier = Modifier
            .height(Dimensions.DockItemSize)
            .clip(PillShape)
            .background(background)
            .animateContentSize(animationSpec = tween(durationMillis = 220))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                contentDescription = label
            }
            .padding(horizontal = Dimensions.SpacingMedium + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(Dimensions.IconSizeMedium - 2.dp)
        )
        if (selected) {
            Box(modifier = Modifier.width(Dimensions.SpacingSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun ZenDockActionButton(action: ZenDockAction) {
    val colors = ZenSpendTheme.colors.tone(ZenTone.Coral)
    Box(
        modifier = Modifier
            .size(Dimensions.DockItemSize)
            .clip(PillShape)
            .background(colors.container)
            .clickable(role = Role.Button, onClick = action.onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
            tint = colors.content,
            modifier = Modifier.size(Dimensions.IconSizeMedium)
        )
    }
}
