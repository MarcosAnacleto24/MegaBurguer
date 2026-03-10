package com.example.megaburguer.util

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

object FirebaseHelper : IFirebaseHelper {

    override fun getAuth() = FirebaseAuth.getInstance()

    override fun isAuthenticated() = getAuth().currentUser != null

    override fun getUserId() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override suspend fun getUserType(): String? {

        val userId = getUserId()
        if (userId.isEmpty()) return null

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
        } catch (e: CancellationException) {
            // Relançar a exceção de cancelamento para a Coroutine saber que parou
            throw e
        } catch (e: Exception) {
            // Logar o erro real para não "engolir" problemas técnicos
            android.util.Log.e("FirebaseHelper", "Erro ao buscar tipo de usuário", e)
            null
        }
    }


}