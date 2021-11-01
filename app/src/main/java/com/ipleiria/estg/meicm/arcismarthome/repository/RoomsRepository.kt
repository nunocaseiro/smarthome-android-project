package com.ipleiria.estg.meicm.arcismarthome.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.models.*
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomsRepository(private val database: RoomsDatabase) {

    val roomsDB: LiveData<List<RoomDataModel>> = database.roomDatabaseDao.getAllRooms()

    val rooms: LiveData<List<Room>> = Transformations.map(database.roomDatabaseDao.getAllRooms()) {
        it.asDomainModel()
    }


    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    suspend fun refreshRooms() {
        withContext(Dispatchers.IO) {
            Log.d("refresh rooms","refresh rooms is called");
            val rooms = ApiClient.service.populateRooms(AppData.instance.user.token, AppData.instance.home.id!!)
            Log.d("v",rooms.toString())
            val rc = RoomContainer(rooms)
            database.roomDatabaseDao.clear()
            database.roomDatabaseDao.insertAll(rc.asDatabaseModel())
        }
    }

}