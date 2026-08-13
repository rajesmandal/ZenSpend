package com.android.zenspend.feature.sms.data.mapper

import com.android.zenspend.feature.sms.data.entity.SmsDraftEntity
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft

fun SmsDraftEntity.toDomain() = SmsTransactionDraft(
    id = id,
    amount = amount,
    type = type,
    date = date,
    merchant = merchant,
    accountLast4 = accountLast4,
    sender = sender,
    fingerprint = fingerprint,
    status = status
)

fun SmsTransactionDraft.toEntity() = SmsDraftEntity(
    id = id,
    amount = amount,
    type = type,
    date = date,
    merchant = merchant,
    accountLast4 = accountLast4,
    sender = sender,
    fingerprint = fingerprint,
    status = status
)
