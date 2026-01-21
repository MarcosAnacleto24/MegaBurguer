package com.example.megaburguer.domain.extract

import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.data.repository.extract.ExtractDataSourceImp
import jakarta.inject.Inject

class GetExtractUseCase @Inject constructor(
    private val extractDataSourceImp: ExtractDataSourceImp
) {
    suspend operator fun invoke(): List<OrderItem> {
       return extractDataSourceImp.getExtractList()
    }
}


