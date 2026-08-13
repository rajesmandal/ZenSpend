package com.android.zenspend.feature.creditcard.data.mapper

import com.android.zenspend.core.database.entity.CreditCardEntity
import com.android.zenspend.feature.creditcard.domain.model.CreditCard

fun CreditCardEntity.toDomain() = CreditCard(
    id = id,
    walletId = walletId,
    name = bankName, // Using bankName as name for now
    bankName = bankName,
    lastFourDigits = lastFourDigits,
    creditLimit = creditLimit,
    billingCycleDate = billingCycleDate,
    paymentDueDate = paymentDueDate,
    active = active
)

fun CreditCard.toEntity() = CreditCardEntity(
    id = id,
    walletId = walletId,
    bankName = bankName,
    lastFourDigits = lastFourDigits,
    creditLimit = creditLimit,
    billingCycleDate = billingCycleDate,
    paymentDueDate = paymentDueDate,
    active = active
)
