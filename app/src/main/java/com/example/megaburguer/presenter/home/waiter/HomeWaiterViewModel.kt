package com.example.megaburguer.presenter.home.waiter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.domain.tables.GetTablesUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class HomeWaiterViewModel @Inject constructor(
    private val  getTablesUseCase: GetTablesUseCase
) : ViewModel() {

    fun getTables() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val tables = getTablesUseCase.invoke()
            emit(StateView.Success(tables))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }
}