package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ipleiria.estg.meicm.arcismarthome.models.Notification


@Entity(tableName = "notifications_table")
data class NotificationDataModel (@PrimaryKey(autoGenerate = false)
                             var id: Int,
                             @ColumnInfo(name = "seen")
                             var seen: Boolean,
                             @ColumnInfo(name = "notification")
                             var notification: String,
                             @ColumnInfo(name = "created")
                             var created: String,
                             @ColumnInfo(name = "licensePlate")
                             var licensePlate: String,
                             @ColumnInfo(name = "description")
                             var description: String,
                             @ColumnInfo(name = "photo")
                             var photo: String)

    fun List<NotificationDataModel>.asDomainModel(): List<Notification> {
        return map {
            Notification(
                id = it.id,
                notification = it.notification,
                seen = it.seen,
                created = it.created,
                licensePlate = it.licensePlate,
                description = it.description,
                photo = it.photo
                )
        }
    }

fun NotificationDataModel.asDM(notification: NotificationDataModel): Notification{
    return  Notification(notification.id,notification.notification, notification.seen,notification.created, notification.licensePlate,notification.description,notification.photo)

}
