package com.example.megaburguer.domain.orderPrint

import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.repository.orderPrint.OrderPrintDataSourceImp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveOrderPrintUseCase @Inject constructor(
    private val orderPrintDataSourceImp: OrderPrintDataSourceImp
) {
    operator fun invoke(): Flow<List<OrderItem>> = orderPrintDataSourceImp.observeOrderPrint()

}