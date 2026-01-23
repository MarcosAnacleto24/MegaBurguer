package com.example.megaburguer.presenter.home.admin.manage_menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.domain.menu.DeleteMenuUseCase
import com.example.megaburguer.domain.menu.GetMenuUseCase
import com.example.megaburguer.domain.menu.SaveMenuUseCase
import com.example.megaburguer.util.StateView
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class ManageMenuViewModel @Inject constructor(
    private val saveMenuUseCase: SaveMenuUseCase,
    private val getMenuUseCase: GetMenuUseCase,
    private val deleteMenuUseCase: DeleteMenuUseCase
) : ViewModel() {

    fun saveMenu(menu: Menu) = liveData(Dispatchers.IO) {

        emit(StateView.Loading())

        try {
            saveMenuUseCase.invoke(menu)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }

    }

    fun getMenus() = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            val menu = getMenuUseCase.invoke()
            emit(StateView.Success(menu))

        } catch (ex: Exception) {

            emit(StateView.Error(ex.message.toString()))

        }

    }


    fun deleteMenu(menuId: String) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())

        try {
            deleteMenuUseCase.invoke(menuId)
            emit(StateView.Success(Unit))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message.toString()))

        }
    }
}