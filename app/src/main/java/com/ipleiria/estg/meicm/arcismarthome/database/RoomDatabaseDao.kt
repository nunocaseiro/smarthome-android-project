package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RoomDatabaseDao {

    @Query("SELECT * from rooms_table WHERE id = :key")
    suspend fun get(key: Int): RoomDataModel

    @Query("SELECT * FROM rooms_table ORDER BY id DESC")
    fun getAllRooms(): LiveData<List<RoomDataModel>>

    @Query("SELECT * FROM rooms_table ORDER BY id DESC LIMIT 1")
    suspend fun getRoom(): RoomDataModel?

    @Query("SELECT * from rooms_table WHERE id = :key")
    fun getRoomWithId(key: Int): LiveData<RoomDataModel>

    @Query("DELETE FROM rooms_table")
    suspend fun clear()

    @Insert
    suspend fun insert(room: RoomDataModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(rooms: List<RoomDataModel>)

    @Update
    fun update(room: RoomDataModel)

    @Delete
    fun delete(room: RoomDataModel)

}