package com.example.megaburguer.presenter.home.staff.tableDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.domain.extract.SaveExtractUseCase
import com.example.megaburguer.domain.orderItems.DeleteOrderItemUseCase
import com.example.megaburguer.domain.orderItems.GetOrderItemUseCase
import com.example.megaburguer.domain.users.GetUserUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class TableDetailsViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getOrderItemUseCase: GetOrderItemUseCase,
    private val saveExtractUseCase: SaveExtractUseCase,
    private val deleteOrderItemUseCase: DeleteOrderItemUseCase
) : ViewModel() {

    fun getUser(userId: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val users = getUserUseCase.invoke(userId)
            emit(StateView.Success(users))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }

    fun getOrderList(idTable: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val orderList = getOrderItemUseCase.invoke(idTable)
            emit(StateView.Success(orderList))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }
    }

    fun saveExtractList(orderItemList: List<OrderItem>) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            saveExtractUseCase.invoke(orderItemList)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }

    fun deleteOrderItem(idTable: String) = liveData(Dispatchers.IO) {

        emit(StateView.Loading())

        try {
            deleteOrderItemUseCase.invoke(idTable)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }

}