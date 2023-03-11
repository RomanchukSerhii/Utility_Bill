package com.example.utilitybill.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.utilitybill.database.model.Service
import com.example.utilitybill.database.AppDatabase
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()

    private val _isMeterChecked = MutableLiveData<Boolean>()
    val isMeterChecked: LiveData<Boolean> = _isMeterChecked

    fun getServices(): LiveData<List<Service>> {
        return appDao.getServices()
    }

    fun getService(serviceId: Int): LiveData<Service> {
        return appDao.getService(serviceId)
    }

    fun switchMeterCheck(isChecked: Boolean) {
        _isMeterChecked.value = isChecked
    }

    fun addService(
        name: String,
        tariff: Double,
        previousValue: Int,
        isHasMeter: Boolean,
        meterUnit: String
    ) {
        viewModelScope.launch {
            val maxOrder = appDao.getMaxOrder() ?: -1
            val service = Service(
                order = maxOrder + 1,
                name = name,
                tariff = tariff,
                previousValue = previousValue,
                isHasMeter = isHasMeter,
                unit = meterUnit
            )
            appDao.addService(service)
        }
    }

    fun updateService(
        serviceId: Int,
        order: Int,
        name: String,
        tariff: Double,
        previousValue: Int,
        currentValue: Int,
        isServiceUsed: Boolean,
        isHasMeter: Boolean,
        meterUnit: String
    ) {
        val service = Service (
            id = serviceId,
            order = order,
            name = name,
            tariff = tariff,
            previousValue = previousValue,
            currentValue = currentValue,
            isUsed = isServiceUsed,
            isHasMeter = isHasMeter,
            unit = meterUnit
        )
        viewModelScope.launch {
            appDao.updateService(service)
        }
    }

    fun updateServices(services: List<Service>) {
        for (i in services.indices) {
            services[i].order = i
        }
        viewModelScope.launch {
            appDao.updateServices(services)
        }
    }

    fun updateUsedStatus(id: Int, isUsed: Boolean) {
        viewModelScope.launch {
            appDao.changeUsedStatus(id, isUsed)
        }
    }

    fun updateMeterValue(id: Int, previousValue: Int, currentValue: Int) {
        viewModelScope.launch {
            appDao.changeValues(id, previousValue, currentValue)
        }
    }

    fun removeService(id: Int) {
        viewModelScope.launch {
            appDao.removeService(id)
        }
    }
}