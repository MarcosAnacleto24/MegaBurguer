package com.example.megaburguer.data.repository.extract

import com.example.megaburguer.data.model.OrderItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine

class ExtractDataSourceImp @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) : ExtractDataSource {
    override suspend fun saveExtractList(orderItemList: List<OrderItem>) {
        return suspendCancellableCoroutine { continuation ->

                orderItemList.forEach { orderItem ->
                    firebaseDatabase.reference
                        .child("extracts")
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

    override suspend fun getExtractList(): List<OrderItem> {
        return suspendCancellableCoroutine { continuation ->
            firebaseDatabase.reference
                .child("extracts")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val extractList = mutableListOf<OrderItem>()
                        for (ds in snapshot.children) {
                            val menu = ds.getValue(OrderItem::class.java)
                            menu?.let { extractList.add(it) }

                        }

                        continuation.resumeWith(Result.success(extractList))
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