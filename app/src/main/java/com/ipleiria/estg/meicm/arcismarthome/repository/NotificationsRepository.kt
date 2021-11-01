package com.ipleiria.estg.meicm.arcismarthome.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.asDomainModel
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Notification
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.models.NotificationContainer
import com.ipleiria.estg.meicm.arcismarthome.models.asDatabaseModel
import com.ipleiria.estg.meicm.arcismarthome.server.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationsRepository(private val database: NotificationDatabase) {

    val notifications: LiveData<List<Notification>> = Transformations.map(database.notificationDatabaseDao.getAllNotifications()) {
        it.asDomainModel()
    }

    val notificationsDB: LiveData<List<NotificationDataModel>> = database.notificationDatabaseDao.getAllNotifications()

    /**
     * Refresh the videos stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     *
     */
    suspend fun refreshNotifications() {
        withContext(Dispatchers.IO) {
            Log.d("refresh notifications","refresh videos is called");
            val notifications = ApiClient.service.getNotifications(AppData.instance.user.token,
                AppData.instance.user.id!!)
            Log.d("v",notifications.toString())
            val vc = NotificationContainer(notifications)
            database.notificationDatabaseDao.clear()
            database.notificationDatabaseDao.insertAll(vc.asDatabaseModel())
        }
    }

}