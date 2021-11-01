package com.ipleiria.estg.meicm.arcismarthome.ui.register

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentHomeKeyBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Home
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.HouseKeyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeKeyFragment : Fragment() {

    lateinit var binding: FragmentHomeKeyBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        container?.removeAllViews()
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home_key,
            container,
            false
        )

        val nightModeFlags = context?.resources?.configuration!!.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> binding.logo.setImageResource(R.drawable.logo_light_mode)
            Configuration.UI_MODE_NIGHT_YES -> binding.logo.setImageResource(R.drawable.logo_dark_mode)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> binding.logo.setImageResource(R.drawable.logo_light_mode)
        }

        binding.btnSubmit.setOnClickListener { check() }
        binding.iconQrcode.setOnClickListener { qrcode() }

        binding.viewHomeKey.setOnClickListener{
            AppData.instance.hideKeyboard(requireContext(), it)
        }
        return binding.root
    }

    private fun qrcode() {
        val intentIntegrator = IntentIntegrator.forSupportFragment(this)
        intentIntegrator.setBeepEnabled(false)
        intentIntegrator.initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                val message = "Cannot read toast"
                showSnack(message)
            } else {
                binding.etHouseKey.setText(result.contents, TextView.BufferType.EDITABLE)
                val toast = "Scanned from fragment: " + result.contents
                showSnack(toast)
                check()

            }
        }
    }


    private fun check() {
        if (binding.etHouseKey.text.isNotEmpty()) {
            AppData.instance.houseKey = binding.etHouseKey.text.toString()
            callCheckKey()

        } else {
            showSnack("Please insert valid number")

        }
    }

    private fun callCheckKey() {
        val checkResponseCall = ApiClient.service.checkKey(AppData.instance.houseKey)
        checkResponseCall!!.enqueue((object : Callback<HouseKeyResponse> {
            override fun onResponse(
                call: Call<HouseKeyResponse>,
                response: Response<HouseKeyResponse>
            ) {
                if (response.isSuccessful) {
                    checkResponse(response.body())
                } else {
                    showSnack("Key not valid")
                }
            }

            override fun onFailure(call: Call<HouseKeyResponse?>, t: Throwable) {
                Log.e("Request failed", "Checking key failed")
                val message = t.localizedMessage
                if (message!= null){
                    showSnack(message)
                }
            }
        }))

    }

    private fun checkResponse(response: HouseKeyResponse?) {
        if (response!!.validKey && !response.homeCreated && !response.profilesCreated) {
            this.findNavController().navigate(HomeKeyFragmentDirections.actionHouseKeyToRegisterHome())
        }

        if (response.validKey && response.homeCreated && !response.profilesCreated) {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("Home already exists with this code. However there are no admin. Do you want create one?")
                .setNegativeButton("No") { _, _ ->
                    this.findNavController()
                        .navigate(HomeKeyFragmentDirections.actionHouseKeyToLoginFragment())
                }
                .setPositiveButton("Yes") { _, _ -> getHouseWithKey() }
            builder.create()
            builder.show()
        }

        if (response.validKey && response.homeCreated && response.profilesCreated) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Home and admin already exists. You will return to login page.")
                .setNegativeButton("OK") { _, _ ->
                    this.findNavController()
                        .navigate(HomeKeyFragmentDirections.actionHouseKeyToLoginFragment())
                }
            builder.create()
            builder.show()
        }

        if (!response.validKey && !response.homeCreated && !response.profilesCreated){
            showSnack("Invalid key. Please insert valid key")
        }
    }


    private fun getHouseWithKey() {
        val checkResponseCall = ApiClient.service.getHomeWKey(AppData.instance.houseKey)
        checkResponseCall!!.enqueue((object : Callback<Home> {
            override fun onResponse(call: Call<Home>, response: Response<Home>) {
                if (response.isSuccessful) {

                    val body = response.body()
                    if (body != null) {
                        AppData.instance.home = body
                        findNavController().navigate(HomeKeyFragmentDirections.actionHouseKeyToRegisterAccountFragment())
                    }else{
                        showSnack("There are no home with that key")
                    }
                }
            }

            override fun onFailure(call: Call<Home?>, t: Throwable) {
                Log.e("Request failed", "Checking key failed")
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


}