package com.example.megaburguer.data.repository.users

import com.example.megaburguer.data.model.User

interface UserDataSource {

    suspend fun getUsers(): List<User>
}