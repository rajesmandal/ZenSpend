package com.android.zenspend.feature.people.presentation.screen

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatLoanDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
