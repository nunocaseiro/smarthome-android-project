package com.ipleiria.estg.meicm.arcismarthome.ui.house

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.RoomDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDatabaseDao

class HouseViewModelFactory(private val dataSourceRoom: RoomDatabaseDao, private val dataSourceSensor: SensorDatabaseDao,  private val application: Application) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HouseViewModel::class.java)) {
            return HouseViewModel(dataSourceSensor, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}