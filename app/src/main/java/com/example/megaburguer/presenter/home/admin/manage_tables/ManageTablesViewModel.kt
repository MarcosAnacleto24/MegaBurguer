package com.example.megaburguer.presenter.home.admin.manage_tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.data.model.Table
import com.example.megaburguer.domain.tables.SaveTablesUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class ManageTablesViewModel @Inject constructor(
    private val saveTablesUseCase: SaveTablesUseCase
): ViewModel() {

    fun saveTable(table: Table) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            saveTablesUseCase.invoke(table)
            emit(StateView.Success(Unit))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }
}