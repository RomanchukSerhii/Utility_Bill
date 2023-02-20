package com.example.utilitybill.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.utilitybill.database.Service
import com.example.utilitybill.database.ServiceDatabase
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val serviceDao = ServiceDatabase.getDatabase(application).serviceDao()

    fun getServices(): LiveData<List<Service>> {
        return serviceDao.getServices()
    }

    fun addService(name: String, tariff: Double, previousValue: Int) {
        val service = Service(
            name = name,
            tariff = tariff,
            previousValue = previousValue
        )
        viewModelScope.launch {
            serviceDao.addService(service)
        }
    }

    fun updateUsedStatus(id: Int, isUsed: Boolean) {
        viewModelScope.launch {
            serviceDao.changeUsedStatus(id, isUsed)
        }
    }

    fun removeService(id: Int) {
        viewModelScope.launch {
            serviceDao.removeService(id)
        }
    }
}