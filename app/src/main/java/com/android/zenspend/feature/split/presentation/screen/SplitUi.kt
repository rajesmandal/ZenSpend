package com.android.zenspend.feature.split.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.zenspend.R
import com.android.zenspend.core.ui.util.formatMinorUnits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun rupees(minor: Long): String = stringResource(R.string.amount_rupees, formatMinorUnits(minor))

internal fun formatSplitDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
