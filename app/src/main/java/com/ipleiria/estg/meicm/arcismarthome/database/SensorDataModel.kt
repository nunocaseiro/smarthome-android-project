package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor

@Entity(tableName = "sensors_table")
data class SensorDataModel(
    @PrimaryKey(autoGenerate = false)
    var id: Int?,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "sensortype")
    var sensortype: String?,
    @ColumnInfo(name = "value")
    var value: Double?,
    @ColumnInfo(name = "gpio")
    var gpio: Int?,
    @ColumnInfo(name = "room")
    var room: Int?,
    @ColumnInfo(name = "actuator")
    var actuator: Int?,
    @ColumnInfo(name = "icon")
    var icon: Int?,
    @ColumnInfo(name = "status")
    var status: String?,
    @ColumnInfo(name = "auto")
    var auto: Boolean?,
    @ColumnInfo(name = "temp_lim")
    var temp_lim: Double?,
    @ColumnInfo(name = "lux_lim")
    var lux_lim: Double?,
    @ColumnInfo(name = "roomname")
    var roomname: String?
) {

    override fun toString(): String {
        return name!!
    }
}

fun List<SensorDataModel>.asDomainModel(): List<Sensor> {
    return map {
        Sensor(
            id = it.id,
            name = it.name,
            sensortype = it.sensortype,
            value = it.value,
            gpio = it.gpio,
            room = it.room,
            status = it.status,
            actuator = it.actuator,
            auto = it.auto,
            temp_lim = it.temp_lim,
            lux_lim = it.lux_lim,
            roomname = it.roomname
        )
    }
}

fun SensorDataModel.asDM(sensor: SensorDataModel): Sensor {
    return Sensor(
        sensor.id,
        sensor.name,
        sensor.sensortype,
        sensor.value,
        sensor.gpio,
        sensor.room,
        sensor.roomname,
        sensor.status,
        sensor.actuator,
        sensor.temp_lim,
        sensor.lux_lim,
        sensor.auto
    )
}