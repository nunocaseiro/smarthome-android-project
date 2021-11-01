package com.ipleiria.estg.meicm.arcismarthome.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.repository.FavouritesRepository

import com.ipleiria.estg.meicm.arcismarthome.repository.RoomsRepository
import com.ipleiria.estg.meicm.arcismarthome.repository.SensorsRepository
import kotlinx.coroutines.launch
import java.io.IOException

class DashboardViewModel(application: Application) :  ViewModel() {

    val sensorsRepository =  SensorsRepository(SensorDatabase.getInstance(application))
    val favouritesRepository =  FavouritesRepository(FavouriteDatabase.getInstance(application))

    var sensors = sensorsRepository.sensors
    var favourites = favouritesRepository.favourites

    private val _sensorClickedId = MutableLiveData<Int>()

    val getSensorClickedId: LiveData<Int>
        get() = _sensorClickedId

    init{
        getSensors()
        getFavourites()
    }

    private fun getFavourites() {

            viewModelScope.launch {
                try {
                    favouritesRepository.refreshFavourites()
                } catch (networkError: IOException) {
                    Log.d("failed refresh", "failed refresh sensors")
                }
            }
    }

    fun onSensorClicked(sensorId: Int) {
        _sensorClickedId.value = sensorId
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

    fun onFavouriteDetailNavigated() {
        _sensorClickedId.value = null
    }


}