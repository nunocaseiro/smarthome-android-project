package com.ipleiria.estg.meicm.arcismarthome.wearable.models

data class Home(var id: Int?, var name: String?, var lat: String?, var long: String?){
    var rooms: ArrayList<Room> = ArrayList()
}