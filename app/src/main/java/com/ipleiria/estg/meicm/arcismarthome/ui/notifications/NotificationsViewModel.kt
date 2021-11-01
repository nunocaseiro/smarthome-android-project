package com.ipleiria.estg.meicm.arcismarthome.ui.notifications

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.repository.NotificationsRepository
import kotlinx.coroutines.launch
import java.io.IOException


class NotificationsViewModel(dataSource: NotificationDatabaseDao, application: Application) :  ViewModel() {

    val notificationsRepository =  NotificationsRepository(NotificationDatabase.getInstance(application))

    val database = dataSource

    var notifications = notificationsRepository.notifications

    var notificationsDB = notificationsRepository.notificationsDB

    private val _notificationsFiltred = MutableLiveData<List<NotificationDataModel>>()

    private val _notificationClickedId = MutableLiveData<Int>()

    private var _notificationDeleted = MutableLiveData<Int>()

    val getNotificationClickedId: LiveData<Int>
        get() = _notificationClickedId

    val getNotificationDeleted: LiveData<Int>
        get() = _notificationDeleted

    fun onNotificationBeforeDelete(id: Int){
        _notificationDeleted.value = id
    }

    fun onNotificationAfterDelete(){
        _notificationDeleted.value = 0
    }

     fun onNotificationClicked(id: Int) {
        _notificationClickedId.value = id
    }

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

     fun delete(notification: NotificationDataModel) {
        database.delete(notification)
    }

    fun onNotificationDetailNavigated() {
        _notificationClickedId.value = null
    }

}