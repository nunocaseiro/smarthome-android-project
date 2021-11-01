package com.ipleiria.estg.meicm.arcismarthome.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDataModel

@Dao
interface VehicleDatabaseDao {

    @Insert
    suspend fun insert(vehicle: VehicleDataModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( videos: List<VehicleDataModel>)
    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
    suspend fun update(vehicle: VehicleDataModel)

    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from vehicles_table WHERE id = :key")
    suspend fun get(key: Int): VehicleDataModel

    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM vehicles_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM vehicles_table ORDER BY id DESC")
    fun getAllVehicles(): LiveData<List<VehicleDataModel>>

    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM vehicles_table ORDER BY id DESC LIMIT 1")
    suspend fun getVehicle(): VehicleDataModel?

    /**
     * Selects and returns the night with given nightId.
     */
    @Query("SELECT * from vehicles_table WHERE id = :key")
    fun getVehicleWithId(key: Int): LiveData<VehicleDataModel>
}
