package com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.repository.FavouritesRepository
import com.ipleiria.estg.meicm.arcismarthome.repository.SensorsRepository
import kotlinx.coroutines.launch
import java.io.IOException

class SensorViewModel(sensorKey: Int = 0,dataSourceRoom: RoomDatabaseDao,dataSource: SensorDatabaseDao,dataSourceFav: FavouriteDatabaseDao,application: Application) : ViewModel() {

    private val sensorsRepository = SensorsRepository(
        SensorDatabase.getInstance(application)
    )

    private val roomDao = dataSourceRoom
    val sensorDao = dataSource
    val favDao = dataSourceFav

    private var sensor: LiveData<SensorDataModel> = MutableLiveData()
    private var atuador: LiveData<SensorDataModel> = MutableLiveData()
    private var room: LiveData<RoomDataModel> = MutableLiveData()

    fun getSensor() = sensor

    fun getActuator() = atuador

    init {

        if (sensorKey != 0) {
            sensor = sensorDao.getSensorWithId(sensorKey)
        }
    }

    fun getActuator(id: Int) {
        atuador = sensorDao.getSensorWithId(id)
    }

    fun onStart() {

        viewModelScope.launch {
            try {
                sensorsRepository.refreshSensors()
            } catch (networkError: IOException) {
                Log.d("failed refresh", "failed refresh notifications")
            }
        }
    }
}