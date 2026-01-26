package com.example.megaburguer.data.repository.tables

import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.Table
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                        val errorMessage = when (error.code) {
                            DatabaseError.PERMISSION_DENIED -> "Sem permissão para visualizar o cardápio."
                            DatabaseError.NETWORK_ERROR,
                            DatabaseError.DISCONNECTED -> "Verifique sua conexão com a internet."
                            DatabaseError.EXPIRED_TOKEN -> "Sua sessão expirou. Faça login novamente."
                            else -> "Erro ao carregar mesas. Tente novamente."
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }
                })
        }
    }

    override fun observeTables(): Flow<List<Table>> = callbackFlow {
        val tablesRef = firebaseDatabase.reference.child("tables")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tableList = mutableListOf<Table>()
                for (ds in snapshot.children) {
                    val table = ds.getValue(Table::class.java)
                    table?.let { tableList.add(it) }

                }

                trySend(tableList)
            }

            override fun onCancelled(error: DatabaseError) {
                val errorMessage = when (error.code) {
                    DatabaseError.PERMISSION_DENIED -> "Sem permissão para visualizar o cardápio."
                    DatabaseError.NETWORK_ERROR,
                    DatabaseError.DISCONNECTED -> "Verifique sua conexão com a internet."
                    DatabaseError.EXPIRED_TOKEN -> "Sua sessão expirou. Faça login novamente."
                    else -> "Erro ao carregar mesas. Tente novamente."
                }

                close(Exception(errorMessage))
            }
        }
        tablesRef.addValueEventListener(listener)
        awaitClose { tablesRef.removeEventListener(listener) }
    }

    override suspend fun deleteTable(tableId: String) {
        return suspendCoroutine { continuation ->
            firebaseDatabase.reference
                .child("tables")
                .child(tableId)
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

    override suspend fun updateTableStatus(tableId: String, newStatus: TableStatus,  lastUpdated: Long, lockedBy: String) {
        return suspendCoroutine { continuation ->
            val data = mapOf(
                "status" to newStatus,
                "lastUpdated" to lastUpdated,
                "lockedBy" to lockedBy
            )

            firebaseDatabase.reference
                .child("tables")
                .child(tableId)
                .updateChildren(data)
                .addOnCompleteListener{ task ->
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

}