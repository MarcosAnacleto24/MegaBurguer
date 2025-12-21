package com.example.megaburguer.util

import androidx.annotation.StringRes

sealed class StateView<T>(
    val data: T? = null,
    val message: String? = null,
    @param:StringRes val stringResId: Int? = null
) {
    class Loading<T> : StateView<T>(data = null, message = null)

    class Error<T>(message: String? = null, @StringRes stringResId: Int? = null) : StateView<T>(message = message, stringResId = stringResId)

    class Success<T>(data: T, message: String? = null) : StateView<T>(data, message)

}