package com.example.utilitybill.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.utilitybill.database.Service
import com.example.utilitybill.database.ServiceDatabase
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val serviceDao = ServiceDatabase.getDatabase(application).serviceDao()

    private val _isMeterChecked = MutableLiveData<Boolean>()
    val isMeterChecked: LiveData<Boolean> = _isMeterChecked


    fun getServices(): LiveData<List<Service>> {
        return serviceDao.getServices()
    }

    fun getService(serviceId: Int): LiveData<Service> {
        return serviceDao.getService(serviceId)
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
            val maxOrder = serviceDao.getMaxOrder() ?: -1
            val service = Service(
                order = maxOrder + 1,
                name = name,
                tariff = tariff,
                previousValue = previousValue,
                isHasMeter = isHasMeter,
                unit = meterUnit
            )
            serviceDao.addService(service)
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
            serviceDao.updateService(service)
        }
    }

    fun updateServices(services: List<Service>) {
        for (i in services.indices) {
            services[i].order = i
        }
        viewModelScope.launch {
            serviceDao.updateServices(services)
        }
    }

    fun updateUsedStatus(id: Int, isUsed: Boolean) {
        viewModelScope.launch {
            serviceDao.changeUsedStatus(id, isUsed)
        }
    }

    fun updateMeterValue(id: Int, previousValue: Int, currentValue: Int) {
        viewModelScope.launch {
            serviceDao.changeValues(id, previousValue, currentValue)
        }
    }

    fun updateCurrentValue(id: Int, currentValue: Int) {
        viewModelScope.launch {
            serviceDao.changeCurrentValue(id, currentValue)
        }
    }

    fun removeService(id: Int) {
        viewModelScope.launch {
            serviceDao.removeService(id)
        }
    }
}