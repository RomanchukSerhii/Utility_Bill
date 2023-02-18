package com.example.utilitybill

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val serviceDao = ServiceDatabase.getDatabase(application).serviceDao()

    fun getServices(): LiveData<List<Service>> {
        return serviceDao.getServices()
    }

    fun addService(service: Service) {
        serviceDao.addService(service)
    }

    fun removeService(id: Int) {
        serviceDao.removeService(id)
    }
}