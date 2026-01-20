package com.example.megaburguer.data.repository.auth

import com.example.megaburguer.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
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

    override suspend fun recover(email: String) {
        return suspendCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        continuation.resumeWith(Result.success(Unit))

                    } else {
                        continuation.resumeWith(Result.failure(task.exception!!))
                    }
                }

        }

    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val backendUrl = "https://backend-mega-burguer.onrender.com/deleteUser"

    override suspend fun deleteUser(userId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val json = JSONObject().apply { put("uid", userId) }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (continuation.isActive) {
                    if (response.isSuccessful) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        val errorMsg = response.body?.string() ?: "Erro ao deletar usuário"
                        continuation.resume(Result.failure(Exception(errorMsg)))
                    }
                }
            }
        })
    }
}