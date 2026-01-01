package com.example.megaburguer.domain.menu

import com.example.megaburguer.data.repository.menu.MenuDataSourceImp
import jakarta.inject.Inject

class DeleteMenuUseCase @Inject constructor(
    private val menuDataSourceImp: MenuDataSourceImp
) {
    suspend operator fun invoke(menuId: String) {
        menuDataSourceImp.deleteMenu(menuId)
    }
}