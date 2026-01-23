package com.example.megaburguer.domain.orderPrint

import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.repository.orderPrint.OrderPrintDataSourceImp
import jakarta.inject.Inject

class SaveOrderPrintUseCase @Inject constructor(
    private val orderPrintDataSourceImp: OrderPrintDataSourceImp
) {
    suspend operator fun invoke(orderItemList: List<OrderItem>) {
       return orderPrintDataSourceImp.saveOrderPrintList(orderItemList)
    }
}


