package com.example.megaburguer.domain.auth

import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSourceImp
import jakarta.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val authFirebaseDataSourceImp: AuthFirebaseDataSourceImp
) {
    suspend operator fun invoke(uid: String): Result<Unit> {
       return authFirebaseDataSourceImp.deleteUser(uid)
    }
}