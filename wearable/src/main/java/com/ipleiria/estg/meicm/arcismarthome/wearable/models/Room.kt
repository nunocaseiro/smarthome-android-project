package com.ipleiria.estg.meicm.arcismarthome.wearable.models

data class Room (var id: Int, var name: String, var home: Int, var ip : String, var roomtype : String) {
    var sensors: ArrayList<Sensor> = ArrayList()

    override fun toString(): String {
        return name
    }
}