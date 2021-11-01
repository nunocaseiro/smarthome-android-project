package com.ipleiria.estg.meicm.arcismarthome.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ipleiria.estg.meicm.arcismarthome.MainActivity
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.models.*
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.VehicleContainer
import com.ipleiria.estg.meicm.arcismarthome.server.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttException
import java.util.*

class SensorsRepository(private val database: SensorDatabase) {

    val sensorsDB: LiveData<List<SensorDataModel>> = database.sensorDatabaseDao.getAllSensors()

    val sensors: LiveData<List<Sensor>> =
        Transformations.map(database.sensorDatabaseDao.getAllSensors()) {
            it.asDomainModel()
        }

    init {
        /* AppData.instance.sensorValueUpdate.observeForever {
             val id = it.Id
             val value = it.value
             sensorsDB.observeForever {
                 for (sensor in it) {
                     if (sensor.id == id) {
                         sensor.value = value
                         runBlocking {
                             withContext(Dispatchers.IO) {
                                 database.sensorDatabaseDao.update(sensor)
                             }
                         }
                     }
                 }

             }
         }*/
    }


    suspend fun refreshSensors() {
        withContext(Dispatchers.IO) {
            Log.d("refresh sensors", "refresh sensors is called");
            val sensors = ApiClient.service.getSensors(AppData.instance.user.token, AppData.instance.home.id!!)
            Log.d("v", sensors.toString())
            val newSensors = LinkedList<Sensor>()

            for (i in sensors){
                newSensors.add(Sensor(i.id, i.name, i.sensortype, i.value, i.gpio, i.room,i.roomname, i.status, i.actuator,  i.temp_lim,i.lux_lim, i.auto))
                if (AppData.instance.mqttClient.isConnected && i.id != null){
                    subscribe("/"+i.id, qos= 1)
                }
            }
            val sc = SensorContainer(newSensors)
            database.sensorDatabaseDao.clear()
            database.sensorDatabaseDao.insertAll(sc.asDatabaseModel())
        }
    }

    private fun subscribe(topic: String, qos: Int = 1) {
        try {
            AppData.instance.mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(MainActivity.TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}