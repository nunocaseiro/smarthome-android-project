package com.ipleiria.estg.meicm.arcismarthome.ui.notifications.notificationDetails

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.ui.vehicles.vehicleDetail.VehicleDetailViewModel

class NotificationDetailViewModelFactory(private val notificationKey: Int,
                                         private val dataSource: NotificationDatabaseDao, private val application: Application)  : ViewModelProvider.Factory {
           @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationDetailViewModel::class.java)) {
                return NotificationDetailViewModel(notificationKey, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}