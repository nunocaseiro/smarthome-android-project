package com.ipleiria.estg.meicm.arcismarthome.models

import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDataModel
import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.util.*

@JsonClass(generateAdapter = true)
data class Sensor(
    var id: Int?,
    var name: String?,
    var sensortype: String?,
    var value: Double?,
    var gpio: Int?,
    var room: Int?,
    var roomname: String?,
    var status: String?,
    var actuator: Int?,
    var temp_lim: Double?,
    var lux_lim: Double?,
    var auto: Boolean?
) : Serializable {
    var icon: Int = 0

    data class SensorRequest(
        var id: Int?,
        var name: String?,
        var sensortype: String?,
        var value: Double?,
        var gpio: Int?,
        var ios: Boolean = false,
        var room: Int?,
        var roomname: String?,
        var actuator: String?,
        var icon: Int?,
        var status: String?,
        var temp_lim: Double?,
        var lux_lim: Double?,
        var auto: Boolean?
    )

    init {
        icon = when (sensortype) {
            "led" -> R.drawable.ic_led_border
            "camera" -> R.drawable.ic_camera_border
            "plug" -> R.drawable.ic_plug_border
            "servo" -> R.drawable.ic_servo_border
            "temperature" -> R.drawable.ic_temperature_border
            "luminosity" -> R.drawable.ic_luminosity_border
            "motion" -> R.drawable.ic_motion_border
            // "door" -> R.drawable.ic_servo_border
            // "gate" -> R.drawable.ic_servo_border
            else -> R.drawable.ic_no_icon_border_24dp
        }
    }

    override fun toString(): String {
        return name!!
    }
}

@JsonClass(generateAdapter = true)
data class SensorContainer(val sensors: List<Sensor>)

fun SensorContainer.asDatabaseModel(): List<SensorDataModel> {
    return sensors.map {
        SensorDataModel(
            id = it.id!!,
            name = it.name!!,
            sensortype = it.sensortype!!,
            value = it.value!!,
            gpio = it.gpio!!,
            room = it.room!!,
            roomname = it.roomname,
            actuator = it.actuator,
            icon = it.icon,
            status = it.status,
            lux_lim = it.lux_lim,
            temp_lim = it.temp_lim,
            auto = it.auto
        )
    }
}