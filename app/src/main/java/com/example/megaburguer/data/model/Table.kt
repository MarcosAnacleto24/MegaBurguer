package com.example.megaburguer.data.model

import android.os.Parcelable
import com.example.megaburguer.data.enum.TableStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class Table(
    val id: String = "",
    val number: String = "",
    var status: TableStatus = TableStatus.OPEN,
    var lastUpdated: Long = 0L,
    var lockedBy: String = ""
): Parcelable
