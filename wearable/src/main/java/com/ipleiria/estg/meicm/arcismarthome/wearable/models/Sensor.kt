package com.ipleiria.estg.meicm.arcismarthome.wearable.models

import com.ipleiria.estg.meicm.arcismarthome.wearable.R
import java.io.Serializable

data class Sensor (var id: Int?, var name: String?, var sensortype: String?, var value: Double?, var room: Int?, var status: String?) :
    Serializable {
    var icon: Int = 0

    init {
        icon = when(sensortype) {
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
}
