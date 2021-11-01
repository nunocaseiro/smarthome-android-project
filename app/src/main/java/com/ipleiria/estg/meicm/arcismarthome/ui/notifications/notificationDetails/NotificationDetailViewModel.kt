package com.ipleiria.estg.meicm.arcismarthome.ui.notifications.notificationDetails

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.repository.NotificationsRepository
import kotlinx.coroutines.launch
import java.io.IOException

class NotificationDetailViewModel(private val notificationKey: Int = 0, dataSource: NotificationDatabaseDao, application: Application) : ViewModel() {

    val notificationsRepository =  NotificationsRepository(NotificationDatabase.getInstance(application))

    init {
        onStart()
    }

    fun onStart() {

        viewModelScope.launch {
            try {
                notificationsRepository.refreshNotifications()
            } catch (networkError: IOException) {
                Log.d("failed refresh", "failed refresh notifications")
            }
        }
    }

    val database = dataSource

    private var notification: LiveData<NotificationDataModel> = MutableLiveData()

    fun getNotification() = notification

    private val _navigateToVehicleList = MutableLiveData<Int?>()

    init {
        if (notificationKey != 0) {
            notification = database.getNotificationWithId(notificationKey)
        }
    }
}