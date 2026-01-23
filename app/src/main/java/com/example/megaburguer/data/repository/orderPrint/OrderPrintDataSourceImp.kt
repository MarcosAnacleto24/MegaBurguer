package com.example.megaburguer.data.repository.orderPrint

import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.model.Table
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

class OrderPrintDataSourceImp @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : OrderPrintDataSource {
    override suspend fun saveOrderPrintList(orderPrintList: List<OrderItem>) {
        return suspendCancellableCoroutine { continuation ->

                orderPrintList.forEach { orderItem ->
                    firebaseDatabase.reference
                        .child("print")
                        .child(orderItem.id).setValue(orderItem)
                        .addOnCompleteListener { task ->
                            if (continuation.isActive) {

                                if (task.isSuccessful) {
                                    continuation.resumeWith(Result.success(Unit))
                                } else {
                                    continuation.resumeWith(Result.failure(task.exception!!))
                                }
                            }
                        }
                }
        }
    }

    override fun observeOrderPrint():  Flow<List<OrderItem>> = callbackFlow {
        val tablesRef = firebaseDatabase.reference.child("print")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val printList = mutableListOf<OrderItem>()
                for (ds in snapshot.children) {
                    val table = ds.getValue(OrderItem::class.java)
                    table?.let { printList.add(it) }

                }

                trySend(printList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        tablesRef.addValueEventListener(listener)
        awaitClose { tablesRef.removeEventListener(listener) }
    }

    override suspend fun deletePrintedItems(itemIds: List<String>) {
        return suspendCancellableCoroutine { continuation ->
            val ref = firebaseDatabase.reference.child("print")

            // Vamos contar quantos foram deletados para retornar sucesso apenas no final
            var itemsProcessed = 0

            if(itemIds.isEmpty()) {
                continuation.resumeWith(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            itemIds.forEach { id ->
                ref.child(id).removeValue().addOnCompleteListener {
                    itemsProcessed++
                    if (itemsProcessed == itemIds.size) {
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.success(Unit))
                        }
                    }
                }
            }
        }
    }

}