package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.repository.VehiclesRepository

import kotlinx.coroutines.launch
import java.io.IOException

class VehicleViewModel(dataSource: VehicleDatabaseDao, application: Application) :  ViewModel() {

    val vehiclesRepository =  VehiclesRepository(VehicleDatabase.getInstance(application))

    val database = dataSource

    var vehicles = vehiclesRepository.vehicles

    var vehiclesDB = vehiclesRepository.vehiclesDB

    private val _navigateToVehicleDetail = MutableLiveData<Int>()

    val navigateToVehicleDetail: LiveData<Int>
        get() = _navigateToVehicleDetail

    fun onVehicleClicked(id: Int) {
        _navigateToVehicleDetail.postValue(id)
    }

    fun onVehicleDetailNavigated() {
        _navigateToVehicleDetail.value = null
    }

    fun onStart() {

        viewModelScope.launch {
            try {
                 vehiclesRepository.refreshVehicles()
            } catch (networkError: IOException) {
                    Log.d("failed refresh","failed refresh vehicles")
            }
        }
    }





}