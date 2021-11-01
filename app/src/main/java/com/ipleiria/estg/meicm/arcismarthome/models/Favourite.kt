package com.ipleiria.estg.meicm.arcismarthome.models

import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.squareup.moshi.JsonClass



@JsonClass(generateAdapter = true)
data class FavouriteContainer(val favourites: List<Favourite>)


@JsonClass(generateAdapter = true)
data class Favourite(var id: Int?,var sensor: Int, var user: Int)


fun FavouriteContainer.asDatabaseModel(): List<FavouriteDataModel> {
    return favourites.map {
        FavouriteDataModel(
            id = it.id,
            user = it.user,
            sensor = it.sensor
        )
    }
}