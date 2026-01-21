package com.example.megaburguer.presenter.home.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.data.enum.TableStatus
import com.example.megaburguer.domain.tables.GetTablesUseCase
import com.example.megaburguer.domain.tables.ObserveTablesUseCase
import com.example.megaburguer.domain.tables.UpdateTableStatusUseCase
import com.example.megaburguer.domain.users.GetUserUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class HomeStaffViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val observeTablesUseCase: ObserveTablesUseCase,
    private val updateTableStatusUseCase: UpdateTableStatusUseCase
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

    fun observeTables() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            observeTablesUseCase.invoke().collect { tables ->
                emit(StateView.Success(tables))
            }


        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }

    fun getUser(userId: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val users = getUserUseCase.invoke(userId)
            emit(StateView.Success(users))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }

    fun updateTableStatus(tableId: String, newStatus: TableStatus, userId: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            updateTableStatusUseCase.invoke(tableId, newStatus,userId )
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }

}