package com.example.megaburguer.data.repository.orderPrint

import com.example.megaburguer.data.model.OrderItem
import kotlinx.coroutines.flow.Flow

interface OrderPrintDataSource {

    suspend fun saveOrderPrintList(orderPrintList: List<OrderItem>)

    fun observeOrderPrint(): Flow<List<OrderItem>>

    suspend fun deletePrintedItems(itemIds: List<String>)

}