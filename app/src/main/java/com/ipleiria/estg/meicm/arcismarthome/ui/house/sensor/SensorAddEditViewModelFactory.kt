package com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.RoomDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDatabaseDao

class SensorAddEditViewModelFactory (
    private val sensorKey: Int, private val dataSourceRoomsDatabase: RoomDatabaseDao,
    private val dataSource: SensorDatabaseDao, private val application: Application
)  : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorAddEditViewModel::class.java)) {
            return SensorAddEditViewModel(sensorKey, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}