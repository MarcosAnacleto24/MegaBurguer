package com.example.megaburguer.data.repository.tables

import com.example.megaburguer.data.model.Table

interface TablesDataSource {

    suspend fun saveTable(table: Table)
}