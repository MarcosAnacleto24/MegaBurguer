package com.example.megaburguer.data.repository.users

import com.example.megaburguer.data.model.User
import com.example.megaburguer.util.FirebaseHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlin.coroutines.suspendCoroutine

class UserDataSourceImp @Inject constructor(
private val firebaseDatabase: FirebaseDatabase
): UserDataSource {
    override suspend fun getUsers(): List<User> {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userList: MutableList<User> = mutableListOf()

                        for (ds in snapshot.children) {
                            val user = ds.getValue(User::class.java)

                            user?.let { userList.add(it) }
                        }

                        continuation.resumeWith(Result.success(
                            userList.apply { removeIf { it.id == FirebaseHelper.getUserId() } }
                        ))

                    }

                    override fun onCancelled(error: DatabaseError) {
                        val errorMessage = when (error.code) {
                            DatabaseError.PERMISSION_DENIED -> "Sem permissão para visualizar o cardápio."
                            DatabaseError.NETWORK_ERROR,
                            DatabaseError.DISCONNECTED -> "Verifique sua conexão com a internet."
                            DatabaseError.EXPIRED_TOKEN -> "Sua sessão expirou. Faça login novamente."
                            else -> "Erro ao carregar usuários. Tente novamente."
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }

                })

        }

    }

    override suspend fun getUser(userId: String): User? {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("users")
                .child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val user = snapshot.getValue(User::class.java)
                        continuation.resumeWith(Result.success(user))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val errorMessage = when (error.code) {
                            DatabaseError.PERMISSION_DENIED -> "Sem permissão para visualizar o cardápio."
                            DatabaseError.NETWORK_ERROR,
                            DatabaseError.DISCONNECTED -> "Verifique sua conexão com a internet."
                            DatabaseError.EXPIRED_TOKEN -> "Sua sessão expirou. Faça login novamente."
                            else -> "Erro ao carregar usuário. Tente novamente."
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }

                })
        }
    }

}