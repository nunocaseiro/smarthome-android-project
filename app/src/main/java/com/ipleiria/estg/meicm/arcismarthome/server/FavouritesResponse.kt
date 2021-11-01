package com.ipleiria.estg.meicm.arcismarthome.server

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FavouritesResponse: Serializable {
    @SerializedName("id")
    var id: Int? = null
    @SerializedName("user")
    var user: Int? = null
    @SerializedName("sensor")
    var sensor: Int? = null
}
