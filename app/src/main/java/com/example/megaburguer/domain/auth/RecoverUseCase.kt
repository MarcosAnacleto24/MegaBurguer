package com.example.megaburguer.domain.auth

import com.example.megaburguer.data.repository.auth.AuthFirebaseDataSourceImp
import jakarta.inject.Inject

class RecoverUseCase @Inject constructor(
    private val authFirebaseDataSourceImp: AuthFirebaseDataSourceImp
)  {

    suspend operator fun invoke(email: String) {
        return authFirebaseDataSourceImp.recover(email)

    }
}