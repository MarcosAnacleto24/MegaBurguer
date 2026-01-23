package com.example.megaburguer.data.repository.menu

import com.example.megaburguer.data.model.Menu
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
                        continuation.resumeWith(Result.failure(task.exception!!))
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
                        error.toException().let{
                            continuation.resumeWith(Result.failure(it))
                        }
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
                        continuation.resumeWith(Result.failure(task.exception!!))
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
                        continuation.resumeWith(Result.failure(task.exception!!))
                    }
                }
        }
    }
}