package com.ipleiria.estg.meicm.arcismarthome.database
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface FavouriteDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favourite: FavouriteDataModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(favourites: List<FavouriteDataModel>)
    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
     fun update(favourite: FavouriteDataModel)

    @Delete
    fun delete(favourite: FavouriteDataModel)
    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from favourites_table WHERE id = :key")
    suspend fun get(key: Int): FavouriteDataModel

    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM favourites_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM favourites_table ORDER BY id DESC")
    fun getAllFavourites(): LiveData<List<FavouriteDataModel>>

    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM favourites_table ORDER BY id DESC LIMIT 1")
    suspend fun getFavourite(): FavouriteDataModel?

    /**
     * Selects and returns the night with given nightId.
     */
    @Query("SELECT * from favourites_table WHERE id = :key")
    fun getFavouriteWithId(key: Int): LiveData<FavouriteDataModel>

    @Query("SELECT * from favourites_table WHERE sensor = :key")
    fun getFavouriteWithSensorId(key: Int): LiveData<FavouriteDataModel>


}