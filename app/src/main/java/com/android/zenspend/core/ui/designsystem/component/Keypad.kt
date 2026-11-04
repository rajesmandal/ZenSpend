package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.android.zenspend.R
import com.android.zenspend.core.ui.theme.AmountMedium
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.RowShape

/** A key on the amount keypad. */
sealed interface KeypadKey {
    data class Digit(val value: Char) : KeypadKey
    data object Dot : KeypadKey
    data object Backspace : KeypadKey
}

private const val MAX_INTEGER_DIGITS = 9
private const val MAX_FRACTION_DIGITS = 2

/**
 * Pure reducer: applies [key] to the amount text typed so far.
 * Kept outside the composable so it is trivially unit-testable.
 */
fun applyKeypadKey(current: String, key: KeypadKey): String = when (key) {
    KeypadKey.Backspace -> current.dropLast(1)

    KeypadKey.Dot -> when {
        current.contains('.') -> current
        current.isEmpty() -> "0."
        else -> "$current."
    }

    is KeypadKey.Digit -> {
        val dotIndex = current.indexOf('.')
        when {
            dotIndex >= 0 && current.length - dotIndex - 1 >= MAX_FRACTION_DIGITS -> current
            dotIndex < 0 && current.length >= MAX_INTEGER_DIGITS -> current
            current == "0" -> key.value.toString()
            else -> current + key.value
        }
    }
}

private val keypadRows: List<List<KeypadKey>> = listOf(
    listOf('1', '2', '3').map { KeypadKey.Digit(it) },
    listOf('4', '5', '6').map { KeypadKey.Digit(it) },
    listOf('7', '8', '9').map { KeypadKey.Digit(it) },
    listOf(KeypadKey.Dot, KeypadKey.Digit('0'), KeypadKey.Backspace)
)

/** 3×4 numeric keypad for entering money amounts. */
@Composable
fun ZenKeypad(
    onKey: (KeypadKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: KeypadKey,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RowShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(Dimensions.Hairline, MaterialTheme.colorScheme.outlineVariant, RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Dimensions.SpacingLarge - 1.dp),
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            is KeypadKey.Digit -> Text(text = key.value.toString(), style = AmountMedium)
            KeypadKey.Dot -> Text(text = ".", style = AmountMedium)
            KeypadKey.Backspace -> Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.keypad_backspace),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
