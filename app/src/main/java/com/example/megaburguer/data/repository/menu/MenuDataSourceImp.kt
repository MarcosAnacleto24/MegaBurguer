package com.example.megaburguer.data.repository.menu

import com.example.megaburguer.data.model.Menu
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlin.coroutines.suspendCoroutine

class MenuDataSourceImp @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : MenuDataSource {
    override suspend fun saveMenu(menu: Menu) {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("menu")
                .child(menu.id)
                .setValue(menu)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        val errorMessage = when (val exception = task.exception) {
                            is FirebaseNetworkException -> "Sem conexão com a internet."
                            is com.google.firebase.database.DatabaseException -> "Erro de permissão ou dados inválidos."
                            else -> "Erro ao salvar item: ${exception?.message}" // Fallback
                        }
                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }
                }
        }
    }

    override suspend fun getMenus(): List<Menu> {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("menu")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val menuList = mutableListOf<Menu>()
                        for (ds in snapshot.children) {
                            val menu = ds.getValue(Menu::class.java)
                            menu?.let { menuList.add(it) }

                        }

                        continuation.resumeWith(Result.success(menuList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val errorMessage = when (error.code) {
                            DatabaseError.PERMISSION_DENIED -> "Sem permissão para visualizar o cardápio."
                            DatabaseError.NETWORK_ERROR,
                            DatabaseError.DISCONNECTED -> "Verifique sua conexão com a internet."
                            DatabaseError.EXPIRED_TOKEN -> "Sua sessão expirou. Faça login novamente."
                            else -> "Erro ao carregar cardápio. Tente novamente."
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }
                })
        }
    }

    override suspend fun updateMenu(menu: Menu) {
        return suspendCoroutine { continuation ->

            val menuItem = mapOf(
                "id" to menu.id,
                "nameItem" to menu.nameItem,
                "price" to menu.price,
                "category" to menu.category
            )

            firebaseDatabase.reference
                .child("menu")
                .child(menu.id)
                .updateChildren(menuItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        val errorMessage = when (val exception = task.exception) {
                            is FirebaseNetworkException -> "Sem conexão com a internet."
                            is com.google.firebase.database.DatabaseException -> "Erro de permissão ou dados inválidos."
                            else -> "Erro ao atualizar item: ${exception?.message}" // Fallback
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }
                }
        }
    }

    override suspend fun deleteMenu(menuId: String) {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("menu")
                .child(menuId)
                .removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        val errorMessage = when (val exception = task.exception) {
                            is FirebaseNetworkException -> "Sem conexão com a internet."
                            is com.google.firebase.database.DatabaseException -> "Erro de permissão ou dados inválidos."
                            else -> "Erro ao deletar item: ${exception?.message}" // Fallback
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }
                }
        }
    }
}