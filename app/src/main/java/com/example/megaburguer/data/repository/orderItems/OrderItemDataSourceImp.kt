package com.example.megaburguer.data.repository.orderItems

import com.example.megaburguer.data.model.OrderItem
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine

class OrderItemDataSourceImp @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : OrderItemDataSource {
    override suspend fun saveOrderItemList(orderItemList: List<OrderItem>) {
        return suspendCancellableCoroutine { continuation ->

                orderItemList.forEach { orderItem ->
                    firebaseDatabase.reference
                        .child("tables")
                        .child(orderItem.idTable)
                        .child("orders")
                        .child(orderItem.id).setValue(orderItem)
                        .addOnCompleteListener { task ->
                            if (continuation.isActive) {

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
        }
    }

    override suspend fun getOrderItemList(idTable: String): List<OrderItem> {
        return suspendCancellableCoroutine { continuation ->
            firebaseDatabase.reference
                .child("tables")
                .child(idTable)
                .child("orders")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val orderList = mutableListOf<OrderItem>()
                        for (ds in snapshot.children) {
                            val menu = ds.getValue(OrderItem::class.java)
                            menu?.let { orderList.add(it) }

                        }

                        continuation.resumeWith(Result.success(orderList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val errorMessage = when (error.code) {
                            DatabaseError.PERMISSION_DENIED -> "Sem permissão para visualizar o cardápio."
                            DatabaseError.NETWORK_ERROR,
                            DatabaseError.DISCONNECTED -> "Verifique sua conexão com a internet."
                            DatabaseError.EXPIRED_TOKEN -> "Sua sessão expirou. Faça login novamente."
                            else -> "Erro ao carregar itens. Tente novamente."
                        }

                        continuation.resumeWith(Result.failure(Exception(errorMessage)))
                    }

                })


        }
    }

    override suspend fun deleteOrderItem(idTable: String) {
        return suspendCancellableCoroutine { continuation ->
            firebaseDatabase.reference
                .child("tables")
                .child(idTable)
                .child("orders")
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