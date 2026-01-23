package com.example.megaburguer.presenter.auth.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.domain.auth.DeleteUserUseCase
import com.example.megaburguer.domain.users.GetUsersUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class DeleteUserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
): ViewModel() {

    fun getUsers() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val users = getUsersUseCase.invoke()
            emit(StateView.Success(users))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }

    }

    fun deleteUser(uid: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        val result = deleteUserUseCase.invoke(uid)
        if (result.isSuccess) {

            emit(StateView.Success(Unit))

        } else {
            emit(StateView.Error(result.exceptionOrNull()?.message.toString()))
        }
    }


}