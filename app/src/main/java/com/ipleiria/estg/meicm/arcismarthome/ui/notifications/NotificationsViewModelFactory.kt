package com.ipleiria.estg.meicm.arcismarthome.ui.notifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabaseDao


class NotificationsViewModelFactory(private val dataSource: NotificationDatabaseDao, private val application: Application) : ViewModelProvider.Factory {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
                return NotificationsViewModel(dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

}

