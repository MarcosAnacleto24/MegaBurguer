package com.example.megaburguer.domain.tables

import com.example.megaburguer.data.model.Table
import com.example.megaburguer.data.repository.tables.TablesDataSourceImp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveTablesUseCase @Inject constructor(
    private val tablesDataSourceImp: TablesDataSourceImp
) {
    operator fun invoke(): Flow<List<Table>> = tablesDataSourceImp.observeTables()

}