package com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.database.RoomDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDatabaseDao

class SensorViewModelFactory(private val sensorKey: Int, private val dataSourceRoom: RoomDatabaseDao,
                             private val dataSource: SensorDatabaseDao, private val dataSourceFav: FavouriteDatabaseDao, private val application: Application
)  : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            return SensorViewModel(sensorKey, dataSourceRoom, dataSource, dataSourceFav,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}