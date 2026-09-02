package com.android.zenspend.feature.household.data.mapper

import com.android.zenspend.core.database.entity.HouseholdEntity
import com.android.zenspend.core.database.entity.HouseholdMemberEntity
import com.android.zenspend.feature.household.domain.model.*

fun HouseholdEntity.toDomain() = Household(
    id = id,
    name = name,
    ownerUserId = ownerUserId,
    memberLimit = memberLimit,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Household.toEntity() = HouseholdEntity(
    id = id,
    name = name,
    ownerUserId = ownerUserId,
    memberLimit = memberLimit,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun HouseholdMemberEntity.toDomain() = HouseholdMember(
    householdId = householdId,
    userId = userId,
    role = HouseholdRole.valueOf(role),
    displayName = displayName,
    joinedAt = joinedAt,
    status = MemberStatus.valueOf(status)
)

fun HouseholdMember.toEntity() = HouseholdMemberEntity(
    householdId = householdId,
    userId = userId,
    role = role.name,
    displayName = displayName,
    joinedAt = joinedAt,
    status = status.name
)
