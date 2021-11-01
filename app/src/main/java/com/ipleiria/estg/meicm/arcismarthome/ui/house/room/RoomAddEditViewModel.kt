package com.ipleiria.estg.meicm.arcismarthome.ui.house.room

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.repository.NotificationsRepository
import com.ipleiria.estg.meicm.arcismarthome.repository.RoomsRepository
import kotlinx.coroutines.launch
import java.io.IOException

class RoomAddEditViewModel(roomKey: Int, dataSourceRoom: RoomDatabaseDao) :  ViewModel() {

    val database = dataSourceRoom

    private var room: LiveData<RoomDataModel> = MutableLiveData()

    fun getRoom() = room

    init {
        if (roomKey != 0) {
            room = database.getRoomWithId(roomKey)
        }else{
            val aux = MutableLiveData<RoomDataModel>()
            aux.value = RoomDataModel(null,null,null,null,null,false)
            room=aux
        }
    }
}