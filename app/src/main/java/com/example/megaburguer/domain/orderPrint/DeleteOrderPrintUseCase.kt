package com.example.megaburguer.domain.orderPrint

import com.example.megaburguer.data.repository.orderPrint.OrderPrintDataSourceImp
import javax.inject.Inject

class DeleteOrderPrintUseCase @Inject constructor(
    private val orderPrintDataSourceImp: OrderPrintDataSourceImp
) {
    suspend operator fun invoke(itemIds: List<String>) {
        orderPrintDataSourceImp.deletePrintedItems(itemIds)
    }
}