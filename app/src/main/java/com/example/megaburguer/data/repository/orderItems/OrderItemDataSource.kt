package com.example.megaburguer.data.repository.orderItems

import com.example.megaburguer.data.model.OrderItem

interface OrderItemDataSource {

    suspend fun saveOrderItemList(orderItemList: List<OrderItem>)

}