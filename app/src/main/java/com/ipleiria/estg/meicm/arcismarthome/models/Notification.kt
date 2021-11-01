package com.ipleiria.estg.meicm.arcismarthome.models

import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class NotificationContainer(val notifications: List<Notification>)


@JsonClass(generateAdapter = true)
data class Notification(
    var id: Int,
    var notification: String,
    var seen: Boolean,
    var created: String,
    var licensePlate: String,
    var description: String,
    var photo: String
)


fun NotificationContainer.asDatabaseModel(): List<NotificationDataModel> {
    return notifications.map {
        NotificationDataModel(
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
