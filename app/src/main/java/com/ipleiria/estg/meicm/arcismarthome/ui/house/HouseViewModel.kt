package com.ipleiria.estg.meicm.arcismarthome.ui.house

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

class HouseViewModel(dataSourceSensor: SensorDatabaseDao, application: Application) :  ViewModel() {

    val sensorsRepository =  SensorsRepository(SensorDatabase.getInstance(application))

    var sensors = sensorsRepository.sensors

    val roomsRepository =  RoomsRepository(RoomsDatabase.getInstance(application))

    var rooms = roomsRepository.rooms

    var dataSourceS = dataSourceSensor

    private var _roomsDone = MutableLiveData<Int>()

    private var sensor: LiveData<SensorDataModel> = MutableLiveData()

    fun getSensor()  = sensor

    val roomsDone: LiveData<Int>
        get() = _roomsDone

    fun getRooms() {
        viewModelScope.launch {
            try {
                roomsRepository.refreshRooms()

            } catch (networkError: IOException) {
                Log.d("failed refresh", "failed refresh sensors")
            }
        }
    }

    fun getSensors(){
        viewModelScope.launch {
            try {
                sensorsRepository.refreshSensors()
            } catch (networkError: IOException) {
                Log.d("failed refresh", "failed refresh sensors")
            }
        }
    }

    fun afterRoomsDownloaded(){
        _roomsDone.postValue(1)
    }

    fun afterSensors(){
        _roomsDone.postValue(null)
    }
}