package com.example.megaburguer.domain.orderItems

import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.repository.orderItems.OrderItemDataSourceImp
import jakarta.inject.Inject

class GetOrderItemUseCase @Inject constructor(
    private val orderItemDataSourceImp: OrderItemDataSourceImp
) {
    suspend operator fun invoke(idTable: String): List<OrderItem> {
       return orderItemDataSourceImp.getOrderItemList(idTable)
    }
}


