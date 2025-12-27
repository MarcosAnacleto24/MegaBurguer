package com.example.megaburguer.data.repository.tables

import com.example.megaburguer.data.model.Table
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    override suspend fun getTables(): List<Table> {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("tables")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val table = mutableListOf<Table>()
                        for (ds in snapshot.children) {
                            val tables = ds.getValue(Table::class.java)
                            tables?.let { table.add(it) }

                        }
                        continuation.resumeWith(Result.success(table))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        error.toException().let{
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                })
        }
    }

}