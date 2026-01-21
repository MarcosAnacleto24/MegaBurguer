package com.example.megaburguer.domain.orderItems

import com.example.megaburguer.data.repository.orderItems.OrderItemDataSourceImp
import jakarta.inject.Inject

class DeleteOrderItemUseCase @Inject constructor(
    private val orderItemDataSourceImp: OrderItemDataSourceImp
) {
    suspend operator fun invoke(tableId: String) {
        orderItemDataSourceImp.deleteOrderItem(tableId)
    }
}