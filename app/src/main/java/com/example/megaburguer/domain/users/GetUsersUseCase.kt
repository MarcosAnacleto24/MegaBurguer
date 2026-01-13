package com.example.megaburguer.domain.users

import com.example.megaburguer.data.model.User
import com.example.megaburguer.data.repository.users.UserDataSourceImp
import jakarta.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userDataSourceImp: UserDataSourceImp
) {
    suspend operator fun invoke(): List<User> {
        return userDataSourceImp.getUsers()
    
    }
}