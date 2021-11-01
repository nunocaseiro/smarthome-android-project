package com.ipleiria.estg.meicm.arcismarthome.ui.house

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.*
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentHouseBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Room
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HouseFragment : Fragment() {

    lateinit var binding: FragmentHouseBinding
    lateinit var houseViewModel: HouseViewModel

    private var listOfRooms: List<Room> = ArrayList()
    private var listOfSensors: HashMap<Room, LinkedList<Sensor>> = HashMap()
    private var expandableListAdapter: ExpandableListAdapter? = null
    private var lastExpandedPosition = -1
    private var listSensors: List<Sensor> = ArrayList()
    private lateinit var selectedRoom: Room
    private lateinit var selectedSensor: Sensor

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.add_rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.add_rotate_close_anim
        )
    }
    private val fromRight: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.add_from_right_anim
        )
    }
    private val toRight: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.add_to_right_anim
        )
    }
    private var clicked = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_house, container, false)

        val application = requireNotNull(this.activity).application
        val dataSourceSensor = SensorDatabase.getInstance(application).sensorDatabaseDao
        val dataSourceRoom = RoomsDatabase.getInstance(application).roomDatabaseDao

        val viewModelFactory = HouseViewModelFactory(dataSourceRoom, dataSourceSensor, application)

        houseViewModel =
            ViewModelProvider(this, viewModelFactory).get(HouseViewModel::class.java)

        houseViewModel.getRooms()
        houseViewModel.getSensors()

        binding.houseViewModel = houseViewModel

        houseViewModel.rooms.observe(viewLifecycleOwner, {
            listOfRooms = it

            if (listOfRooms.isNotEmpty()) binding.lbNoRooms.visibility = View.GONE
            else binding.lbNoRooms.visibility = View.VISIBLE

            expandableListAdapter =
                ExpandableListAdapter(binding.root.context, listOfRooms, listOfSensors, binding)

            binding.expandableList.setAdapter(expandableListAdapter)

            houseViewModel.afterRoomsDownloaded()
        })



        houseViewModel.roomsDone.observe(viewLifecycleOwner, { roomsOk ->
            if (roomsOk == 1) {

                houseViewModel.sensors.observe(viewLifecycleOwner, { sensors ->

                    listSensors = sensors
                    listOfSensors.clear()


                    for (i in listOfRooms) {
                        listOfSensors[i] = LinkedList()
                        for (j in sensors) {
                            if (j.room == i.id) {
                                listOfSensors[i]?.add(j)
                            }
                        }
                    }

                    binding.expandableList.setOnGroupExpandListener { groupPosition ->
                        /*
                        if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                          binding.expandableRoomName.collapseGroup(lastExpandedPosition)
                        }
                        */

                        lastExpandedPosition = groupPosition

                        selectedRoom = listOfRooms[groupPosition]

                    }

                    binding.expandableList.setOnChildClickListener { _, _, _, childPosition, _ ->
                        selectedSensor = listOfSensors[selectedRoom]!![childPosition]

                        findNavController().navigate(
                            HouseFragmentDirections.actionNavigationHouseToSensorViewFragment(
                                selectedSensor.id!!
                            ,1)
                        )

                        true
                    }

                    AppData.instance.sensorUpdate.observe(viewLifecycleOwner, {
                        if (it != 0) {
                            houseViewModel.getSensors()
                            AppData.instance.sensorUpdate.value = 0
                            expandableListAdapter!!.notifyDataSetChanged()
                        }
                    })
                })
                houseViewModel.afterSensors()
            }
        })

        AppData.instance.sensorValueUpdate.observe(viewLifecycleOwner, { itSensorValue ->
            if (itSensorValue != null) {

                if (itSensorValue.from != "android") {

                    val id = itSensorValue.id
                    val value = itSensorValue.value
                    Log.d("VALUE", id.toString())

                    //falta meter aqui para mudar o status da mesma forma que no fragmento detalhes

                    for (it in listSensors) {
                        if (it.id == id) {
                            if (value != it.value) {
                                if (it.sensortype == "motion" || it.sensortype == "led" || it.sensortype == "plug" || it.sensortype == "led" || it.sensortype == "camera" || it.sensortype == "servo") {
                                    if (value == 1.00) {
                                        it.status = "On"
                                    } else {
                                        it.status = "Off"
                                    }
                                }
                                it.value = value
                                CoroutineScope(Dispatchers.IO).launch {
                                    val s = SensorDataModel(
                                        it.id,
                                        it.name,
                                        it.sensortype,
                                        it.value,
                                        it.gpio,
                                        it.room,
                                        it.actuator,
                                        it.icon,
                                        it.status,
                                        it.auto,
                                        it.temp_lim,
                                        it.lux_lim,
                                        it.roomname
                                    )
                                    houseViewModel.dataSourceS.update(s)
                                }
                                expandableListAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        })


        if (AppData.instance.user.role == "admin") {
            binding.btnAdd.visibility = View.VISIBLE

            binding.btnAdd.setOnClickListener {
                if (!clicked) {
                    binding.btnRoom.visibility = View.VISIBLE
                    binding.btnSA.visibility = View.VISIBLE

                    binding.btnRoom.startAnimation(fromRight)
                    binding.btnSA.startAnimation(fromRight)
                    binding.btnAdd.startAnimation(rotateOpen)

                    binding.btnRoom.isClickable = true
                    binding.btnSA.isClickable = true
                } else {
                    binding.btnRoom.visibility = View.INVISIBLE
                    binding.btnSA.visibility = View.INVISIBLE

                    binding.btnRoom.startAnimation(toRight)
                    binding.btnSA.startAnimation(toRight)
                    binding.btnAdd.startAnimation(rotateClose)

                    binding.btnRoom.isClickable = false
                    binding.btnSA.isClickable = false
                }

                clicked = !clicked
            }

            binding.btnRoom.setOnClickListener {
                findNavController().navigate(
                    HouseFragmentDirections.actionNavigationHouseToRoomAddEditFragment(
                        0
                    )
                )
            }

            binding.btnSA.setOnClickListener {
                findNavController().navigate(
                    HouseFragmentDirections.actionNavigationHouseToSensorAddEditFragment(0,0)
                )
            }
        } else binding.btnAdd.visibility = View.GONE



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
           findNavController().navigate(HouseFragmentDirections.actionNavigationHouseToNavigationDashboard())
        }


        expandableListAdapter =
            ExpandableListAdapter(binding.root.context, listOfRooms, listOfSensors, binding)

        binding.expandableList.setAdapter(expandableListAdapter)

        binding.lifecycleOwner = this

        return binding.root
    }
}
