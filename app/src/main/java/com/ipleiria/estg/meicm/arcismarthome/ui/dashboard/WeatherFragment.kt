package com.ipleiria.estg.meicm.arcismarthome.ui.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentWeatherBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.round


class WeatherFragment : Fragment() {

    private lateinit var binding: FragmentWeatherBinding
    var apiKeyWeatherApi = "821c9e5f1e8a64039f4dcc09c945ae94"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_weather,container,false)

        if (AppData.instance.weather){
            AppData.instance.weather = false
        getWeatherData()
        }

        return binding.root
    }


    private fun getWeatherData(){
        val weatherResponseCall = ApiClient.serviceWeather.getWeatherOfHome(AppData.instance.home.latitude!!, AppData.instance.home.longitude!!, apiKeyWeatherApi)
        weatherResponseCall.enqueue((object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        binding.weatherData = weatherResponse

                        val icon = resources.getIdentifier("w" + weatherResponse.weather?.get(0)?.icon.toString(), "drawable", context!!.packageName)
                        binding.ivWeather.setImageResource(icon)

                        binding.tvTemperature.text = (round(weatherResponse.main!!.temp!!.toDouble() * 10.0)/10.0).toString().plus("ºC")

                        binding.groupWeather.visibility = View.VISIBLE
                        binding.groupLoading.visibility = View.GONE
                        AppData.instance.weather = true
                    }
                } else {
                    AppData.instance.weather = true
                    showSnack("Weather not loaded")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                AppData.instance.weather = true
                showSnack("Request failed.Weather not loaded")
            }
        }))
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }


}

