package com.example.utilitybill.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.utilitybill.database.AppDatabase
import com.example.utilitybill.database.Bill
import kotlinx.coroutines.launch

class BillViewModel(application: Application): AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()

    fun getBills(): LiveData<List<Bill>> {
        return appDao.getBills()
    }

    fun getBill(id: Int): LiveData<Bill> {
        return appDao.getBill(id)
    }

    fun addBill(bill: Bill) {
        viewModelScope.launch {
            appDao.addBill(bill)
        }
    }

    fun removeBill(id: Int) {
        viewModelScope.launch {
            appDao.removeBill(id)
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch {
            appDao.updateBill(bill)
        }
    }
}