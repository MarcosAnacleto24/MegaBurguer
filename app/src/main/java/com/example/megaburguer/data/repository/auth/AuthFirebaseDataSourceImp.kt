package com.example.megaburguer.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import jakarta.inject.Inject
import kotlin.coroutines.suspendCoroutine

class AuthFirebaseDataSourceImp @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): AuthFirebaseDataSource {
    override suspend fun login(email: String, password: String) {
        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        continuation.resumeWith(Result.failure(task.exception!!))
                    }
                }
        }
    }
}