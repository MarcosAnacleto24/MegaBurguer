package com.example.megaburguer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderItem(
    val id: String = "",
    val nameItem: String = "",
    val price: Float = 0f,
    val quantity: Int = 0,
    val observation: String = "",
    var printed: Boolean = false
): Parcelable
