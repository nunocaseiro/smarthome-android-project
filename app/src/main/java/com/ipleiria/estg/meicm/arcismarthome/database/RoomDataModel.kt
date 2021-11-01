package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ipleiria.estg.meicm.arcismarthome.models.Room
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor

@Entity(tableName = "rooms_table")
data class RoomDataModel(
    @PrimaryKey(autoGenerate = false)
    var id: Int?,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "home")
    var home: Int?,
    @ColumnInfo(name = "ip")
    var ip: String?,
    @ColumnInfo(name = "roomtype")
    var roomtype: String?,
    @ColumnInfo(name = "testing")
    var testing: Boolean?
) {

    override fun toString(): String {
        return name!!
    }
}

fun List<RoomDataModel>.asDomainModel(): List<Room> {
    return map {
        Room(
            id = it.id,
            name = it.name,
            home = it.home,
            ip = it.ip,
            roomtype = it.roomtype,
            testing = it.testing,
        )
    }
}

fun RoomDataModel.asDM(room: RoomDataModel): Room {
    return Room(room.id, room.name, room.home, room.ip, room.roomtype, room.testing)
}
