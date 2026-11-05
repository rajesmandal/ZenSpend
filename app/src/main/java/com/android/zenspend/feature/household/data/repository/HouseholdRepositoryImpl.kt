package com.android.zenspend.feature.household.data.repository

import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.dao.HouseholdDao
import com.android.zenspend.core.household.HouseholdScopeManager
import com.android.zenspend.feature.household.data.mapper.toDomain
import com.android.zenspend.feature.household.data.mapper.toEntity
import com.android.zenspend.feature.household.domain.model.*
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val householdDao: HouseholdDao,
    private val scopeManager: HouseholdScopeManager
) : HouseholdRepository {

    override fun observeActiveHousehold(): Flow<Household?> =
        householdDao.observeActiveHousehold().map { it?.toDomain() }

    override fun observeMembers(householdId: String): Flow<List<HouseholdMember>> =
        householdDao.observeMembers(householdId).map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveHousehold(): Household? {
        val id = scopeManager.activeHouseholdId.value
            ?: return null
        return householdDao.getHouseholdById(id)?.toDomain()
    }

    override suspend fun createHousehold(name: String, ownerUserId: String): Resource<Household> {
        return try {
            val householdId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val household = Household(
                id = householdId,
                name = name.trim(),
                ownerUserId = ownerUserId,
                memberLimit = MEMBER_LIMIT,
                createdAt = now,
                updatedAt = now
            )

            val householdRef = households().document(householdId)
            val displayName = firebaseAuth.currentUser?.displayName ?: "Owner"
            firestore.batch()
                .set(householdRef, mapOf(
                    "id" to householdId,
                    "name" to household.name,
                    "ownerUserId" to ownerUserId,
                    "memberLimit" to MEMBER_LIMIT,
                    "memberCount" to 1,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                .set(householdRef.collection("members").document(ownerUserId), mapOf(
                    "householdId" to householdId,
                    "userId" to ownerUserId,
                    "role" to HouseholdRole.OWNER.name,
                    "displayName" to displayName,
                    "joinedAt" to FieldValue.serverTimestamp(),
                    "status" to MemberStatus.ACTIVE.name
                ))
                .set(
                    firestore.collection("users").document(ownerUserId),
                    mapOf("currentHouseholdId" to householdId, "updatedAt" to FieldValue.serverTimestamp()),
                    SetOptions.merge()
                )
                .commit().await()

            householdDao.upsertHousehold(household.toEntity())
            householdDao.upsertMembers(listOf(
                HouseholdMember(
                    householdId = householdId,
                    userId = ownerUserId,
                    role = HouseholdRole.OWNER,
                    displayName = displayName,
                    joinedAt = now
                ).toEntity()
            ))
            scopeManager.setActiveHouseholdId(householdId)

            Resource.Success(household)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun createInvitation(householdId: String, createdByUserId: String): Resource<Invitation> {
        return try {
            val tokenBytes = ByteArray(32)
            SecureRandom().nextBytes(tokenBytes)
            val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
            // The document id IS the hash of the secret token: only the token holder can read it.
            val invitationId = sha256(token)
            val now = System.currentTimeMillis()
            val expiresAt = now + INVITATION_TTL_MS
            val household = householdDao.getHouseholdById(householdId)

            invitations().document(invitationId).set(mapOf(
                "invitationId" to invitationId,
                "householdId" to householdId,
                "householdName" to household?.name.orEmpty(),
                "ownerUserId" to createdByUserId,
                "createdByUserId" to createdByUserId,
                "expiresAt" to expiresAt,
                "status" to InvitationStatus.PENDING.name,
                "createdAt" to FieldValue.serverTimestamp()
            )).await()

            Resource.Success(
                Invitation(
                    invitationId = invitationId,
                    householdId = householdId,
                    token = token,
                    createdByUserId = createdByUserId,
                    expiresAt = expiresAt,
                    status = InvitationStatus.PENDING,
                    createdAt = now
                )
            )
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun joinHouseholdWithToken(token: String): Resource<Household> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: return Resource.Error(
                AppError.ValidationError.GenericError("Sign in first")
            )
            val invitationRef = invitations().document(sha256(token))
            val invitation = invitationRef.get().await()
            if (!invitation.exists()) {
                return Resource.Error(AppError.ValidationError.GenericError("Invalid or used invitation"))
            }
            if (invitation.getString("status") != InvitationStatus.PENDING.name) {
                return Resource.Error(AppError.ValidationError.GenericError("This invitation was already used or revoked"))
            }
            if ((invitation.getLong("expiresAt") ?: 0L) < System.currentTimeMillis()) {
                return Resource.Error(AppError.ValidationError.GenericError("This invitation has expired"))
            }
            val householdId = invitation.getString("householdId")
                ?: return Resource.Error(AppError.ValidationError.GenericError("Invalid invitation"))
            val householdRef = households().document(householdId)

            firestore.batch()
                .update(invitationRef, mapOf(
                    "status" to InvitationStatus.USED.name,
                    "usedByUserId" to uid,
                    "usedAt" to FieldValue.serverTimestamp()
                ))
                .set(householdRef.collection("members").document(uid), mapOf(
                    "householdId" to householdId,
                    "userId" to uid,
                    "role" to HouseholdRole.MEMBER.name,
                    "displayName" to (firebaseAuth.currentUser?.displayName ?: "Member"),
                    "joinedAt" to FieldValue.serverTimestamp(),
                    "status" to MemberStatus.ACTIVE.name,
                    "invitationId" to invitationRef.id
                ))
                .update(householdRef, mapOf(
                    "memberCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                .set(
                    firestore.collection("users").document(uid),
                    mapOf("currentHouseholdId" to householdId, "updatedAt" to FieldValue.serverTimestamp()),
                    SetOptions.merge()
                )
                .commit().await()

            // Now a member: the household document is readable.
            scopeManager.setActiveHouseholdId(householdId)
            refreshActiveHousehold(householdId)
            val household = householdDao.getHouseholdById(householdId)?.toDomain()
                ?: return Resource.Error(AppError.ValidationError.GenericError("Could not load the household"))
            Resource.Success(household)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun revokeInvitation(householdId: String, invitationId: String): Resource<Unit> {
        return try {
            invitations().document(invitationId)
                .update("status", InvitationStatus.REVOKED.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun leaveHousehold(householdId: String, userId: String): Resource<Unit> {
        return try {
            if (householdDao.getHouseholdById(householdId)?.ownerUserId == userId) {
                return Resource.Error(AppError.ValidationError.GenericError("The owner cannot leave the shared cashbook"))
            }
            val householdRef = households().document(householdId)
            firestore.batch()
                .delete(householdRef.collection("members").document(userId))
                .update(householdRef, mapOf(
                    "memberCount" to FieldValue.increment(-1),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                .set(
                    firestore.collection("users").document(userId),
                    mapOf("currentHouseholdId" to null, "updatedAt" to FieldValue.serverTimestamp()),
                    SetOptions.merge()
                )
                .commit().await()
            clearLocalHouseholdCache()
            scopeManager.setActiveHouseholdId(null)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun removeMember(householdId: String, memberUserId: String, ownerUserId: String): Resource<Unit> {
        return try {
            val householdRef = households().document(householdId)
            firestore.batch()
                .delete(householdRef.collection("members").document(memberUserId))
                .update(householdRef, mapOf(
                    "memberCount" to FieldValue.increment(-1),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                .commit().await()
            refreshActiveHousehold(householdId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun renameHousehold(householdId: String, name: String): Resource<Unit> {
        return try {
            firestore.collection("households").document(householdId)
                .update(mapOf(
                    "name" to name.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )).await()
            householdDao.getHouseholdById(householdId)?.let { existing ->
                householdDao.upsertHousehold(existing.copy(name = name.trim(), updatedAt = System.currentTimeMillis()))
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun clearLocalHouseholdCache() {
        householdDao.clearAllMembers()
        householdDao.clearHouseholds()
    }

    /**
     * Pulls the household and its members from Firestore into the local cache so each phone sees
     * who joined or left. If the household is no longer readable (we were removed), the local
     * shared state is dropped and the app falls back to personal mode.
     */
    override suspend fun refreshActiveHousehold(householdId: String) {
        try {
            val snapshot = households().document(householdId).get().await()
            if (!snapshot.exists()) {
                dropLocalHousehold()
                return
            }
            val now = System.currentTimeMillis()
            householdDao.upsertHousehold(
                Household(
                    id = householdId,
                    name = snapshot.getString("name").orEmpty(),
                    ownerUserId = snapshot.getString("ownerUserId").orEmpty(),
                    memberLimit = (snapshot.getLong("memberLimit") ?: MEMBER_LIMIT.toLong()).toInt(),
                    createdAt = snapshot.getTimestamp("createdAt")?.toDate()?.time ?: now,
                    updatedAt = snapshot.getTimestamp("updatedAt")?.toDate()?.time ?: now
                ).toEntity()
            )
            val members = households().document(householdId).collection("members").get().await()
                .documents.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    HouseholdMember(
                        householdId = householdId,
                        userId = userId,
                        role = HouseholdRole.valueOf(doc.getString("role") ?: HouseholdRole.MEMBER.name),
                        displayName = doc.getString("displayName") ?: "Member",
                        joinedAt = doc.getTimestamp("joinedAt")?.toDate()?.time ?: now,
                        status = MemberStatus.ACTIVE
                    )
                }
            householdDao.clearMembers(householdId)
            householdDao.upsertMembers(members.map { it.toEntity() })
            if (members.none { it.userId == firebaseAuth.currentUser?.uid }) dropLocalHousehold()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) dropLocalHousehold()
            // Any other failure (offline, timeout) keeps the cached state.
        }
    }

    private suspend fun dropLocalHousehold() {
        clearLocalHouseholdCache()
        scopeManager.setActiveHouseholdId(null)
    }

    private fun households() = firestore.collection("households")

    private fun invitations() = firestore.collection("invitations")

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val INVITATION_TTL_MS = 48L * 60 * 60 * 1000
        private const val MEMBER_LIMIT = 2
    }
}
