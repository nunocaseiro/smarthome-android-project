package com.ipleiria.estg.meicm.arcismarthome.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDataModel

import com.ipleiria.estg.meicm.arcismarthome.models.Vehicle
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.asDomainModel
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.VehicleContainer
import com.ipleiria.estg.meicm.arcismarthome.server.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VehiclesRepository(private val database: VehicleDatabase) {

    val vehicles: LiveData<List<Vehicle>> = Transformations.map(database.vehicleDatabaseDao.getAllVehicles()) {
        it.asDomainModel()
    }

    val vehiclesDB: LiveData<List<VehicleDataModel>> = database.vehicleDatabaseDao.getAllVehicles()


    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    suspend fun refreshVehicles() {
        withContext(Dispatchers.IO) {
            Log.d("refresh videos","refresh videos is called");
            val vehicles = ApiClient.service.getVehicles(AppData.instance.user.token, AppData.instance.home.id!!)
            Log.d("v",vehicles.toString())
            val vc = VehicleContainer(vehicles)
            database.vehicleDatabaseDao.clear()
            database.vehicleDatabaseDao.insertAll(vc.asDatabaseModel())
        }
    }

}