package com.example.megaburguer.data.repository.tables

import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.data.model.Table
import kotlinx.coroutines.flow.Flow

interface TablesDataSource {

    suspend fun saveTable(table: Table)

    suspend fun getTables(): List<Table>

    fun observeTables(): Flow<List<Table>>

    suspend fun deleteTable(tableId: String)

    suspend fun updateTableStatus(tableId: String, newStatus: TableStatus,  lastUpdated: Long, lockedBy: String)

}