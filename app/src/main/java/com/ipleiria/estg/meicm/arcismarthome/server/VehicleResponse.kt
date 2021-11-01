package com.ipleiria.estg.meicm.arcismarthome.server

import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDataModel
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class VehicleContainer(val vehicles: List<NetworkVehicle>)


@JsonClass(generateAdapter = true)
data class NetworkVehicle(
    val id: Int ,val brand: String, val licenseplate: String, val model: String, val year: Int, val home: Int)


/**
 * Convert Network results to database objects
 */
fun VehicleContainer.asDatabaseModel(): List<VehicleDataModel> {
    return vehicles.map {
        VehicleDataModel(
            id = it.id,
            brand = it.brand,
            licenseplate = it.licenseplate,
            model = it.model,
            year = it.year,
            home = it.home)
    }
}