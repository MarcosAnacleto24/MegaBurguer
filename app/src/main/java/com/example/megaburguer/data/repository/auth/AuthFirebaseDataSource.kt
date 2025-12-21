package com.example.megaburguer.data.repository.auth

interface AuthFirebaseDataSource {

    suspend fun login(email: String, password: String)

}