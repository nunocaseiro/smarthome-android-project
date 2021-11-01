package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles.vehicleDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabaseDao

class VehicleDetailViewModelFactory(  private val vehicleKey: Int,
                                      private val dataSource: VehicleDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleDetailViewModel::class.java)) {
            return VehicleDetailViewModel(vehicleKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}