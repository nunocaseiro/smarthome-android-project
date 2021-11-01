package com.ipleiria.estg.meicm.arcismarthome.wearable.server

import com.ipleiria.estg.meicm.arcismarthome.wearable.models.Sensor
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    // region Login/Register and User
    @POST("dj-rest-auth/login/")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>
    // endregion

    @GET("api/sensorsandroid/")
    fun getSensors(
        @Header("Authorization") token: String,
        @Query("home") home: Int
    ): Call<List<Sensor>>

}