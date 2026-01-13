package com.example.megaburguer.presenter.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.megaburguer.data.enum.TableStatus

class SharedOrderViewModel : ViewModel() {
    private val _tableStatusEvent = MutableLiveData<Pair<String, TableStatus>?>()
    val tableStatusEvent: LiveData<Pair<String, TableStatus>?> get() = _tableStatusEvent

    fun setTableStatus(tableId: String, status: TableStatus) {
        _tableStatusEvent.value = Pair(tableId, status)
    }

    fun consumeEvent() {
        _tableStatusEvent.value = null
    }
}