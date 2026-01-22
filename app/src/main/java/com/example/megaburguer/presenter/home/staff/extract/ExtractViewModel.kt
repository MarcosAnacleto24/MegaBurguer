package com.example.megaburguer.presenter.home.staff.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.domain.extract.DeleteExtractUseCase
import com.example.megaburguer.domain.extract.GetExtractUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class ExtractViewModel @Inject constructor(
    private val getExtractUseCase: GetExtractUseCase,
    private val deleteExtractUseCase: DeleteExtractUseCase
) : ViewModel() {


    fun getExtract() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val orderList = getExtractUseCase.invoke()
            emit(StateView.Success(orderList))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }
    }


    fun deleteExtract() = liveData(Dispatchers.IO) {

        emit(StateView.Loading())

        try {
            deleteExtractUseCase.invoke()
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }

}