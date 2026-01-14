package com.example.megaburguer.domain.tables

import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.repository.tables.TablesDataSourceImp
import jakarta.inject.Inject

class UpdateTableStatusUseCase @Inject constructor(
    private val tablesDataSourceImp: TablesDataSourceImp
) {
    suspend operator fun invoke(tableId: String, newStatus: TableStatus, userId: String) {
        val now = System.currentTimeMillis()
        tablesDataSourceImp.updateTableStatus(tableId, newStatus, now, userId)
    }
}
