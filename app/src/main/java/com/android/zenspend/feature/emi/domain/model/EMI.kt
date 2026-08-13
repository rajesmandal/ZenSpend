package com.android.zenspend.feature.emi.domain.model

import com.android.zenspend.core.database.model.EMIStatus

data class EMI(
    val id: Long = 0,
    val name: String,
    val walletId: Long?,
    val principalAmount: Long,
    val emiAmount: Long,
    val interestRate: Double?,
    val totalTenure: Int,
    val remainingInstallments: Int,
    val startDate: Long,
    val endDate: Long,
    val dueDay: Int,
    val status: EMIStatus = EMIStatus.ACTIVE
)
