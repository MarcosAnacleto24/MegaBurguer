package com.example.megaburguer.data.repository.tables

import com.example.megaburguer.data.model.Table
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import kotlin.coroutines.suspendCoroutine

class TablesDataSourceImp @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
):TablesDataSource {
    override suspend fun saveTable(table: Table) {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("tables")
                .child(table.id)
                .setValue(table)
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