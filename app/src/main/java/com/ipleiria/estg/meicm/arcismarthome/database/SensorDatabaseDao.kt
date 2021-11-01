package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
 interface SensorDatabaseDao {

   @Query("SELECT * from sensors_table WHERE id = :key")
   suspend fun get(key: Int): SensorDataModel

   @Query("SELECT * FROM sensors_table ORDER BY id DESC")
   fun getAllSensors(): LiveData<List<SensorDataModel>>

   @Query("SELECT * FROM sensors_table ORDER BY id DESC LIMIT 1")
   suspend fun getSensor(): SensorDataModel?

   @Query("SELECT * from sensors_table WHERE id = :key")
   fun getSensorWithId(key: Int): LiveData<SensorDataModel>

  @Query("SELECT * FROM sensors_table WHERE room = :keyRoom ORDER BY id DESC")
   fun getSensorsByRoom(keyRoom: Int): LiveData<List<SensorDataModel>>

   @Query("DELETE FROM sensors_table")
   suspend fun clear()

   @Insert
   suspend fun insert(sensor: SensorDataModel)

   @Insert(onConflict = OnConflictStrategy.REPLACE)
   fun insertAll(sensors: List<SensorDataModel>)

   @Update
    suspend fun update(sensor: SensorDataModel)

   @Delete
   fun delete(sensor: SensorDataModel)

}