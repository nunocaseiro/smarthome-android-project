package com.ipleiria.estg.meicm.arcismarthome.server

import com.ipleiria.estg.meicm.arcismarthome.database.RoomDataModel
import com.ipleiria.estg.meicm.arcismarthome.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.*
import kotlin.collections.HashMap


interface UserService {

    // region Session
    @POST("dj-rest-auth/login/")
    fun loginUser(@Body loginRequest: LoginRequest?): Call<LoginResponse?>?

    @POST("api/register/")
    fun registerUser(@Body user: UserRegister): Call<Void>

    @DELETE("api/users/{id}/")
    fun deleteUser(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    @PUT("api/changepassword/{id}/")
    fun updatePassword(@Body password: PasswordRequest, @Path("id") id: Int, @Header("Authorization") token: String): Call<Void>

    @PATCH("api/profiles/{id}/")
    fun updatePhoto(@Body photo: HashMap<String,String>, @Path("id") id: Int, @Header("Authorization") token: String): Call<ProfileResponse>?


    @PUT("api/users/{id}/")
    fun updateUser(@Body user: UserRegister, @Path("id") id: Int, @Header("Authorization") token: String): Call<UserResponse>

    @GET("api/userprofile/")
    fun populateUserWHome(
            @Header("Authorization") token: String,
            @Query("username") username: String?
    ): Call<List<ProfileResponse>>

    @GET("api/deletephoto/")
    fun deleteProfilePhoto(
            @Header("Authorization") token: String,
            @Query("profile") profile: Int): Call<Void>

    @POST("dj-rest-auth/logout/")
    fun logoutUser(@Query("token") token: String?): Call<Void>
    // endregion


    // region Dashboard Fragment
    @GET("data/2.5/weather?units=metric")
    fun getWeatherOfHome(
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("appid") appid: String
    ): Call<WeatherResponse>

    @GET("api/statistics")
    fun getStatistics(
            @Header("Authorization") token: String,
            @Query("home") home: Int
    ): Call<StatisticsResponse>
    // endregion


    // region House Fragment
    @GET("api/actualrooms/")
    suspend fun populateRooms(
            @Header("Authorization") token: String,
            @Query("home") home: Int
    ): List<Room>

    @GET("api/sensorsandroid/")
    suspend fun getSensors(
            @Header("Authorization") token: String,
            @Query("home") home: Int
    ): List<Sensor>

    @GET("api/sensortypes")
    suspend fun getSensorType(@Header("Authorization") token: String): Any

    @POST("api/rooms/")
    fun createRoom(@Header("Authorization") token: String, @Body room: RoomDataModel): Call<Void>

    @PUT("api/rooms/{id}/")
    fun updateRoom(
            @Header("Authorization") token: String,
            @Path("id") id: Int,
            @Body room: RoomDataModel
    ): Call<Void>

    @DELETE("api/rooms/{id}/")
    fun deleteRoom(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>

    @POST("api/sensors/")
    fun createSensor(
            @Header("Authorization") token: String,
            @Body sensor: Sensor.SensorRequest
    ): Call<Void>

    @PUT("api/sensors/{id}/")
    fun updateSensor(
            @Header("Authorization") token: String,
            @Path("id") id: Int,
            @Body sensor: Sensor.SensorRequest
    ): Call<Void>

    @DELETE("api/sensors/{id}/")
    fun deleteSensor(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>
    // endregion


    // region Favorites
    @GET("api/favouritesofuser/")
    suspend fun getFavourites(
            @Header("Authorization") token: String,
            @Query("username") user: String
    ): List<Favourite>

    @POST("api/favourites/")
    fun insertFavourite(
            @Header("Authorization") token: String,
            @Body favourite: Favourite
    ): Call<Favourite>

    @DELETE("api/favourites/{id}/")
    fun deleteFavourite(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>
    // endregion


    // region Vehicles Fragment
    @GET("api/vehiclesofhome")
    suspend fun getVehicles(
            @Header("Authorization") token: String,
            @Query("home") home: Int
    ): List<NetworkVehicle>

    @PUT("api/vehicles/{id}/")
    fun updateVehicle(
            @Header("Authorization") token: String,
            @Path("id") id: Int?,
            @Body vehicle: Vehicle
    ): Call<NetworkVehicle>

    @DELETE("api/vehicles/{id}/")
    fun deleteVehicle(
            @Header("Authorization") token: String,
            @Path("id") id: Int
    ): Call<NetworkVehicle>

    @POST("api/vehicles/")
    fun insertVehicle(
            @Header("Authorization") token: String,
            @Body vehicle: Vehicle
    ): Call<NetworkVehicle>
    // endregion


    // region Notifications Fragment
    @GET("api/getNotificationsByUser")
    suspend fun getNotifications(
            @Header("Authorization") token: String,
            @Query("user") user: Int
    ): List<Notification>

    @Headers("Content-Type: application/json")
    @PUT("api/notifications/{id}/")
    fun updateNotification(
            @Header("Authorization") token: String,
            @Path("id") id: Int?,
            @Body notification: Notification
    ): Call<Notification>

    @DELETE("api/notifications/{id}/")
    fun deleteNotification(
            @Header("Authorization") token: String,
            @Path("id") id: Int
    ): Call<Notification>
    // endregion


    // region Settings Fragment
    @GET("api/accounts")
    fun getHomeAccounts(
            @Header("Authorization") token: String,
            @Query("home") home: Int
    ): Call<ArrayList<Any>>

    @PATCH("api/users/{id}/")
    fun updateUserRole(
            @Header("Authorization") token: String,
            @Path("id") id: Int,
            @Body userRoleGroup: RoleGroupRequest
    ): Call<Void>
    // endregion


    //region House
    @GET("api/checkkey/")
    fun checkKey(@Query("key") key: String): Call<HouseKeyResponse>?

    @POST("api/homes/")
    fun createHome(@Body home: Home): Call<Home>

    @GET("api/gethousewkey/")
    fun getHomeWKey(@Query("key") key: String): Call<Home>?

    @DELETE("api/homes/{id}/")
    fun deleteHome(@Header("Authorization") token: String, @Path("id") id: Int): Call<Void>


    @Headers("Content-Type: application/json")
    @PUT("api/homes/{id}/")
    fun updateHome(
            @Header("Authorization") token: String,
            @Path("id") id: Int?,
            @Body home: Home
    ): Call<Home>

    //endregion
    @POST("https://dialogflow.googleapis.com/v2/projects/arcidialogflow/agent/entityTypes/{entity}/entities:batchUpdate?key=")
   fun createEntityValue(@Header("Authorization") token: String, @Header("Accept") accept: String, @Header("Content-Type") ct: String, @Path("entity") string: String, @Body body:RequestBody): Call<Void>

    @POST("https://dialogflow.googleapis.com/v2/projects/arcidialogflow/agent/entityTypes/{entity}/entities:batchDelete?key=")
    fun deleteEntityValue(@Header("Authorization") token: String, @Header("Accept") accept: String, @Header("Content-Type") ct: String, @Path("entity") string: String, @Body body:RequestBody): Call<Void>
}