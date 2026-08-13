package com.android.zenspend.feature.emi.data.mapper

import com.android.zenspend.core.database.entity.EMIEntity
import com.android.zenspend.feature.emi.domain.model.EMI

fun EMIEntity.toDomain() = EMI(
    id = id,
    name = name,
    walletId = walletId,
    principalAmount = principalAmount,
    emiAmount = emiAmount,
    interestRate = interestRate,
    totalTenure = totalTenure,
    remainingInstallments = remainingInstallments,
    startDate = startDate,
    endDate = endDate,
    dueDay = dueDay,
    status = status
)

fun EMI.toEntity() = EMIEntity(
    id = id,
    name = name,
    walletId = walletId,
    principalAmount = principalAmount,
    emiAmount = emiAmount,
    interestRate = interestRate,
    totalTenure = totalTenure,
    remainingInstallments = remainingInstallments,
    startDate = startDate,
    endDate = endDate,
    dueDay = dueDay,
    status = status
)
