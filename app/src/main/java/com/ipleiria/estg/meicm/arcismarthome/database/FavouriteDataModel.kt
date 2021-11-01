package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ipleiria.estg.meicm.arcismarthome.models.Favourite
import com.ipleiria.estg.meicm.arcismarthome.models.Notification


@Entity(tableName = "favourites_table")
data class FavouriteDataModel (@PrimaryKey(autoGenerate = false)
                             var id: Int?,
                             @ColumnInfo(name = "sensor")
                             var sensor: Int,
                             @ColumnInfo(name = "user")
                             var user: Int)

    fun List<FavouriteDataModel>.asDomainModel(): List<Favourite> {
        return map {
            Favourite(
                id = it.id,
                sensor = it.sensor,
                user = it.user
                )
        }
    }

/*fun NotificationDataModel.asDM(notification: NotificationDataModel): Notification{
    return  Notification(notification.id,notification.notification, notification.seen,notification.created, notification.licensePlate,notification.description,notification.photo)

}
*/