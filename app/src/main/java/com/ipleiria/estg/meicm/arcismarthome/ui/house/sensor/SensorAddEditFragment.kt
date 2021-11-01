package com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.RoomDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.RoomsDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDatabase
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentSensorAddEditBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class SensorAddEditFragment : Fragment() {

    private var toAdd = true
    private lateinit var binding: FragmentSensorAddEditBinding
    private var sensorId: Int = 0
    private lateinit var sensor: SensorDataModel
    private var rooms: ArrayList<RoomDataModel> = ArrayList()
    private lateinit var sensorAddEditViewModel: SensorAddEditViewModel
    private var sensorTypes = mutableListOf<String>()
    private var sensorsOfRoom = LinkedList<SensorDataModel>()
    private var navId: Int = 0
    private var oldName : String? = null


    private var canListen = false

    companion object {
        private const val HINT_TYPE = "Select a type"
        private const val HINT_ROOM = "Select a room"
        private const val NO_ASSIGNED = "Not assigned"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sensor_add_edit, container, false)
        binding.lifecycleOwner = this

        val saveButton = binding.include.btnEditSave
        saveButton.text = getString(R.string.save)

        val bundle = arguments
        sensorId = bundle!!.getSerializable("sensorId") as Int
        navId = bundle.getSerializable("navKey") as Int
        toAdd = sensorId == 0

        val application = requireNotNull(this.activity).application
        val dataSource = SensorDatabase.getInstance(application).sensorDatabaseDao
        val dataSourceRooms = RoomsDatabase.getInstance(application).roomDatabaseDao
        val viewModelFactory =
            SensorAddEditViewModelFactory(sensorId, dataSourceRooms, dataSource, application)

        sensorAddEditViewModel = viewModelFactory.let {
            ViewModelProvider(this, it).get(
                SensorAddEditViewModel::class.java
            )
        }


        binding.viewSensorEdit.setOnClickListener {
            AppData.instance.hideKeyboard(requireContext(), it)
        }

        sensorAddEditViewModel.getSensor().observe(viewLifecycleOwner, {
            if (it != null) {
                sensor = it
                binding.sensor = it

                sensorAddEditViewModel.roomsRepository.roomsDB.observe(viewLifecycleOwner, { itRooms ->
                    if (itRooms != null) {
                        rooms.clear()
                        rooms.addAll(itRooms)

                    }

                    binding.spinSensorType.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                ) {
                                    val selectedType = binding.spinSensorType.selectedItem.toString()
                                            .toLowerCase(Locale.ROOT)
                                    sensor.icon = resources.getIdentifier(
                                            "ic_" + selectedType + "_border",
                                            "drawable",
                                            context!!.packageName
                                    )
                                    if (it.icon != null) {
                                        if (it.icon != 0) {
                                            binding.ivSensorIcon.setImageResource(sensor.icon!!)
                                        }
                                    }


                                    when (binding.spinSensorType.selectedItem.toString()
                                            .toLowerCase(Locale.ROOT)) {
                                        "luminosity" -> {
                                            if (sensor.lux_lim == null) binding.etSensorTempLum.setText("")
                                            else binding.etSensorTempLum.setText(sensor.lux_lim.toString())

                                            binding.groupLimits.visibility = View.VISIBLE
                                            binding.lbTempLum.text =
                                                    getString(R.string.luminosity_two_points)
                                            binding.lbUnity.text = getString(R.string.lb_percentage)
                                        }
                                        "temperature" -> {
                                            if (sensor.temp_lim == null) binding.etSensorTempLum.setText("")
                                            else binding.etSensorTempLum.setText(sensor.temp_lim.toString())

                                            binding.groupLimits.visibility = View.VISIBLE
                                            binding.lbTempLum.text =
                                                    getString(R.string.temperature_two_points)
                                            binding.lbUnity.text = getString(R.string.lb_celsius)
                                        }
                                        else -> {
                                            binding.groupLimits.visibility = View.GONE
                                        }
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }

                    binding.spinSensorRoom.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                ) {
                                    val room =
                                            binding.spinSensorRoom.getItemAtPosition(position) as RoomDataModel
                                    room.id?.let { it1 -> sensorAddEditViewModel.getSensorsOfRoom(it1) }

                                    sensorAddEditViewModel.getSensorsOfRoom()
                                            .observe(viewLifecycleOwner, { itSensors ->
                                                sensorsOfRoom.clear()

                                                var actuator = SensorDataModel(
                                                        null,
                                                        NO_ASSIGNED,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        null
                                                )
                                                sensorsOfRoom.add(0, actuator)

                                                if (itSensors.isNotEmpty()) {
                                                    for (sensorOfRoom in itSensors) {
                                                        if (sensorOfRoom.id != sensorId) {
                                                            sensorsOfRoom.add(sensorOfRoom)

                                                            if (sensorOfRoom.id == sensor.actuator) {
                                                                actuator = sensorOfRoom
                                                            }
                                                        }
                                                    }
                                                }

                                                val actuatorSpinner = binding.spinSensorActuator
                                                val actuatorAdapter = ArrayAdapter(
                                                        requireContext(),
                                                        R.layout.spiner_list,
                                                        sensorsOfRoom
                                                )
                                                actuatorSpinner.adapter = actuatorAdapter

                                                binding.spinSensorActuator.setSelection(
                                                        actuatorAdapter.getPosition(
                                                                actuator
                                                        )
                                                )
                                            })
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }

                    runBlocking {
                        getSensorTypes()
                    }

                    binding.groupLoading.visibility = View.GONE
                    binding.groupRest.visibility = View.VISIBLE
                    doWhat()

                    binding.spinSensorActuator.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                ) {
                                    if (position == 0) {
                                        binding.cbAuto.visibility = View.GONE
                                        binding.lbAuto.visibility = View.GONE
                                    } else {
                                        binding.cbAuto.visibility = View.VISIBLE
                                        binding.lbAuto.visibility = View.VISIBLE
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }

                    saveButton.setOnClickListener {
                        if (allFilled().first) {
                            if (toAdd) {
                                saveButton.isEnabled = false
                                fillManaged()
                                createSensor()
                            } else if (wasEdited()) {
                                saveButton.isEnabled = false
                                fillManaged()
                                updateSensor()
                            } else {
                                if (sensor.id != null) {
                                    saveButton.isEnabled = false
                                    findNavController().navigate(
                                            SensorAddEditFragmentDirections.actionSensorEditFragmentToSensorViewFragment(
                                                    sensor.id!!, navId)
                                    )
                                }
                            }
                        } else showSnack(allFilled().second)
                    }

                    canListen = true
                })
            }
        })

        return binding.root
    }


    private suspend fun getSensorTypes() {
        try {
            withContext(Dispatchers.IO) {
                val sensorTypesResponse =
                    async { ApiClient.service.getSensorType(AppData.instance.user.token) }

                val data = JSONObject(sensorTypesResponse.await().toString())
                val dataJSONArray: JSONArray = data["data"] as JSONArray
                sensorTypes.clear()
                for (i in 0 until dataJSONArray.length()) {
                    sensorTypes.add(dataJSONArray[i].toString().capitalize(Locale.ROOT))
                }
            }
        } catch (t: Throwable) {
            Log.e("Request failed", t.localizedMessage!!.toString())
        }
    }


    private fun doWhat() {
        val typeSpinner = binding.spinSensorType
        val typeAdapter: ArrayAdapter<String>
        sensorTypes.add(0, HINT_TYPE)
        typeAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spiner_list,
            sensorTypes
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.themeColor))
                }

                return view
            }
        }
        typeSpinner.adapter = typeAdapter

        val roomSpinner = binding.spinSensorRoom
        val roomAdapter: ArrayAdapter<RoomDataModel>
        rooms.add(0, RoomDataModel(0, HINT_ROOM, 0, "", "", false))
        roomAdapter = object : ArrayAdapter<RoomDataModel>(
            requireContext(),
            R.layout.spiner_list,
            rooms
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.themeColor))
                }

                return view
            }
        }
        roomSpinner.adapter = roomAdapter

        if (toAdd) {
            binding.tvTitleDevice.text = getString(R.string.create_device)

            typeSpinner.setSelection(-1)
            roomSpinner.setSelection(-1)
        }
        else {
            binding.tvTitleDevice.text = getString(R.string.edit_device)

            insertInformation(typeAdapter, roomAdapter)
        }
    }


    private fun insertInformation(
        typeAdapter: ArrayAdapter<String>,
        roomAdapter: ArrayAdapter<RoomDataModel>
    ) {

        binding.ivSensorIcon.setImageResource(sensor.icon!!)
        binding.etSensorName.setText(sensor.name)
        binding.spinSensorType.setSelection(
            typeAdapter.getPosition(
                sensor.sensortype!!.capitalize(
                    Locale.ROOT
                )
            )
        )

        for (room in rooms) {
            if (this.sensor.room == room.id) {
                binding.spinSensorRoom.setSelection(roomAdapter.getPosition(room))

                break
            }
        }
    }


    private fun allFilled(): Pair<Boolean,String> {
        var allFilled = true
        var msg = ""

        if (binding.etSensorName.text.isEmpty() || binding.spinSensorType.selectedItemPosition == 0 || binding.spinSensorRoom.selectedItemPosition == 0 || binding.etSensorGpio.text.isEmpty()) {
            allFilled = false
            msg = appendMsg(msg,"Please fill all inputs")
        }

        if ((binding.spinSensorType.selectedItem == "Temperature" || binding.spinSensorType.selectedItem == "Luminosity") && binding.etSensorTempLum.text.isEmpty()) {
            allFilled = false
            msg = appendMsg(msg,"Limit value is empty")
        }

        if (binding.etSensorGpio.text.isNotEmpty()) {
            if (binding.etSensorGpio.text.toString().length < 3){

                    if (binding.etSensorGpio.text.toString().toInt() < 1 || binding.etSensorGpio.text.toString().toInt() > 40
                    ) {
                        allFilled = false
                        msg = appendMsg(msg,"GPIO value must be between 1 and 40")
                    }
            }else{
                allFilled = false
                msg = appendMsg(msg,"GPIO value must be between 1 and 40")
            }
        }

        return Pair(allFilled,msg)
    }

    private fun appendMsg(fullMessage: String, appendText: String ): String {
        val and = " and "
        var msg = fullMessage
        if (msg.isEmpty()){
            msg += appendText.capitalize(Locale.ROOT)
        }else{
            msg+=and
            msg+=appendText
        }
        return msg
    }

    private fun wasEdited(): Boolean {
        return sensor.name != binding.etSensorName.text.toString() ||
                sensor.sensortype != binding.spinSensorType.getItemAtPosition(binding.spinSensorType.selectedItemPosition)
            .toString().toLowerCase(Locale.ROOT) ||
                sensor.room != (binding.spinSensorRoom.getItemAtPosition(binding.spinSensorRoom.selectedItemPosition) as RoomDataModel).id ||
                sensor.gpio.toString() != binding.etSensorGpio.text.toString() ||
                sensor.actuator != (binding.spinSensorActuator.selectedItem as SensorDataModel).id ||
                ((sensor.sensortype == "temperature" && sensor.temp_lim.toString() != binding.etSensorTempLum.text.toString())
                        || ((sensor.sensortype == "luminosity" && sensor.lux_lim.toString() != binding.etSensorTempLum.text.toString()))) || sensor.auto != binding.cbAuto.isChecked
    }

    private fun fillManaged() {
        oldName = sensor.name
        sensor.name = binding.etSensorName.text.toString()
        sensor.room = (binding.spinSensorRoom.selectedItem as RoomDataModel).id
        sensor.sensortype = binding.spinSensorType.selectedItem.toString().toLowerCase(Locale.ROOT)
        sensor.gpio = binding.etSensorGpio.text.toString().toInt()
        sensor.roomname = (binding.spinSensorRoom.selectedItem as RoomDataModel).name
        if (binding.spinSensorType.selectedItem == "Temperature") sensor.temp_lim =
            binding.etSensorTempLum.text.toString().toDouble()
        else sensor.temp_lim = 0.0

        if (binding.spinSensorType.selectedItem == "Luminosity") sensor.lux_lim =
            binding.etSensorTempLum.text.toString().toDouble()
        else sensor.lux_lim = 0.0

        if ((binding.spinSensorActuator.selectedItem as SensorDataModel).name == NO_ASSIGNED) {
            sensor.actuator = 0
        } else {
            sensor.actuator = (binding.spinSensorActuator.selectedItem as SensorDataModel).id
        }

        sensor.value = 0.0
        if (sensor.sensortype == "servo"){

        sensor.status = "Closed"
        }else if (sensor.sensortype == "luminosity" || sensor.sensortype == "temperature"){
            sensor.status = "Ok"
        }else{
            sensor.status = "Off"
        }

        sensor.auto = binding.cbAuto.isChecked
    }


    private fun createSensor() {
        var actuatorAux: String? = ""
        if (sensor.actuator != 0) {
            actuatorAux = sensor.actuator.toString()
        }

        val sensorToSend = Sensor.SensorRequest(
            sensor.id,
            sensor.name,
            sensor.sensortype,
            0.0,
            sensor.gpio,
            false,
            sensor.room,
            sensor.roomname,
            actuatorAux,
            sensor.icon,
            sensor.status,
            sensor.temp_lim,
            sensor.lux_lim,
            sensor.auto
        )

        val sensorCreateResponseCall =
            ApiClient.service.createSensor(AppData.instance.user.token, sensorToSend)
        sensorCreateResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    sensorToSend.name?.let { context?.let { it1 -> AppData.instance.createEntity(it1,it, "sensor") } }
                    val message = "Sensor created with success"
                    showSnack(message)
                    AppData.instance.sensorUpdate.value = 0


                    CoroutineScope(Dispatchers.IO).launch {
                        sensorAddEditViewModel.sensorDataSource.insert(sensor)
                    }
                    findNavController().navigate(SensorAddEditFragmentDirections.actionSensorAddEditFragmentToNavigationHouse())

                } else {
                    binding.include.btnEditSave.isEnabled = true
                    val message = "Sensor not created"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", "Create Sensor failed")
                binding.include.btnEditSave.isEnabled = true
                val message = t.localizedMessage
                if ( message != null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun updateSensor() {
        var actuatorAux: String? = ""
        if (sensor.actuator != 0) {
            actuatorAux = sensor.actuator.toString()
        }

        val sensorToSend = Sensor.SensorRequest(
            sensor.id,
            sensor.name,
            sensor.sensortype,
            sensor.value,
            sensor.gpio,
            false,
            sensor.room,
            sensor.roomname,
            actuatorAux,
            sensor.icon,
            sensor.status,
            sensor.temp_lim,
            sensor.lux_lim,
            sensor.auto
        )

        val sensorUpdateResponseCall =
            ApiClient.service.updateSensor(AppData.instance.user.token, sensor.id!!, sensorToSend)
        sensorUpdateResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val message = "Sensor updated with success"
                    showSnack(message)
                    sensorToSend.name?.let { context?.let { it1 -> AppData.instance.deleteEntity(it1,true,it,oldName, "sensor") } }
                    CoroutineScope(Dispatchers.IO).launch {
                        sensorAddEditViewModel.sensorDataSource.update(sensor)
                    }
                    AppData.instance.sensorUpdate.value = 0
                    findNavController().navigate(
                        SensorAddEditFragmentDirections.actionSensorEditFragmentToSensorViewFragment(
                            sensor.id!!
                        ,navId)
                    )
                } else {
                    val message = "Sensor not updated"
                    binding.include.btnEditSave.isEnabled = true
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", "Update Sensor failed")
                binding.include.btnEditSave.isEnabled = true
                val message = t.localizedMessage
                if (message!=null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG)
        snack.setAction("Dismiss") { snack.dismiss() }
        snack.show()
    }
}