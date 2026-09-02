package com.android.zenspend.core.database.converter

import androidx.room.TypeConverter
import com.android.zenspend.core.database.model.*

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType) = value.name

    @TypeConverter
    fun toTransactionType(value: String) = TransactionType.valueOf(value)

    @TypeConverter
    fun fromWalletType(value: WalletType) = value.name

    @TypeConverter
    fun toWalletType(value: String) = WalletType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(value: CategoryType) = value.name

    @TypeConverter
    fun toCategoryType(value: String) = CategoryType.valueOf(value)

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus) = value.name

    @TypeConverter
    fun toTransactionStatus(value: String) = TransactionStatus.valueOf(value)

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource) = value.name

    @TypeConverter
    fun toTransactionSource(value: String) = TransactionSource.valueOf(value)

    @TypeConverter
    fun fromEMIStatus(value: EMIStatus) = value.name

    @TypeConverter
    fun toEMIStatus(value: String) = EMIStatus.valueOf(value)

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType) = value.name

    @TypeConverter
    fun toRecurrenceType(value: String) = RecurrenceType.valueOf(value)

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod) = value.name

    @TypeConverter
    fun toBudgetPeriod(value: String) = BudgetPeriod.valueOf(value)

    @TypeConverter
    fun fromSmsDraftStatus(value: com.android.zenspend.feature.sms.data.entity.SmsDraftStatus) = value.name

    @TypeConverter
    fun toSmsDraftStatus(value: String) = com.android.zenspend.feature.sms.data.entity.SmsDraftStatus.valueOf(value)

    @TypeConverter
    fun fromSyncState(value: SyncState) = value.name

    @TypeConverter
    fun toSyncState(value: String) = SyncState.valueOf(value)
}
