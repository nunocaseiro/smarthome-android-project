package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ipleiria.estg.meicm.arcismarthome.models.Notification
import com.ipleiria.estg.meicm.arcismarthome.models.Vehicle

@Entity(tableName = "vehicles_table")
data class VehicleDataModel(
                    @PrimaryKey(autoGenerate = false)
                    var id: Int,
                    @ColumnInfo(name = "brand")
                    var brand: String,
                    @ColumnInfo(name = "licenseplate")
                    var licenseplate: String,
                    @ColumnInfo(name = "model")
                    var model: String,
                    @ColumnInfo(name = "year")
                    var year: Int,
                    @ColumnInfo(name = "home")
                    var home: Int
)

fun List<VehicleDataModel>.asDomainModel(): List<Vehicle> {
    return map {
        Vehicle(
            id = it.id,
            brand = it.brand,
            licenseplate = it.licenseplate ,
            model = it.model ,
            year = it.year,
        home = it.home)
    }
}

