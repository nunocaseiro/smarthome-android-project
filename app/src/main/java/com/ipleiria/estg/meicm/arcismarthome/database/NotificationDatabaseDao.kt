package com.ipleiria.estg.meicm.arcismarthome.database
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface NotificationDatabaseDao {
    @Insert
    suspend fun insert(notification: NotificationDataModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(notifications: List<NotificationDataModel>)
    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
     fun update(notification: NotificationDataModel)

    @Delete
    fun delete(notification: NotificationDataModel)
    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from notifications_table WHERE id = :key")
    suspend fun get(key: Int): NotificationDataModel

    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM notifications_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM notifications_table ORDER BY id DESC")
    fun getAllNotifications(): LiveData<List<NotificationDataModel>>

    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM notifications_table ORDER BY id DESC LIMIT 1")
    suspend fun getNotification(): NotificationDataModel?

    /**
     * Selects and returns the night with given nightId.
     */
    @Query("SELECT * from notifications_table WHERE id = :key")
    fun getNotificationWithId(key: Int): LiveData<NotificationDataModel>


}