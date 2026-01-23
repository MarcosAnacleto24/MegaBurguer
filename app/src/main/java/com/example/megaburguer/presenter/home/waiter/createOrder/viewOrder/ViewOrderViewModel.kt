package com.example.megaburguer.presenter.home.waiter.createOrder.viewOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.data.model.OrderItem
import com.example.megaburguer.domain.orderItems.SaveOrderItemUseCase
import com.example.megaburguer.domain.orderPrint.SaveOrderPrintUseCase
import com.example.megaburguer.domain.users.GetUserUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class ViewOrderViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val saveOrderItemUseCase: SaveOrderItemUseCase,
    private val saveOrderPrintUseCase: SaveOrderPrintUseCase
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

    fun saveOrderItemList(orderItemList: List<OrderItem>) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            saveOrderItemUseCase.invoke(orderItemList)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }

    fun saveOrderPrintList(orderItemList: List<OrderItem>) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            saveOrderPrintUseCase.invoke(orderItemList)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }
}