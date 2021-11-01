package com.ipleiria.estg.meicm.arcismarthome

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ipleiria.estg.meicm.arcismarthome.databinding.ActivityMainBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.SensorUpdate
import com.ipleiria.estg.meicm.arcismarthome.ui.notifications.NotificationReceiver
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.lang.Math.abs
import kotlin.random.Random


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityMainBinding
    lateinit var gestureDetector: GestureDetector
    var x2: Float = 0.0f
    var x1: Float = 0.0f

    companion object {
        const val MIN_DISTANCE = 150
        const val TAG = "AndroidMqttClient"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.include.homeName = AppData.instance.home.name.toString()
        AppData.instance.housename.observe(this,{
            if ( it != null && it.isNotEmpty()){
                binding.include.homeName = AppData.instance.home.name.toString()
            }
        })

        val nightModeFlags = applicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> binding.include.logo.setImageResource(R.drawable.logo_light_mode)
            Configuration.UI_MODE_NIGHT_YES -> binding.include.logo.setImageResource(R.drawable.logo_dark_mode)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> binding.include.logo.setImageResource(R.drawable.logo_light_mode)
        }

        val navView: BottomNavigationView = binding.navView

        if (AppData.instance.user.role == "user") navView.menu.removeItem(R.id.navigation_notifications)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(listener)

        connect(this)
        createNotificationChannel()
        gestureDetector = GestureDetector(this, this)

    }

    private val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.label) {
            "fragment_sensor_view" -> binding.include.root.visibility = View.GONE
            "fragment_sensor_add_edit" -> binding.include.root.visibility = View.GONE
            "fragment_room_add_edit" -> binding.include.root.visibility = View.GONE
            "fragment_vehicle_detail" -> binding.include.root.visibility = View.GONE
            "fragment_notification_detail" -> binding.include.root.visibility = View.GONE   // doesn't work
            "fragment_register_account" -> binding.include.root.visibility = View.GONE
            else -> binding.include.root.visibility = View.VISIBLE
        }
    }


    fun connect(context: Context) {
        val serverURI = "tcp://161.35.8.148:1883"
        AppData.instance.mqttClient =
            MqttAndroidClient(context, serverURI, AppData.instance.user.username + Random(4).toString() )
        AppData.instance.mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")

                val jsonmsg: JSONObject = JSONObject(message.toString())

                if (topic.equals("/android")) {

                    val value = jsonmsg["value"] as String

                    if (jsonmsg["action"] == "newPhoto" && jsonmsg["user"] == AppData.instance.user.username && AppData.instance.user.role != "user") {
                        AppData.instance.notification.value = value.toInt()

                        //  if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){

                        val notifyIntent =
                            Intent(this@MainActivity, NotificationReceiver::class.java)
                        val alarmManager =
                            this@MainActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val notifyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
                            this@MainActivity,
                            0,
                            notifyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        AlarmManagerCompat.setExactAndAllowWhileIdle(
                            alarmManager,
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            0,
                            notifyPendingIntent
                        )
                        //}
                    }

                } else if (topic.equals("all")) {

                    if (jsonmsg["action"] == "updateSensors") {
                        AppData.instance.sensorUpdate.value = 1
                    }
                } else {

                    if (jsonmsg["action"] == "sval") {

                        val id = topic?.substring(1)?.toInt()
                        val value = jsonmsg["value"].toString().toDouble()
                        val from = jsonmsg["from"].toString()
                        AppData.instance.sensorValueUpdate.value =
                            id?.let { SensorUpdate(it, value, from) }
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
        var c = ""
        options.password = c.toCharArray()
        options.isCleanSession = true
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


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "vehiclesNotifications"
            val descriptionText = "VehicleNotifications "
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }

            channel.enableLights(true)
            channel.lightColor = R.color.ARCIBlue
            channel.enableVibration(true)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event?.action) {
            0 -> {
                x1 = event.x
            }
            1 -> {
                x2 = event.x
                val valueX = x2 - x1
                if (abs(valueX) > MIN_DISTANCE) {
                    if (x2 > x1) {
                        openActivityCamera()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun openActivityCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }


    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        return
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

}
