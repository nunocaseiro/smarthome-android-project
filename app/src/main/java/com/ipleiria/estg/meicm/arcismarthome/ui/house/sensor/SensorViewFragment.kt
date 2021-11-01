package com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentSensorViewBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Favourite
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.ui.register.RegisterHomeFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SensorViewFragment : Fragment() {

    private lateinit var binding: FragmentSensorViewBinding
    private var sensorId: Int = 0
    private var fav = false
    private lateinit var sensor: SensorDataModel
    private lateinit var sensorViewModel: SensorViewModel
    private lateinit var sensorFav: FavouriteDataModel



    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sensor_view, container, false)
        binding.lifecycleOwner = this

        val bundle = arguments
        sensorId = bundle!!.getSerializable("sensorId") as Int
        val navInitKey = bundle.getSerializable("initNavFrag") as Int

        if(AppData.instance.user.role == "admin") {
            val deleteButton = binding.include.btnDelete
            deleteButton.visibility = View.VISIBLE
            val editButton = binding.include.btnEditSave
            editButton.text = getString(R.string.edit)

            deleteButton.setOnClickListener {
                val altDial = AlertDialog.Builder(requireContext())

                val message = String.format(
                    "%s the %s \"%s\"?\n\n%s: %s!",
                    getString(R.string.qst_delete),
                    sensor.sensortype,
                    sensor.name,
                    getString(R.string.attention),
                    getString(R.string.att_delete_recover)
                )

                altDial.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        deleteButton.isEnabled = false
                        dialog.dismiss()
                        deleteSensor()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            editButton.setOnClickListener {
                findNavController().navigate(
                    SensorViewFragmentDirections.actionSensorViewFragmentToSensorEditFragment(
                        sensor.id!!,
                    navInitKey)
                )
            }
        }
        else {
            binding.include.btnEditSave.visibility = View.GONE
            binding.include.btnDelete.visibility = View.GONE
        }



        val application = requireNotNull(this.activity).application
        val dataSource = SensorDatabase.getInstance(application).sensorDatabaseDao
        val dataSourceRoom = RoomsDatabase.getInstance(application).roomDatabaseDao
        val dataSourceFav = FavouriteDatabase.getInstance(application).favouriteDatabaseDao
        val viewModelFactory = SensorViewModelFactory(
            sensorId,
            dataSourceRoom,
            dataSource,
            dataSourceFav,
            application
        )

        sensorViewModel = viewModelFactory.let {
            ViewModelProvider(this, it).get(
                SensorViewModel::class.java
            )
        }



        sensorViewModel.getSensor().observe(viewLifecycleOwner, {
            if (it != null) {
                sensorViewModel.getSensor().removeObservers(viewLifecycleOwner)
                binding.sensor = it
                sensor = it

                insertInformation()
            }
        })

        AppData.instance.sensorUpdate.observe(viewLifecycleOwner, {
            if (it != 0) {

                sensorViewModel.onStart()
                AppData.instance.sensorUpdate.value = 0
            }
        })


        updateValue()


        val statusSwitch = binding.swActuatorStatus

        statusSwitch.setOnClickListener {
            if (sensor.value == 0.0) sensor.value = 1.0
            else sensor.value = 0.0

            updateSensorValue()

            if (sensor.sensortype == "servo") {
                if (sensor.value == 0.0) sensor.status = getString(R.string.closed)
                else sensor.status = getString(R.string.opened)
            } else {
                if (sensor.value == 0.0) sensor.status = getString(R.string.off)
                else sensor.status = getString(R.string.on)
            }

            binding.tvActuatorStatus.text = sensor.status
        }


        binding.ivSensorFavorite.setOnClickListener {
            changeFavourite()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (navInitKey == 0){
                findNavController().navigate(SensorViewFragmentDirections.actionSensorViewFragmentToNavigationDashboard())
            }

            if (navInitKey == 1){
                findNavController().navigate(SensorViewFragmentDirections.actionSensorViewFragmentToNavigationHouse())
            }
        }

        return binding.root
    }

    private fun changeFavourite() {
        if (fav){
            deleteFavourite(sensorFav)
        }else{
            createFavourite()
        }
    }


    private fun insertInformation() {
        binding.ivSensorIcon.setImageResource(sensor.icon!!)

        if (sensor.actuator != null) {
            sensorViewModel.getActuator(sensor.actuator!!)
            sensorViewModel.getActuator().observe(viewLifecycleOwner, {
                if (it != null){
                    binding.tvSensorActuator.text = it.name
                }else{
                    binding.tvSensorActuator.text = getString(R.string.not_assigned)
                }
            })
        } else {
            binding.tvSensorActuator.text = getString(R.string.not_assigned)
        }


        if (sensor.sensortype == "luminosity" || sensor.sensortype == "temperature") {
            binding.groupSensor.visibility = View.VISIBLE
            binding.groupActuator.visibility = View.GONE

            binding.tvSensorValue.text = sensor.value.toString()
        } else {
            binding.groupActuator.visibility = View.VISIBLE
            binding.groupSensor.visibility = View.GONE

            binding.swActuatorStatus.isChecked = sensor.value != 0.0
            binding.tvActuatorStatus.text = sensor.status
        }

        checkFavourite()
    }


    private fun deleteSensor() {
      val sensorDeleteResponseCall =
            ApiClient.service.deleteSensor(AppData.instance.user.token, sensor.id!!)
        sensorDeleteResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val message = "Sensor deleted with success"
                    showSnack(message)

                    sensor.name?.let { context?.let { it1 -> AppData.instance.deleteEntity(it1,false,it,null, "sensor") } }
                    CoroutineScope(Dispatchers.IO).launch {
                        sensorViewModel.sensorDao.delete(sensor)

                    }
                    findNavController().navigate(SensorViewFragmentDirections.actionSensorViewFragmentToNavigationHouse2())
                } else {
                    binding.include.btnDelete.isEnabled = true
                    val message = "Sensor not deleted"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                binding.include.btnDelete.isEnabled = true
                Log.e("Request failed", t.localizedMessage!!)
                showSnack("Sensor delete failed")
            }
        }))
    }


    private fun updateSensorValue() {
        var result = if (sensor.value == 1.0) {
            "on"
        } else {
            "off"
        }

        val jsn =
            JSONObject("{to:'" + sensor.room.toString() + "','from':'android', 'action':'turn', 'value':'" + result + "'}");
        val send = MqttMessage()
        send.payload = jsn.toString().toByteArray()

        result = if (sensor.sensortype == "servo") {
            if (sensor.value == 1.0) {
                getString(R.string.opened)
            }else getString(R.string.closed)
        } else {
            if (sensor.value == 1.0){
                getString(R.string.on)
            }
            else getString(R.string.off)
        }

        val message = sensor.name + " - " + result

        AppData.instance.mqttClient.publish('/' + sensor.id.toString(), send, null, object :
            IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateValue() {
        AppData.instance.sensorValueUpdate.observe(viewLifecycleOwner, {
            if (it != null) {
                if (::sensor.isInitialized && sensor.id == it.id && it.from != "android" && sensor.value != it.value) {
                    val value = it.value

                    if (sensor.sensortype == "motion" || sensor.sensortype == "plug" || sensor.sensortype == "led" || sensor.sensortype == "camera" || sensor.sensortype == "servo") {
                        if (value == 1.00) {
                            sensor.status = "On"
                            if (sensor.sensortype == "servo") {
                                binding.tvActuatorStatus.text = getString(R.string.opened)
                            } else {
                                binding.tvActuatorStatus.text = getString(R.string.on)
                            }

                            binding.swActuatorStatus.isChecked = true
                        } else {
                            sensor.status = "Off"
                            if (sensor.sensortype == "servo") {
                                binding.tvActuatorStatus.text = getString(R.string.closed)
                            } else {
                                binding.tvActuatorStatus.text = getString(R.string.off)
                            }
                            binding.swActuatorStatus.isChecked = false
                        }
                    }

                    binding.tvSensorValue.text = value.toString()
                    sensor.value = value

                    CoroutineScope(Dispatchers.IO).launch {
                        sensorViewModel.sensorDao.update(sensor)
                    }
                }
            }
        })
    }

    private fun checkFavourite(){
        sensorViewModel.favDao.getFavouriteWithSensorId(sensorId).observe(viewLifecycleOwner,{
            if (it!= null){
                fav = true
                sensorFav = it
                binding.ivSensorFavorite.setImageResource(R.drawable.ic_favourite_solid_24dp)
            }else{
                fav = false
                binding.ivSensorFavorite.setImageResource(R.drawable.ic_not_favourite_border_24dp)
            }
        })
    }

    private fun createFavourite() {
        val favouriteCreateResponseCall = ApiClient.service.insertFavourite(
            AppData.instance.user.token,
            Favourite(null, sensorId, AppData.instance.user.id!!)
        )
        favouriteCreateResponseCall.enqueue((object : Callback<Favourite> {
            override fun onResponse(call: Call<Favourite>, response: Response<Favourite>) {
                if (response.isSuccessful) {
                    val message = "Favourite added with success"

                    val favourite = response.body()
                    CoroutineScope(Dispatchers.IO).launch {
                        favourite?.user?.let {
                            FavouriteDataModel(favourite.id, favourite.sensor, it)
                        }?.let { sensorViewModel.favDao.insert(it) }

                    }
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Favourite>, t: Throwable) {
                Log.e("Request failed", "Favourite failed")
                val message = t.localizedMessage
                if (message!= null){
                    showSnack(message)
                }
            }
        }))

    }

    private fun deleteFavourite(favourite: FavouriteDataModel) {
        val sensorDeleteResponseCall =
            favourite.id?.let { ApiClient.service.deleteFavourite(AppData.instance.user.token, it) }
        sensorDeleteResponseCall?.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val message = "Favourite removed with success"
                    showSnack(message)
                    CoroutineScope(Dispatchers.IO).launch {
                        favourite.user.let {
                            FavouriteDataModel(favourite.id, favourite.sensor, it)
                        }.let { sensorViewModel.favDao.delete(it) }

                    }
                } else {
                    val message = "Favourite not removed"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", t.localizedMessage!!)
                showSnack("Request failed. Remove favourite failed")
            }
        }))
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }

}
