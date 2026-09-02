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
import com.google.firebase.functions.FirebaseFunctions
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
    private val functions: FirebaseFunctions,
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
                memberLimit = 2,
                createdAt = now,
                updatedAt = now
            )

            val batch = firestore.batch()
            val householdRef = firestore.collection("households").document(householdId)
            batch.set(householdRef, mapOf(
                "id" to householdId,
                "name" to household.name,
                "ownerUserId" to ownerUserId,
                "memberLimit" to 2,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ))

            val memberRef = householdRef.collection("members").document(ownerUserId)
            val displayName = firebaseAuth.currentUser?.displayName ?: "Owner"
            batch.set(memberRef, mapOf(
                "householdId" to householdId,
                "userId" to ownerUserId,
                "role" to HouseholdRole.OWNER.name,
                "displayName" to displayName,
                "joinedAt" to FieldValue.serverTimestamp(),
                "status" to MemberStatus.ACTIVE.name
            ))

            val userRef = firestore.collection("users").document(ownerUserId)
            batch.set(userRef, mapOf(
                "currentHouseholdId" to householdId,
                "updatedAt" to FieldValue.serverTimestamp()
            ), com.google.firebase.firestore.SetOptions.merge())

            batch.commit().await()

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
            val tokenHash = sha256(token)
            val invitationId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val expiresAt = now + INVITATION_TTL_MS

            firestore.collection("households").document(householdId)
                .collection("invitations").document(invitationId)
                .set(mapOf(
                    "invitationId" to invitationId,
                    "householdId" to householdId,
                    "tokenHash" to tokenHash,
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
            val result = functions
                .getHttpsCallable("joinHouseholdWithToken")
                .call(mapOf("token" to token))
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = result.getData() as? Map<String, Any> ?: emptyMap()
            val householdId = data["householdId"] as String
            val name = data["name"] as String
            val ownerUserId = data["ownerUserId"] as String
            val now = System.currentTimeMillis()

            val household = Household(
                id = householdId,
                name = name,
                ownerUserId = ownerUserId,
                memberLimit = 2,
                createdAt = now,
                updatedAt = now
            )

            householdDao.upsertHousehold(household.toEntity())
            refreshMembersFromFirestore(householdId)
            scopeManager.setActiveHouseholdId(householdId)

            Resource.Success(household)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun revokeInvitation(householdId: String, invitationId: String): Resource<Unit> {
        return try {
            firestore.collection("households").document(householdId)
                .collection("invitations").document(invitationId)
                .update("status", InvitationStatus.REVOKED.name)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun leaveHousehold(householdId: String, userId: String): Resource<Unit> {
        return try {
            functions.getHttpsCallable("leaveHousehold")
                .call(mapOf("householdId" to householdId))
                .await()
            clearLocalHouseholdCache()
            scopeManager.setActiveHouseholdId(null)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun removeMember(householdId: String, memberUserId: String, ownerUserId: String): Resource<Unit> {
        return try {
            functions.getHttpsCallable("removeMember")
                .call(mapOf("householdId" to householdId, "memberUserId" to memberUserId))
                .await()
            refreshMembersFromFirestore(householdId)
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

    private suspend fun refreshMembersFromFirestore(householdId: String) {
        val snapshot = firestore.collection("households").document(householdId)
            .collection("members").whereEqualTo("status", MemberStatus.ACTIVE.name)
            .get().await()
        val members = snapshot.documents.mapNotNull { doc ->
            val userId = doc.getString("userId") ?: return@mapNotNull null
            HouseholdMember(
                householdId = householdId,
                userId = userId,
                role = HouseholdRole.valueOf(doc.getString("role") ?: HouseholdRole.MEMBER.name),
                displayName = doc.getString("displayName") ?: "Member",
                joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis(),
                status = MemberStatus.ACTIVE
            )
        }
        householdDao.clearMembers(householdId)
        householdDao.upsertMembers(members.map { it.toEntity() })
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val INVITATION_TTL_MS = 48L * 60 * 60 * 1000
    }
}
