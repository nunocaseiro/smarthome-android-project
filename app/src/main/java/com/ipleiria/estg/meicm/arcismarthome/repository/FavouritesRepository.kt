package com.ipleiria.estg.meicm.arcismarthome.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.asDomainModel
import com.ipleiria.estg.meicm.arcismarthome.models.*
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavouritesRepository(private val database: FavouriteDatabase) {

    val favouritesDB: LiveData<List<FavouriteDataModel>> = database.favouriteDatabaseDao.getAllFavourites()

    val favourites: LiveData<List<Favourite>> = Transformations.map(database.favouriteDatabaseDao.getAllFavourites()) {
        it.asDomainModel()
    }

    suspend fun refreshFavourites() {
        withContext(Dispatchers.IO) {
            Log.d("refresh favourites", "refresh favourites is called");
            val favourites = ApiClient.service.getFavourites(
                AppData.instance.user.token,
                AppData.instance.user.username!!
            )
            Log.d("v", favourites.toString())
            val vc = FavouriteContainer(favourites)
            database.favouriteDatabaseDao.clear()
            database.favouriteDatabaseDao.insertAll(vc.asDatabaseModel())
        }
    }

}