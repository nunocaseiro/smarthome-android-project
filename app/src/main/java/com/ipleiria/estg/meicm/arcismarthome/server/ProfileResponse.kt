package com.ipleiria.estg.meicm.arcismarthome.server

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ProfileResponse: Serializable {

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("user")
    var user: UserResponse? = null

    @SerializedName("photo")
    var photo: String? = null

    @SerializedName("home")
    var home: HomeResponse? = null

    @SerializedName("favourites")
    var favourites: List<FavouritesResponse>? = null

    @SerializedName("notifications")
    var notifications: List<FavouritesResponse>? = null
}