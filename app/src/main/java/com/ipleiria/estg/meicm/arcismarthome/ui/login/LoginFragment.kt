package com.ipleiria.estg.meicm.arcismarthome.ui.login

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentLoginBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.server.*
import com.ipleiria.estg.meicm.arcismarthome.ui.ParentLoginFragment

class LoginFragment : ParentLoginFragment() {

    private lateinit var dataSourceFav: FavouriteDatabaseDao
    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.lifecycleOwner = this

        val nightModeFlags = context?.resources?.configuration!!.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> binding.logo.setImageResource(R.drawable.logo_light_mode)
            Configuration.UI_MODE_NIGHT_YES -> binding.logo.setImageResource(R.drawable.logo_dark_mode)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> binding.logo.setImageResource(R.drawable.logo_light_mode)
        }

        val application = requireNotNull(this.activity).application
        dataSourceFav = FavouriteDatabase.getInstance(application).favouriteDatabaseDao

        binding.btnLogin.setOnClickListener {
            AppData.instance.hideKeyboard(requireContext(), it)
            binding.clLoading.visibility = View.VISIBLE
            binding.clLogin.visibility = View.GONE
            onClickLogin()
        }
        binding.btnSignin.setOnClickListener { onClickRegisterHouse() }

        binding.fragmentLogin.setOnClickListener {
            AppData.instance.hideKeyboard(requireContext(), it)
        }


        return binding.root
    }


    private fun onClickLogin() {
        if (binding.etUsername.text.isEmpty() || binding.etPassword.text.isEmpty()
        ) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),getString( R.string.tst_fill_the_inputs),Snackbar.LENGTH_LONG).show()
            binding.clLoading.visibility = View.INVISIBLE
            binding.clLogin.visibility = View.VISIBLE
        } else {
            val loginRequest = LoginRequest()
            loginRequest.username = binding.etUsername.text.toString()
            loginRequest.password = binding.etPassword.text.toString()

            loginUser(loginRequest, binding)
        }
    }

    private fun onClickRegisterHouse() {
        this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHouseKey())
    }

}