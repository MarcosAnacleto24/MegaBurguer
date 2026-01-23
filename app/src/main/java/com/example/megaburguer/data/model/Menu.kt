package com.example.megaburguer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
    val id: String = "",
    val nameItem: String = "",
    val price: Float = 0f,
    val category: String = ""

): Parcelable