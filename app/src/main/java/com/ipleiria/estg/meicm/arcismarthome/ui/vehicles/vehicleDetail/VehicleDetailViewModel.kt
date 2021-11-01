package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles.vehicleDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabaseDao

class VehicleDetailViewModel( private val vehicleKey: Int = 0, dataSource: VehicleDatabaseDao) : ViewModel() {

    val database = dataSource

    private var vehicle: LiveData<VehicleDataModel>? = null

    fun getVehicle() = vehicle

    private val _navigateToVehicleList = MutableLiveData<Int?>()

    private val _editVehicle = MutableLiveData<Boolean>()

    val key = vehicleKey

    init {
        if (vehicleKey != 0){
            vehicle=database.getVehicleWithId(vehicleKey)
        }
            _editVehicle.value=false
    }

    val navigateToVehicleListAfterSave: LiveData<Int?>
        get() = _navigateToVehicleList

    fun setEdit() {
        if (_editVehicle.value == true) {
            _editVehicle.value = false
            return
        }

        if (_editVehicle.value == false) {
            _editVehicle.value = true
            return
        }
    }

    fun exitEdit() {
        _editVehicle.value=false
    }

    fun doneNavigating() {
        _navigateToVehicleList.value = null
    }

    fun onDelete() {
        _navigateToVehicleList.value = 3
    }

    fun onSave() {
        if (vehicle == null){
            onAdd()
        }else{
            exitEdit()
            _navigateToVehicleList.postValue(2)
        }

    }

    private fun onAdd(){
        _navigateToVehicleList.postValue(1)
    }


}