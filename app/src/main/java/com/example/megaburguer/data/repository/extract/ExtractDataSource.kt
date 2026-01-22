package com.example.megaburguer.data.repository.extract

import com.example.megaburguer.data.model.OrderItem

interface ExtractDataSource {

    suspend fun saveExtractList(orderItemList: List<OrderItem>)

    suspend fun getExtractList(): List<OrderItem>

    suspend fun deleteExtract()


}