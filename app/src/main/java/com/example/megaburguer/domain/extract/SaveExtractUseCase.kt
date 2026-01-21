package com.example.megaburguer.domain.extract

import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.repository.extract.ExtractDataSourceImp
import jakarta.inject.Inject

class SaveExtractUseCase @Inject constructor(
    private val extractDataSourceImp: ExtractDataSourceImp
) {
    suspend operator fun invoke(orderItemList: List<OrderItem>) {
       return extractDataSourceImp.saveExtractList(orderItemList)
    }
}


