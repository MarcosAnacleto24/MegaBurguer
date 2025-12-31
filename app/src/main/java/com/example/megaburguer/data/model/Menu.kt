package com.example.megaburguer.data.model

import android.os.Parcelable
import com.example.megaburguer.data.enum.MenuCategory
import com.google.firebase.database.FirebaseDatabase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
    var id: String = "",
    val nameItem: String = "",
    val price: Float = 0f,
    val category: MenuCategory,

): Parcelable {

    init {
        this.id = FirebaseDatabase.getInstance().reference.push().key ?: ""
    }
}
