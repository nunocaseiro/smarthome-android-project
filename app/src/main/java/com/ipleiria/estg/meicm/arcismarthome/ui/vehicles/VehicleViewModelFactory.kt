package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabaseDao

class VehicleViewModelFactory (
    private val dataSource: VehicleDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory
    {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
                return VehicleViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

