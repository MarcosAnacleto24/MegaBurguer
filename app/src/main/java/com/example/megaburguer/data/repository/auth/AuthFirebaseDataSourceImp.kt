package com.example.megaburguer.data.repository.auth

import com.example.megaburguer.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import kotlin.coroutines.suspendCoroutine

class AuthFirebaseDataSourceImp @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
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

    override suspend fun register(name: String, email: String, password: String, typeUser: String): User {
        return suspendCoroutine { continuation ->

           firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { taskRegister ->

                    if (taskRegister.isSuccessful) {
                        val userId = taskRegister.result.user?.uid ?: ""
                        val user = User(id = userId, name = name, email = email, typeUser = typeUser)

                        firebaseDatabase.reference
                            .child("users")
                            .child(userId)
                            .setValue(user)
                            .addOnCompleteListener { taskSaveUser ->
                                if (taskSaveUser.isSuccessful) {
                                    continuation.resumeWith(Result.success(user))
                                } else {
                                    continuation.resumeWith(Result.failure(taskSaveUser.exception!!))
                                }
                            }

                    } else {
                        continuation.resumeWith(Result.failure(taskRegister.exception!!))
                    }
                }
        }

    }
}