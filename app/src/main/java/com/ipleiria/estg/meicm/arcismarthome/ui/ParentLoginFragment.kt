package com.ipleiria.estg.meicm.arcismarthome.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.MainActivity
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDatabaseDao
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentLoginBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.User
import com.ipleiria.estg.meicm.arcismarthome.server.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("NAME_SHADOWING", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
abstract class ParentLoginFragment : Fragment() {

    private var singleton = AppData.instance
    private lateinit var dataSourceFav: FavouriteDatabaseDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataSourceFav =
            FavouriteDatabase.getInstance(requireNotNull(this.activity).application).favouriteDatabaseDao
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun loginUser(loginRequest: LoginRequest, binding: FragmentLoginBinding?) {
        val loginResponseCall = ApiClient.service.loginUser(loginRequest)
        loginResponseCall!!.enqueue((object : Callback<LoginResponse?> {
            override fun onResponse(
                call: Call<LoginResponse?>,
                response: Response<LoginResponse?>
            ) {
                if (response.isSuccessful) {

                    if(binding != null) {
                        binding.clLoading.visibility = View.VISIBLE
                        binding.clLogin.visibility = View.GONE
                    }

                    val loginResponse = response.body()

                    if (loginResponse != null) {
                        populateAppData(binding, loginRequest.username, "Token " + loginResponse.key)
                    }

                } else {
                    val message = "Login failed"
                    showSnack(message)
                    if (response.errorBody()!=null){
                        val errorObj = JSONObject(response.errorBody()!!.string())
                        if (errorObj.has("non_field_errors")){
                            if (errorObj.get("non_field_errors") == "Unable to log in with provided credentials"){
                                val message = "Unable to log in with provided credentials"
                                Snackbar.make(requireActivity().findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG).show()
                                }
                            }
                        }

                    if(binding != null) {
                        binding.clLoading.visibility = View.INVISIBLE
                        binding.clLogin.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                Log.e("Request failed", "login call failed")
                val message = t.localizedMessage
                showSnack(message)
            }
        }))
    }

    fun populateAppData(binding: FragmentLoginBinding?, username: String?, token: String) {
        val userResponseCall = ApiClient.service.populateUserWHome(
            token,
            username
        )
        userResponseCall.enqueue((object : Callback<List<ProfileResponse>> {
            override fun onResponse(
                call: Call<List<ProfileResponse>>,
                response: Response<List<ProfileResponse>>
            ) {
                if (response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse != null) {
                        singleton.user = User(
                            userResponse[0].user?.id,
                            username,
                            userResponse[0].user?.email,
                            userResponse[0].photo,
                            userResponse[0].user?.first_name,
                            userResponse[0].user?.last_name,
                            (userResponse[0].user?.groups)!![0],
                            token,
                            userResponse[0].id
                        )

                        singleton.home.id = userResponse[0].home?.id
                        singleton.home.name = userResponse[0].home?.name
                        singleton.home.latitude = userResponse[0].home?.latitude
                        singleton.home.longitude = userResponse[0].home?.longitude

                        if (userResponse[0].favourites != null) {
                            val favorites: List<FavouritesResponse>? = userResponse[0].favourites

                            if (favorites?.size!! > 0) {
                                for (favourite in favorites) {
                                    val fav = FavouriteDataModel(
                                        favourite.id!!,
                                        favourite.sensor!!,
                                        favourite.user!!
                                    )
                                    GlobalScope.launch {
                                        dataSourceFav.clear()
                                        dataSourceFav.insert(fav)
                                    }
                                }
                            }
                        }
                    }

                    startActivity(Intent(context, MainActivity::class.java))
                    activity?.finish()

                } else {
                    val message = "User's info not loaded"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<List<ProfileResponse>>, t: Throwable) {
                Log.e("Request failed", "Populate User and Home failed")

                if(binding != null) {
                    binding.clLoading.visibility = View.GONE
                    binding.clLogin.visibility = View.VISIBLE
                }

                val message = t.localizedMessage
                showSnack(message)
            }
        }))
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss") { snack.dismiss() }
        snack.show()
    }
}