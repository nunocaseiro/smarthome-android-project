package com.ipleiria.estg.meicm.arcismarthome.ui.register

import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentRegisterHomeBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Home
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterHomeFragment : Fragment() {

    private lateinit var binding: FragmentRegisterHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_register_home, container, false)

        binding.btnCreateHouse.setOnClickListener { createHome() }

        val nightModeFlags = context?.resources?.configuration!!.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> binding.logo.setImageResource(R.drawable.logo_light_mode)
            Configuration.UI_MODE_NIGHT_YES -> binding.logo.setImageResource(R.drawable.logo_dark_mode)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> binding.logo.setImageResource(R.drawable.logo_light_mode)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(RegisterHomeFragmentDirections.actionRegisterHomeToLoginFragment())
        }

        binding.viewRegister.setOnClickListener{
            AppData.instance.hideKeyboard(requireContext(), it)
        }

        return binding.root
    }

    private fun createHome() {
        if (binding.etHouseName.text.isNotEmpty() && binding.etLatitude.text.isNotEmpty() && binding.etLongitude.text.isNotEmpty()) {
            binding.btnCreateHouse.isEnabled = false
            val home = Home(
                null,
                binding.etHouseName.text.toString(),
                binding.etLatitude.text.toString(),
                binding.etLongitude.text.toString(),
                AppData.instance.houseKey
            )

            val loginResponseCall = ApiClient.service.createHome(home)
            loginResponseCall.enqueue((object : Callback<Home?> {
                override fun onResponse(call: Call<Home?>, response: Response<Home?>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            AppData.instance.houseKey = ""
                            AppData.instance.home = response.body()!!
                            showSnack("House created")
                            findNavController().navigate(RegisterHomeFragmentDirections.actionRegisterHomeToRegisterAccountFragment())
                        }
                    }else{
                        binding.btnCreateHouse.isEnabled = true
                        showSnack("Please, insert valid data. Use dots instead of comma in latitude and longitude")
                    }
                }

                override fun onFailure(call: Call<Home?>, t: Throwable) {
                    binding.btnCreateHouse.isEnabled = true
                    Log.e("Request failed", "house not created")
                    val message = t.localizedMessage
                    if (message!= null){
                        showSnack(message)
                    }
                }
            }))
        }
        else {
            showSnack("Please, insert valid data. Use dots instead of comma in latitude and longitude ")
        }
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }
}