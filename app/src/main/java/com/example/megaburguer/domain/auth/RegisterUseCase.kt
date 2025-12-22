package com.example.megaburguer.domain.auth

import com.example.megaburguer.data.model.User
import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSourceImp
import jakarta.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authFirebaseDataSourceImp: AuthFirebaseDataSourceImp
) {

    suspend operator fun invoke(name: String, email: String, password: String, typeUser: String): User {
        return authFirebaseDataSourceImp.register(name, email, password, typeUser)
    }
}


