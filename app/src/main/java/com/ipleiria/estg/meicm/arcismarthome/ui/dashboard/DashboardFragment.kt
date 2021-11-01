package com.ipleiria.estg.meicm.arcismarthome.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.MainActivity
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDatabase
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentDashboardBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.StatisticsResponse
import com.ipleiria.estg.meicm.arcismarthome.ui.house.sensor.SensorViewFragmentDirections
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private var sensorsList: MutableList<Sensor> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_dashboard,
            container,
            false
        )

        binding.lbFirstname.text = AppData.instance.user.firstName

        binding.ivArCamera.setOnClickListener {
            (activity as MainActivity).openActivityCamera()
        }

        configStatisticsRecyclerView()

        val application = requireNotNull(this.activity).application

        val viewModelFactory =
            DashboardViewModelFactory(application)

        val dashboardViewModel =
            ViewModelProvider(this, viewModelFactory).get(DashboardViewModel::class.java)

        binding.dashboardViewModel = dashboardViewModel

        binding.lifecycleOwner = this

        val adapterFav = FavouritesAdapter(FavouriteListener { sensorId ->
            dashboardViewModel.onSensorClicked(sensorId)
        })
        binding.favouritesRv.adapter = adapterFav
        val manager = GridLayoutManager(activity, 3)
        binding.favouritesRv.layoutManager = manager
        binding.lbFirstname.text = AppData.instance.user.firstName

        dashboardViewModel.sensors.observe(viewLifecycleOwner, { sensors ->
            if (sensors != null) {
                if (sensors.isNotEmpty()) {
                    dashboardViewModel.favourites.observe(
                        viewLifecycleOwner,
                        { favs ->
                            if (favs != null) {
                                if (favs.isEmpty()) {
                                    binding.favouritesRv.visibility = View.GONE
                                    binding.lbNoFavorites.visibility = View.VISIBLE
                                } else {
                                    binding.favouritesRv.visibility = View.VISIBLE
                                    binding.lbNoFavorites.visibility = View.GONE
                                }
                                sensorsList.clear()
                                for (fav in favs) {
                                    for (sensor in sensors) {
                                        if (sensor.id == fav.sensor) {
                                            sensorsList.add(sensor)
                                            break
                                        }
                                    }
                                }
                                adapterFav.submitList(sensorsList)
                                adapterFav.notifyDataSetChanged()
                            }

                        })
                }
            }
        })

        dashboardViewModel.getSensorClickedId.observe(viewLifecycleOwner, {
            if (it != null) {
                findNavController().navigate(
                    DashboardFragmentDirections.actionNavigationDashboardToSensorViewFragment(it, 0)
                )

                dashboardViewModel.onFavouriteDetailNavigated()
            }

        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val altDial = AlertDialog.Builder(requireContext())

            val message = "Are you sure you want to exit?"

            altDial.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        activity?.finishAffinity()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }


        return binding.root
    }

    private fun configStatisticsRecyclerView() {
        if (AppData.instance.home.id != null) {
            val statisticsResponseCall = ApiClient.service.getStatistics(
                AppData.instance.user.token,
                AppData.instance.home.id!!
            )
            statisticsResponseCall.enqueue((object : Callback<StatisticsResponse> {
                override fun onResponse(
                    call: Call<StatisticsResponse>,
                    response: Response<StatisticsResponse>
                ) {
                    if (response.isSuccessful) {
                        val statisticsResponse = response.body()

                        if (statisticsResponse != null) {
                            binding.lbNumRoomsData.text = statisticsResponse.roomsCount.toString()
                            binding.lbTotalSAData.text = statisticsResponse.sensorsCount.toString()
                        }
                    } else {
                        showSnack("Failed getting statistics data")
                    }
                }

                override fun onFailure(call: Call<StatisticsResponse>, t: Throwable) {
                    Log.e("Request failed", "failed getting statistics data")
                    showSnack("Request failed. Failed getting statistics data")

                }
            }))
        }
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }
}

