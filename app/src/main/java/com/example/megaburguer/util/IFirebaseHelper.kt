package com.example.megaburguer.util

import com.google.firebase.auth.FirebaseAuth

interface IFirebaseHelper {
    fun getAuth(): FirebaseAuth
    fun isAuthenticated(): Boolean
    fun getUserId(): String
    suspend fun getUserType(): String?
}