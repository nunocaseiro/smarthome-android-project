package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDatabase
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentVehiclesBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData


class VehiclesFragment : Fragment() {

    private lateinit var binding: FragmentVehiclesBinding
    lateinit var adapter: VehiclesAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vehicles, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = VehicleDatabase.getInstance(application).vehicleDatabaseDao
        val viewModelFactory = VehicleViewModelFactory(dataSource, application)

        val vehicleViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(VehicleViewModel::class.java)

        binding.vehicleViewModel = vehicleViewModel

        adapter = VehiclesAdapter(VehiclesAdapter.VehicleListener { vehicleId ->
            vehicleViewModel.onVehicleClicked(vehicleId)
        })

        binding.vehicleList.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        );

        binding.vehicleList.adapter = adapter




        if (AppData.instance.user.role == "admin") {
            binding.addVehicle.visibility = View.VISIBLE

            binding.addVehicle.setOnClickListener { navigateToDetail() }
        } else binding.addVehicle.visibility = View.GONE


        vehicleViewModel.onStart()

        vehicleViewModel.vehiclesDB.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isEmpty()) binding.lbNoVehicles.visibility = View.VISIBLE
                else binding.lbNoVehicles.visibility = View.GONE

                adapter.submitList(it)

            }
        })

        vehicleViewModel.navigateToVehicleDetail.observe(viewLifecycleOwner, { vehicle ->
            vehicle?.let {
                if (AppData.instance.user.roleGroup != 2){
                    this.findNavController().navigate(
                        VehiclesFragmentDirections
                            .actionNavigationVehiclesToVehicleDetail(vehicle)
                    )
                    vehicleViewModel.onVehicleDetailNavigated()
                }
            }
        })

        binding.lifecycleOwner = this

        return binding.root
    }

    private fun navigateToDetail() {
        this.findNavController().navigate(
            VehiclesFragmentDirections
                .actionNavigationVehiclesToVehicleDetail(0)
        )
    }
}