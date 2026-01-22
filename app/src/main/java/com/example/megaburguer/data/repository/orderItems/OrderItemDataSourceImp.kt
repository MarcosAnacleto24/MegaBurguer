package com.example.megaburguer.data.repository.orderItems

import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.data.model.OrderItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

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
                                    continuation.resumeWith(Result.failure(task.exception!!))
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
                        error.toException().let{
                            continuation.resumeWith(Result.failure(it))
                        }
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

                        continuation.resumeWith(Result.failure(task.exception!!))
                    }
                }

            }

    }
}