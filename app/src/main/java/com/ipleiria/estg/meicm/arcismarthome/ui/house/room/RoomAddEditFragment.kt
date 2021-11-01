package com.ipleiria.estg.meicm.arcismarthome.ui.house.room

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentRoomAddEditBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class RoomAddEditFragment : Fragment() {

    private var toAdd = true
    private lateinit var binding: FragmentRoomAddEditBinding
    private var roomId: Int = 0
    private lateinit var room: RoomDataModel
    private lateinit var roomAddEditViewModel: RoomAddEditViewModel
    private var oldName : String? = null

    companion object {
        private const val HINT_TYPE = "Select a type"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_room_add_edit, container, false)
        binding.lifecycleOwner = this

        val bundle = arguments
        roomId = bundle!!.getSerializable("roomId") as Int

        toAdd = roomId == 0

        val application = requireNotNull(this.activity).application
        val dataSource = RoomsDatabase.getInstance(application).roomDatabaseDao
        val viewModelFactory = RoomAddEditViewModelFactory(roomId, dataSource)

        roomAddEditViewModel = viewModelFactory.let {
            ViewModelProvider(this, it).get(
                RoomAddEditViewModel::class.java
            )
        }
        binding.viewRoomEdit.setOnClickListener{
            AppData.instance.hideKeyboard(requireContext(), it)
        }

        roomAddEditViewModel.getRoom().observe(viewLifecycleOwner, {
            room = it

            doWhat()

            if (!toAdd) {
                binding.tvTitleRoom.text = getString(R.string.edit_room)

                val deleteButton = binding.include.btnDelete
                deleteButton.visibility = View.VISIBLE

                deleteButton.setOnClickListener {
                    val altDial = AlertDialog.Builder(requireContext())

                    val message = String.format("%s the %s \"%s\"?\n\n%s: %s!\n%s!",getString(R.string.qst_delete),room.roomtype!!.toLowerCase(Locale.ROOT),room.name,getString(R.string.attention),getString(R.string.att_room_delete),getString(R.string.att_delete_recover))

                    altDial.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes) { dialog, _ ->
                            deleteButton.isEnabled = false
                            dialog.dismiss()
                            deleteRoom()
                        }
                        .setNegativeButton(R.string.no) { dialog, _ ->
                            deleteButton.isEnabled = true
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            else binding.tvTitleRoom.text = getString(R.string.create_room)

            val saveButton = binding.include.btnEditSave
            saveButton.text = getString(R.string.save)

            saveButton.setOnClickListener {
                if (allFilled()) {
                    saveButton.isEnabled = false
                    when {
                        toAdd -> {
                            fillManaged()
                            createRoom()
                        }
                        wasEdited() -> {
                            fillManaged()
                            updateRoom()
                        }
                        else -> {
                            findNavController().navigate(RoomAddEditFragmentDirections.actionRoomAddEditFragmentToNavigationHouse())
                        }
                    }
                } else showSnack(getString(R.string.tst_fill_the_inputs))

            }
        })


        return binding.root
    }

    private fun doWhat() {
        val typeSpinner = binding.spinRoomType
        val typesNames = (resources.getStringArray(R.array.room_types)).toMutableList()
        val typeAdapter: ArrayAdapter<String>
        typesNames.add(0, HINT_TYPE)
        typeAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spiner_list,
            typesNames
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

        if (toAdd) {
            typeSpinner.setSelection(-1)
        } else {
            insertInformation(typeAdapter)
        }
    }

    private fun insertInformation(typeAdapter: ArrayAdapter<String>) {
        binding.etRoomName.setText(room.name)
        if (room.roomtype == "living"){
            binding.spinRoomType.setSelection(typeAdapter.getPosition("Living Room"))
        }else{

        binding.spinRoomType.setSelection(typeAdapter.getPosition(room.roomtype.toString().capitalize()))
        }
    }

    private fun allFilled(): Boolean {
        return binding.etRoomName.text.toString() != "" &&
                binding.spinRoomType.selectedItem != HINT_TYPE
    }

    private fun wasEdited(): Boolean {

        return room.name != binding.etRoomName.text.toString() ||
                room.roomtype != binding.spinRoomType.selectedItem.toString()
            .toLowerCase(Locale.ROOT)
    }

    private fun fillManaged() {
        oldName = room.name
        room.name = binding.etRoomName.text.toString()
        room.roomtype =
            binding.spinRoomType.selectedItem.toString().split(" ")[0].toLowerCase(Locale.ROOT)

        room.home = AppData.instance.home.id
        room.ip = "0.0.0.0"
    }


    private fun deleteRoom() {
       val roomDeleteResponseCall =
            ApiClient.service.deleteRoom(AppData.instance.user.token, room.id!!)
        roomDeleteResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    room.name?.let { context?.let { it1 -> AppData.instance.deleteEntity(it1,false, it, oldName, "room") } }
                    val message = "Room deleted with success"
                    showSnack(message)
                    findNavController().navigate(RoomAddEditFragmentDirections.actionRoomAddEditFragmentToNavigationHouse())

                } else {
                    binding.include.btnDelete.isEnabled = true
                    val message = "Room not deleted"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                binding.include.btnDelete.isEnabled = true
                val message = "Request failed. Deleted room unsuccessfully"
                showSnack(message)

            }
        }))
    }

    private fun createRoom() {
        val roomCreateResponseCall = ApiClient.service.createRoom(AppData.instance.user.token, room)
        roomCreateResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val message = "Room created with success"
                    showSnack(message)

                    room.name?.let { context?.let { it1 -> AppData.instance.createEntity(it1,it, "room") } }
                    findNavController().navigate(RoomAddEditFragmentDirections.actionRoomAddEditFragmentToNavigationHouse())
                } else {
                    binding.include.btnEditSave.isEnabled = true
                    val message = "Room not created"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                binding.include.btnEditSave.isEnabled = true
                val message = t.localizedMessage
                if (message!=null){
                showSnack(message)
                }
            }
        }))
    }


    private fun updateRoom() {

        val roomUpdateResponseCall =
            ApiClient.service.updateRoom(AppData.instance.user.token, room.id!!, room)
        roomUpdateResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val message = "Room updated with success"
                    showSnack(message)
                    room.name?.let { context?.let { it1 -> AppData.instance.deleteEntity(it1,true,it,oldName, "room") } }

                    findNavController().navigate(RoomAddEditFragmentDirections.actionRoomAddEditFragmentToNavigationHouse())
                } else {
                    binding.include.btnEditSave.isEnabled = true
                    val message = "Room not updated"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                binding.include.btnEditSave.isEnabled = true
                Log.e("Request failed", "Update Room failed")
                val message = t.localizedMessage
                if (message!=null){

                    showSnack(message)
                }
            }
        }))
    }


    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }
}