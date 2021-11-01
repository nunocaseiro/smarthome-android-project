package com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.repository.RoomsRepository
import com.ipleiria.estg.meicm.arcismarthome.repository.SensorsRepository
import kotlinx.coroutines.launch
import java.io.IOException

class SensorAddEditViewModel(
    sensorKey: Int = 0,
    dataSource: SensorDatabaseDao,
    application: Application
) : ViewModel() {

    val roomsRepository = RoomsRepository(RoomsDatabase.getInstance(application))

    private val sensorsRepository = SensorsRepository(SensorDatabase.getInstance(application))

    val sensorDataSource = dataSource

    private var sensor: LiveData<SensorDataModel> = MutableLiveData()

    private var sensorsOfRoom: LiveData<List<SensorDataModel>> = MutableLiveData()

    fun getSensorsOfRoom() = sensorsOfRoom

    fun getSensor() = sensor

    init {
        getSensors()
        getRooms()
        if (sensorKey != 0) {
            sensor = dataSource.getSensorWithId(sensorKey)
        } else {
            val aux = MutableLiveData<SensorDataModel>()
            aux.value = SensorDataModel(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
            sensor = aux
        }
    }

    fun getSensorsOfRoom(id: Int) {
        sensorsOfRoom = sensorDataSource.getSensorsByRoom(id)
    }

    private fun getRooms() {
        viewModelScope.launch {
            try {
                roomsRepository.refreshRooms()

            } catch (networkError: IOException) {
                Log.d("failed refresh", "failed refresh sensors")
            }
        }
    }

    private fun getSensors() {
        viewModelScope.launch {
            try {
                sensorsRepository.refreshSensors()
            } catch (networkError: IOException) {
                Log.d("failed refresh", "failed refresh sensors")
            }
        }
    }
}