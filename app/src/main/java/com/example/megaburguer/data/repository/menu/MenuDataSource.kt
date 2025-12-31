package com.example.megaburguer.data.repository.menu

import com.example.megaburguer.data.model.Menu

interface MenuDataSource {

    suspend fun saveMenu(menu: Menu)

    suspend fun getMenus(): List<Menu>

    suspend fun updateMenu(menu: Menu)

    suspend fun deleteMenu(menuId: String)

}