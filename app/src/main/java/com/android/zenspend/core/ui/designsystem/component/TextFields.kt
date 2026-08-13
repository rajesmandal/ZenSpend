package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.InputShape

@Composable
fun ZenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        shape = InputShape,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { 
            // Basic filtering for numeric input
            if (it.all { char -> char.isDigit() || char == '.' }) {
                onValueChange(it)
            }
        },
        modifier = modifier.fillMaxWidth(),
        textStyle = AmountLarge,
        leadingIcon = { Text(currencySymbol, style = AmountLarge) },
        isError = isError,
        shape = InputShape,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        placeholder = { Text("0.00", style = AmountLarge) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
