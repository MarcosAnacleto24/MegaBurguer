package com.example.megaburguer.data.repository.menu

import com.example.megaburguer.data.model.Menu
import com.google.firebase.database.FirebaseDatabase
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
                        continuation.resumeWith(Result.failure(task.exception!!))
                    }
                }
        }
    }

    override suspend fun getMenus(): List<Menu> {
        TODO("Not yet implemented")
    }

    override suspend fun updateMenu(menu: Menu) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMenu(menuId: String) {
        TODO("Not yet implemented")
    }
}