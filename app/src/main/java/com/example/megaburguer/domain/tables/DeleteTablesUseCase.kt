package com.example.megaburguer.domain.tables

import com.example.megaburguer.data.repository.tables.TablesDataSourceImp
import javax.inject.Inject

class DeleteTablesUseCase @Inject constructor(
    private val tablesDataSourceImp: TablesDataSourceImp
) {
    suspend operator fun invoke(tableId: String) {
        tablesDataSourceImp.deleteTable(tableId)
    }
}