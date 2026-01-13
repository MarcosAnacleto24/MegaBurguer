package com.example.megaburguer.presenter.auth.recover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.domain.auth.RecoverUseCase
import com.example.megaburguer.domain.users.GetUsersUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class RecoverViewModel @Inject constructor(
    private val recoverUseCase: RecoverUseCase,
    private val getUsersUseCase: GetUsersUseCase
): ViewModel() {

    fun recover(email: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            recoverUseCase.invoke(email)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }

    fun getUsers() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val users = getUsersUseCase.invoke()
            emit(StateView.Success(users))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }
}