package com.ipleiria.estg.meicm.arcismarthome.models

import androidx.room.PrimaryKey

data class Vehicle(
    var id: Int?,
    val brand: String,
    val licenseplate: String,
    val model: String,
    val year: Int,
    val home:Int){
    constructor( brand: String, licenseplate: String, model: String, year: Int, home: Int) : this(null, brand, licenseplate, model, year, home)
}