package com.example.megaburguer.presenter.home.admin.manage_menu.update_manage_menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.domain.menu.UpdateMenuUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class UpdateManageMenuViewModel @Inject constructor(
    private val updateMenuUseCase: UpdateMenuUseCase
) : ViewModel()
{
    fun updateMenu(menu: Menu) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            updateMenuUseCase.invoke(menu)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))
        }
    }
}