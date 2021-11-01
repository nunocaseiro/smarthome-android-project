package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles.vehicleDetail

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabase
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentVehicleDetailBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Vehicle
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.NetworkVehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VehicleDetail : Fragment() {


    lateinit var binding: FragmentVehicleDetailBinding
    lateinit var vehicleDetailViewModel: VehicleDetailViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_vehicle_detail, container, false)

        val application = requireNotNull(this.activity).application

        val arguments = arguments?.let { VehicleDetailArgs.fromBundle(it) }

        val key = arguments?.vehicleKey

        val deleteButton = binding.include.btnDelete
        val editSaveButton = binding.include.btnEditSave


        editSaveButton.text = getString(R.string.save)
        if (key == 0) {
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
        }


        val dataSource = VehicleDatabase.getInstance(application).vehicleDatabaseDao
        val viewModelFactory =
            arguments?.let { VehicleDetailViewModelFactory(it.vehicleKey, dataSource) }

        vehicleDetailViewModel = viewModelFactory?.let {
            ViewModelProvider(
                this,
                it
            ).get(VehicleDetailViewModel::class.java)
        }!!

        binding.vehicleDetailViewModel = vehicleDetailViewModel

        binding.lifecycleOwner = this

        vehicleDetailViewModel.navigateToVehicleListAfterSave.observe(
            viewLifecycleOwner,
            {
                if (it != null && it > 0) {
                    val vold: VehicleDataModel? = vehicleDetailViewModel.getVehicle()?.value

                    if (validInputs()) {
                        if (AppData.instance.home.id != null) {
                            val v = Vehicle(
                                binding.editTextBrand.text.toString(),
                                binding.editTextLicense.text.toString(),
                                binding.editTextModel.text.toString(),
                                binding.editTextYear.text.toString().toInt(),
                                AppData.instance.home.id!!
                            )

                            if (it == 1) {
                                insertVehicle(v)
                            }

                            if (it == 2) { // Observed state is true.
                                v.id = vold?.id!!
                                updateVehicle(v)
                            }
                        }
                    }else{
                        showSnackMaxLines("Please, insert valid data. License plate must have 6 chars. Year must be between 1950 and 2050",5, 6000)
                        editSaveButton.isEnabled = true
                    }

                    if (it == 3) {
                        if (vold != null) {
                            deleteVehicle(vold.id)
                        }
                    }


                }
            })

        binding.frameLayout.setOnClickListener {
            AppData.instance.hideKeyboard(requireContext(), it)
        }

        deleteButton.setOnClickListener {
            val altDial = AlertDialog.Builder(requireContext())

            altDial.setMessage(
                String.format(
                    "Do you want delete this vehicle?"
                )
            )
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    dialog.dismiss()
                    deleteButton.isEnabled =false
                    vehicleDetailViewModel.onDelete()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        editSaveButton.setOnClickListener {
            editSaveButton.isEnabled = false
            vehicleDetailViewModel.onSave()

        }



        return binding.root
    }

    private fun validInputs(): Boolean {
        if (binding.editTextBrand.text.isEmpty()) {
            return false
        }

        if (binding.editTextLicense.text.isEmpty()) {
            return false
        }

        if (binding.editTextModel.text.isEmpty()) {
            return false
        }

        if (binding.editTextYear.text.isEmpty()) {
            return false
        }

        if (binding.editTextLicense.text.length < 6 || binding.editTextLicense.text.length > 6 ){
            return false
        }

        if (binding.editTextYear.text.toString().toInt() < 1950 || binding.editTextYear.text.toString().toInt() > 2050 ) {
            return false
        }

        return true
    }

    private fun updateVehicle(vehicle: Vehicle) {
        val vehicleResponseCall = ApiClient.service.updateVehicle(
            AppData.instance.user.token, vehicle.id, vehicle
        )
        vehicleResponseCall.enqueue((object : Callback<NetworkVehicle> {
            override fun onResponse(
                call: Call<NetworkVehicle>,
                response: Response<NetworkVehicle>
            ) {
                if (response.isSuccessful) {

                    findNavController().navigate(
                        VehicleDetailDirections.actionVehicleDetailToNavigationVehicles()
                    )

                    vehicleDetailViewModel.doneNavigating()

                }else{
                    showSnack("Vehicle not updated")
                }
            }

            override fun onFailure(call: Call<NetworkVehicle>, t: Throwable) {
                Log.e("Request failed", "failed getting vehicle put response")
                val message = t.localizedMessage
                if (message!= null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun insertVehicle(vehicle: Vehicle) {
        val vehicleResponseCall = ApiClient.service.insertVehicle(
            AppData.instance.user.token, vehicle
        )
        vehicleResponseCall.enqueue((object : Callback<NetworkVehicle> {
            override fun onResponse(
                call: Call<NetworkVehicle>,
                response: Response<NetworkVehicle>
            ) {
                if (response.isSuccessful) {
                    showSnack("Vehicle added")
                    findNavController().navigate(
                        VehicleDetailDirections.actionVehicleDetailToNavigationVehicles()
                    )

                    vehicleDetailViewModel.doneNavigating()
                }
            }

            override fun onFailure(call: Call<NetworkVehicle>, t: Throwable) {
                Log.e("Request failed", "failed getting vehicle put response")
                val message = t.localizedMessage
                if (message!= null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun deleteVehicle(vehicleId: Int) {
        val vehicleResponseCall =
            ApiClient.service.deleteVehicle(AppData.instance.user.token, vehicleId)
        vehicleResponseCall.enqueue((object : Callback<NetworkVehicle> {
            override fun onResponse(
                call: Call<NetworkVehicle>,
                response: Response<NetworkVehicle>
            ) {
                if (response.isSuccessful) {

                    showSnack("Vehicle deleted")
                    findNavController().navigate(
                        VehicleDetailDirections.actionVehicleDetailToNavigationVehicles()
                    )

                    vehicleDetailViewModel.doneNavigating()
                }
            }

            override fun onFailure(call: Call<NetworkVehicle>, t: Throwable) {
                Log.e("Request failed", "failed deleting vehicle response")
                val message = t.localizedMessage
                if (message!= null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }

    private fun showSnackMaxLines(message: String, lines: Int, duration: Int){
        val snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),message,duration)
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = lines
        snackbar.setAction("Dismiss") { snackbar.dismiss() }
        snackbar.show()
    }

}