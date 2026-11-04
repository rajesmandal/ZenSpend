package com.android.zenspend.feature.emi.data.mapper

import com.android.zenspend.core.database.entity.EMIEntity
import com.android.zenspend.core.database.entity.EMIPaymentEntity
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EMIPayment

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
    status = status,
    lenderType = lenderType,
    lenderName = lenderName,
    note = note
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
    status = status,
    lenderType = lenderType,
    lenderName = lenderName,
    note = note
)

fun EMIPaymentEntity.toDomain() = EMIPayment(
    id = id,
    emiId = emiId,
    installmentNumber = installmentNumber,
    amount = amount,
    dueDate = dueDate,
    paidDate = paidDate,
    walletId = walletId,
    transactionId = transactionId
)

fun EMIPayment.toEntity() = EMIPaymentEntity(
    id = id,
    emiId = emiId,
    installmentNumber = installmentNumber,
    amount = amount,
    dueDate = dueDate,
    paidDate = paidDate,
    walletId = walletId,
    transactionId = transactionId
)
