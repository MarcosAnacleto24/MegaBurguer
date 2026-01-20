package com.example.megaburguer.data.repository.auth

import com.example.megaburguer.data.model.User

interface AuthFirebaseDataSource {

    suspend fun login(email: String, password: String)

    suspend fun register(name: String, email: String, password: String, typeUser: String): User

    suspend fun recover(email: String)

    suspend fun deleteUser(userId: String): Result<Unit>


}