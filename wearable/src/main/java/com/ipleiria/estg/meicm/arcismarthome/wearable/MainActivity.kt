package com.ipleiria.estg.meicm.arcismarthome.wearable

import android.content.Context
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.ipleiria.estg.meicm.arcismarthome.wearable.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.wearable.server.LoginRequest
import com.ipleiria.estg.meicm.arcismarthome.wearable.server.LoginResponse
import com.ipleiria.estg.meicm.arcismarthome.wearable.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.wearable.models.Sensor
import com.ipleiria.estg.meicm.arcismarthome.wearable.models.SensorUpdate
import com.ipleiria.estg.meicm.arcismarthome.wearable.models.User
import com.ipleiria.estg.meicm.arcismarthome.wearable.server.SensorValueRequest
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : WearableActivity(), MainMenuAdapter.CellClickListener {

    private val instance = AppData.instance
    private lateinit var recyclerView: WearableRecyclerView
    val sensorsList = mutableListOf<Sensor>()

    companion object {
        const val TAG = "AndroidMqttClient"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)
        recyclerView = findViewById(R.id.main_menu_view)
        userLogin()
    }

    private fun userLogin() {
        val loginRequest = LoginRequest()
        loginRequest.username = "smarthome"
        loginRequest.password = "meicm123"

        val loginResponseCall = ApiClient.service.loginUser(loginRequest)
        loginResponseCall.enqueue((object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                instance.user = User(
                    null,
                    loginRequest.username,
                    null,
                    null,
                    null,
                    "Token ${response.body()!!.key}"
                )
                getSensors()
                connect(this@MainActivity)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Error", "Falha no login.")
            }
        }))
    }

    private fun getSensors() {
        val sensorsGetResponseCall =
            ApiClient.service.getSensors(AppData.instance.user.token, 1)
        sensorsGetResponseCall.enqueue((object : Callback<List<Sensor>> {
            override fun onResponse(call: Call<List<Sensor>>, response: Response<List<Sensor>>) {
                for (i in response.body()!!) {
                    sensorsList.add(Sensor(i.id, i.name, i.sensortype, i.value, i.room, i.status))
                }

                fillList(sensorsList)
            }

            override fun onFailure(call: Call<List<Sensor>>, t: Throwable) {
                Log.e("Error", "Falha a obter os sensores.")
            }
        }))
    }

    private fun fillList(sensorsList: MutableList<Sensor>) {
        recyclerView.setHasFixedSize(true)
        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.layoutManager = WearableLinearLayoutManager(this)

        recyclerView.adapter = MainMenuAdapter(this, sensorsList as ArrayList<Sensor>, this)
    }

    fun connect(context: Context) {
        val serverURI = "tcp://161.35.8.148:1883"
        AppData.instance.mqttClient =
            MqttAndroidClient(context, serverURI, AppData.instance.user.username)
        AppData.instance.mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")

                val jsonmsg = JSONObject(message.toString())

                if (topic.equals("/android")) {

                    val value = jsonmsg["value"] as String

                } else if (topic.equals("all")) {

                    if (jsonmsg["action"] == "updateSensors") {
                        AppData.instance.sensorUpdate.value = 1
                    }
                } else {
                    if (jsonmsg["action"] == "sval") {
                        val id = topic?.substring(1)?.toInt()
                        val value = jsonmsg["value"].toString().toDouble()
                        AppData.instance.sensorValueUpdate.value =
                            id?.let { SensorUpdate(it, value) }
                        sensorsList.forEach {
                            if (it.id == id) {
                                it.value = value
                            }
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
                connect(this@MainActivity)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })

        val options = MqttConnectOptions()
        options.userName = "smarthome"
        options.password = "smarthome".toCharArray()
        options.isCleanSession = false

        try {
            AppData.instance.mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    subscribe("/android", qos = 1)
                    subscribe("all", qos = 1)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            AppData.instance.mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun onCellClickListener(sensor: Sensor) {
        var result = if (sensor.value == 1.0) {
            "off"
        } else {
            "on"
        }

        val jsn =
            JSONObject("{to:'" + sensor.room + "','from':'server', 'action':'turn', 'value':'" + result + "'}");
        val send = MqttMessage()
        send.payload = jsn.toString().toByteArray()

        result = if (sensor.sensortype == "servo") {
            if (sensor.value == 1.0) "Opened"
            else "Closed"
        } else {
            if (sensor.value == 1.0) "Off"
            else "On"
        }
        val message = sensor.name + " - " + result
        AppData.instance.mqttClient.publish(
            '/' + sensor.id.toString(),
            send,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Toast.makeText(this@MainActivity, "Falha", Toast.LENGTH_SHORT).show()
                }
            })
    }
}