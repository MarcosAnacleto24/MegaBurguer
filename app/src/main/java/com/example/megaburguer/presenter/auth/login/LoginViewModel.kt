package com.example.megaburguer.presenter.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.domain.auth.LoginUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {

    fun login(email: String, password: String) = liveData(Dispatchers.IO) {

        emit(StateView.Loading())

        try {
            loginUseCase.invoke(email, password)
            emit(StateView.Success(null))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }
}