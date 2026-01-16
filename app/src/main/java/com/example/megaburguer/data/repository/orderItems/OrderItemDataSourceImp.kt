package com.example.megaburguer.data.repository.orderItems

import com.example.megaburguer.data.model.OrderItem
import com.google.firebase.database.FirebaseDatabase
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
                        .child("orders").push().setValue(orderItem)
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
}