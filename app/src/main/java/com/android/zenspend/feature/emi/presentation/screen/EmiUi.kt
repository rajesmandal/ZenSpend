package com.android.zenspend.feature.emi.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.zenspend.R
import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.core.ui.theme.ZenTone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared look of the EMI screens: how each lender type is named, coloured and drawn. */
@StringRes
internal fun EMILenderType.labelRes(): Int = when (this) {
    EMILenderType.CREDIT_CARD -> R.string.emi_lender_card
    EMILenderType.BANK -> R.string.emi_lender_bank
    EMILenderType.PERSON -> R.string.emi_lender_person
    EMILenderType.OTHER -> R.string.emi_lender_other
}

internal fun EMILenderType.tone(): ZenTone = when (this) {
    EMILenderType.CREDIT_CARD -> ZenTone.Sun
    EMILenderType.BANK -> ZenTone.Sky
    EMILenderType.PERSON -> ZenTone.Lilac
    EMILenderType.OTHER -> ZenTone.Mint
}

internal fun EMILenderType.icon(): ImageVector = when (this) {
    EMILenderType.CREDIT_CARD -> Icons.Default.CreditCard
    EMILenderType.BANK -> Icons.Default.AccountBalance
    EMILenderType.PERSON -> Icons.Default.Person
    EMILenderType.OTHER -> Icons.Default.MoreHoriz
}

/** "05 Nov 2026" in the device language. */
internal fun formatEmiDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
