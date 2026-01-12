package com.example.megaburguer.presenter.home.waiter.createOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.domain.menu.GetMenuUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val getMenuUseCase: GetMenuUseCase
): ViewModel() {

    fun getMenus() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val menu = getMenuUseCase.invoke()
            emit(StateView.Success(menu))

        } catch (ex: Exception) {

            emit(StateView.Error(ex.message.toString()))

        }

    }
}