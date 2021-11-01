package com.ipleiria.estg.meicm.arcismarthome.server

import java.io.Serializable

class HomeResponse: Serializable {
    var id: Int = 0
    var name: String = ""
    var latitude: String = ""
    var longitude: String = ""
}