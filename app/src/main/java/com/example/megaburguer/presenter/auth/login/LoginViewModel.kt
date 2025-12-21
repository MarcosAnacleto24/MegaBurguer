package com.example.megaburguer.presenter.auth.login

import android.provider.Settings.Global.getString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.R
import com.example.megaburguer.domain.auth.LoginUseCase
import com.example.megaburguer.util.StateView
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
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

            val errorMessage = when(ex) {
                is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
                   R.string.account_not_register_or_password_invalid

                else -> R.string.error_generic
            }

            emit(StateView.Error(stringResId = errorMessage))

        }
    }
}