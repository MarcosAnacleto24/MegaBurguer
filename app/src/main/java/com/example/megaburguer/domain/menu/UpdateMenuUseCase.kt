package com.example.megaburguer.domain.menu

import com.example.megaburguer.data.model.Menu
import com.example.megaburguer.data.repository.menu.MenuDataSourceImp
import jakarta.inject.Inject

class UpdateMenuUseCase @Inject constructor(
    private val menuDataSourceImp: MenuDataSourceImp
) {
    suspend operator fun invoke(menu: Menu) {
       return menuDataSourceImp.updateMenu(menu)
    }
}