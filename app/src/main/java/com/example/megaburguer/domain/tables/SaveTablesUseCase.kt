package com.example.megaburguer.domain.tables

import com.example.megaburguer.data.model.Table
import com.example.megaburguer.data.repository.tables.TablesDataSourceImp
import jakarta.inject.Inject

class SaveTablesUseCase @Inject constructor(
    private val tablesDataSourceImp: TablesDataSourceImp
)  {
    suspend operator fun invoke(table: Table) {
        return tablesDataSourceImp.saveTable(table)
    }

}