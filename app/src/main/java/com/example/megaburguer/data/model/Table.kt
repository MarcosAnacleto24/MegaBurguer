package com.example.megaburguer.data.model

import android.os.Parcelable
import com.example.megaburguer.data.enum.TableStatus
import com.google.firebase.database.FirebaseDatabase
import kotlinx.parcelize.Parcelize

@Parcelize
data class Table(
    var id: String = "",
    val number: String = "",
    var status: TableStatus = TableStatus.OPEN
): Parcelable
