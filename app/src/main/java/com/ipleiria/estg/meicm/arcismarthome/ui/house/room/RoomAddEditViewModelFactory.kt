package com.ipleiria.estg.meicm.arcismarthome.ui.house.room

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.database.RoomDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.ui.notifications.notificationDetails.NotificationDetailViewModel

class RoomAddEditViewModelFactory(private val roomKey: Int,
                                  private val dataSource: RoomDatabaseDao
)  : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomAddEditViewModel::class.java)) {
            return RoomAddEditViewModel(roomKey, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}