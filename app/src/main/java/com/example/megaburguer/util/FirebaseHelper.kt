package com.example.megaburguer.util

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

class FirebaseHelper {

    companion object {

        fun getAuth() = FirebaseAuth.getInstance()

        fun isAuthenticated() = getAuth().currentUser != null

        fun getUserId() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        suspend fun getUserType(): String? {

            val userId = getUserId()

            return try {
                // Busca o valor no caminho /users/{userId}/typeUser
                val dataSnapshot = Firebase.database.reference
                    .child("users")
                    .child(userId)
                    .child("typeUser")
                    .get()
                    .await() // Usa .await() para esperar o resultado de forma limpa

                // Retorna o valor como String, ou null se não existir
                dataSnapshot.getValue(String::class.java)
            } catch (e: Exception) {
                null
            }
        }


    }


}