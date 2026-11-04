package com.android.zenspend.core.auth.data

import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.android.zenspend.core.auth.domain.model.AuthState
import com.android.zenspend.core.auth.domain.model.AuthUser
import com.android.zenspend.core.auth.domain.repository.AuthRepository
import com.android.zenspend.core.common.error.AppError
import com.android.zenspend.core.common.result.Resource
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val credentialManager = CredentialManager.create(context)

    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            val state = if (user != null) {
                AuthState.Authenticated(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString()
                )
            } else {
                AuthState.Unauthenticated
            }
            trySend(state)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        val user = firebaseAuth.currentUser ?: return null
        return AuthUser(
            userId = user.uid,
            displayName = user.displayName,
            email = user.email,
            photoUrl = user.photoUrl?.toString()
        )
    }

    override suspend fun signInWithGoogle(credential: String): Resource<AuthUser> {
        return try {
            val authCredential = GoogleAuthProvider.getCredential(credential, null)
            val result = firebaseAuth.signInWithCredential(authCredential).await()
            val user = result.user ?: return Resource.Error(
                AppError.UnexpectedError(IllegalStateException("Sign-in failed: no user returned"))
            )

            upsertUserProfile(user.uid, user.displayName, user.email, user.photoUrl?.toString())

            Resource.Success(
                AuthUser(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString()
                )
            )
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    override fun getGoogleSignInIntent(): Intent? = null

    /** [activityContext] must be an Activity: Credential Manager shows its account picker on top of it. */
    suspend fun signInWithCredentialManager(activityContext: Context, webClientId: String): Resource<AuthUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            signInWithGoogle(googleIdTokenCredential.idToken)
        } catch (e: Exception) {
            Resource.Error(AppError.UnexpectedError(e))
        }
    }

    private suspend fun upsertUserProfile(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ) {
        val userRef = firestore.collection("users").document(uid)
        val snapshot = userRef.get().await()
        val data = hashMapOf<String, Any?>(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!snapshot.exists()) {
            data["createdAt"] = FieldValue.serverTimestamp()
            data["currentHouseholdId"] = null
        }
        userRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }
}
