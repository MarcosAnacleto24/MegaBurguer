package com.example.megaburguer.presenter.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.R
import com.example.megaburguer.domain.auth.RegisterUseCase
import com.example.megaburguer.util.StateView
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
): ViewModel() {

    fun register(name: String, email: String, password: String, typeUser: String) =
        liveData(Dispatchers.IO) {

        emit(StateView.Loading())

        try {

            val user = registerUseCase.invoke(name, email, password, typeUser)
            emit(StateView.Success(user))

        } catch (ex: FirebaseAuthException) {

            val errorMessage = when(ex.errorCode) {
                "ERROR_WEAK_PASSWORD" -> R.string.enter_password_stronger
                "ERROR_INVALID_EMAIL" -> R.string.email_invalid
                "ERROR_EMAIL_ALREADY_IN_USE" -> R.string.this_email_used
                else -> R.string.error_register

            }

            emit(StateView.Error(stringResId = errorMessage))


        }
    }


}