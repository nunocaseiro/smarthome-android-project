package com.ipleiria.estg.meicm.arcismarthome.models

import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.RoomDataModel
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Room (var id: Int?, var name: String?, var home: Int?, var ip : String?, var roomtype : String?, var testing: Boolean? = false) : Serializable {
    var sensors: ArrayList<Sensor> = ArrayList()

    override fun toString(): String {
        return name!!
    }
}

@JsonClass(generateAdapter = true)
data class RoomContainer(val rooms: List<Room>)

fun RoomContainer.asDatabaseModel(): List<RoomDataModel> {
    return rooms.map {
        RoomDataModel(
            id = it.id!!,
            name = it.name!!,
            home = it.home!!,
            ip = it.ip,
            roomtype = it.roomtype!!,
            testing = it.testing!!,
        )
    }
}