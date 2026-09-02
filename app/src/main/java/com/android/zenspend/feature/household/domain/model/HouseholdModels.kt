package com.android.zenspend.feature.household.domain.model

data class Household(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val memberLimit: Int = 2,
    val createdAt: Long,
    val updatedAt: Long
)

enum class HouseholdRole {
    OWNER,
    MEMBER
}

enum class MemberStatus {
    ACTIVE,
    REMOVED
}

data class HouseholdMember(
    val householdId: String,
    val userId: String,
    val role: HouseholdRole,
    val displayName: String,
    val joinedAt: Long,
    val status: MemberStatus = MemberStatus.ACTIVE
)

enum class InvitationStatus {
    PENDING,
    USED,
    REVOKED,
    EXPIRED
}

data class Invitation(
    val invitationId: String,
    val householdId: String,
    val token: String,
    val createdByUserId: String,
    val expiresAt: Long,
    val status: InvitationStatus,
    val createdAt: Long
)
