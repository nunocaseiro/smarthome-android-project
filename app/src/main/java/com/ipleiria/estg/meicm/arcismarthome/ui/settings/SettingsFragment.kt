package com.ipleiria.estg.meicm.arcismarthome.ui.settings

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.internal.LinkedTreeMap
import com.ipleiria.estg.meicm.arcismarthome.LoginActivity
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentSettingsBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Home
import com.ipleiria.estg.meicm.arcismarthome.models.User
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.ProfileResponse
import com.ipleiria.estg.meicm.arcismarthome.utils.email.GMailSender
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var singleton = AppData.instance
    private var listOfAccounts = mutableListOf<User>()
    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var recyclerAccounts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.lifecycleOwner = this

        getAccounts()

        binding.btnAdd.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionNavigationSettingsToRegisterAccountFragment2(
                    1
                )
            )
        }

        binding.ivEditProfile.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionNavigationSettingsToRegisterAccountFragment2(
                    2
                )
            )
        }

        val recyclerViewAccounts = binding.rvHomeAccounts
        recyclerAccounts = recyclerViewAccounts
        recyclerViewAccounts.adapter = ItemAdapter(binding, listOfAccounts)
        recyclerViewAccounts.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAccounts.setHasFixedSize(true)

        insertInformation()


        binding.ivLogout.setOnClickListener {
            val diaLogout = AlertDialog.Builder(requireContext())
            diaLogout.setMessage(
                String.format(
                    "%s?",
                    getString(R.string.qst_logout)
                )
            )
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    dialog.dismiss()
                    logout(1)
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        binding.btnSave.setOnClickListener {

            val home = AppData.instance.home
            home.name = binding.etHomeName.text.toString()
            home.latitude = binding.etHomeLatitude.text.toString()
            home.longitude = binding.etHomeLongitude.text.toString()
            AppData.instance.housename.postValue(home.name)
            if (home.name!!.isNotEmpty()) {
                if (home.latitude!!.isNotEmpty() && home.latitude!!.length <= 5 && home.longitude!!.isNotEmpty() && home.longitude!!.length <= 5 &&
                        home.latitude!!.toDouble() >= -90 &&  home.latitude!!.toDouble() <= 90 && home.longitude!!.toDouble() >= -180 && home.longitude!!.toDouble() <= 80) {

                    val updateHome = ApiClient.service.updateHome(
                        AppData.instance.user.token,
                        AppData.instance.home.id,
                        home
                    )

                    updateHome.enqueue((object : Callback<Home> {
                        override fun onResponse(call: Call<Home>, response: Response<Home>) {
                            if (response.isSuccessful) {


                                val imm =
                                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(binding.etHomeName.windowToken, 0)
                                imm.hideSoftInputFromWindow(binding.etHomeLatitude.windowToken, 0)
                                imm.hideSoftInputFromWindow(binding.etHomeLongitude.windowToken, 0)


                                AppData.instance.home = home.copy()
                                val message = "Home was updated."
                                showSnack(message)
                            } else {
                                val message = "Home was not updated."
                                showSnack(message)
                            }
                        }

                        override fun onFailure(call: Call<Home>, t: Throwable) {
                            Log.e("Request failed", "Update home failed")
                            showSnack(t.localizedMessage!!)
                        }
                    }))
                } else {
                    showSnackMaxLines("Wrong coordinates. Latitude must be between -90 and 90. Logitude must be between -180 and 80",7,6000)
                }
            } else {
                showSnack("Name is empty.")
            }
        }


        binding.btnDeleteHome.setOnClickListener {
            val altDial = AlertDialog.Builder(requireContext())

            val message = String.format(
                "%s your home \"%s\"?\n\n%s: %s!",
                getString(R.string.qst_delete),
                singleton.home.name,
                getString(R.string.attention),
                getString(R.string.att_delete_recover)
            )

            binding.btnDeleteHome.isEnabled = false

            altDial.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    binding.btnDeleteHome.isEnabled = true
                    dialog.dismiss()
                    deleteHome()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    binding.btnDeleteHome.isEnabled = true
                    dialog.dismiss()
                }
                .show()
        }


        binding.viewSettings.setOnClickListener {
            AppData.instance.hideKeyboard(requireContext(), it)
        }




        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            binding.civProfileImage.setImageURI(imageUri)

            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (bitmap != null) {

                val imageBytes: ByteArray = imageToByteArray(bitmap)
                val encodedImage: String = Base64.encodeToString(
                    imageBytes,
                    Base64.DEFAULT
                ) // actual conversion to Base64 String Image
                uploadFile(encodedImage)
            }

        }
    }

    private fun imageToByteArray(bitmapImage: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        return baos.toByteArray();
    }


    @SuppressLint("SetTextI18n")
    private fun insertInformation() {
        if (singleton.user.photo != null) Glide.with(binding.root).load(singleton.user.photo)
            .into(binding.civProfileImage)
        else binding.civProfileImage.setImageResource(R.drawable.ic_profile_solid)

        binding.tvProfileName.text = singleton.user.firstName + " " + singleton.user.lastName

        if (singleton.user.role == "admin") {
            binding.etHomeName.setText(singleton.home.name)
            binding.etHomeLatitude.setText(singleton.home.latitude)
            binding.etHomeLongitude.setText(singleton.home.longitude)
        } else {
            binding.groupNotAdmin.visibility = View.GONE
        }

        //TODO
        if (AppData.instance.home.id == 21){
            binding.btnDeleteHome.isEnabled = false
        }
    }


    private fun getAccounts() {
        val accountsResponseCall = ApiClient.service.getHomeAccounts(
            singleton.user.token,
            singleton.home.id!!
        )
        accountsResponseCall.enqueue((object : Callback<ArrayList<Any>> {
            override fun onResponse(
                call: Call<ArrayList<Any>>,
                response: Response<ArrayList<Any>>
            ) {
                if (response.isSuccessful) {
                    val accounts = response.body()

                    var houseAccount: User
                    for (account in accounts!!) {
                        val data = JSONObject((account as LinkedTreeMap<*, *>)["user"].toString())

                        if ((data["id"] as Double).toInt() == singleton.user.id) continue

                        houseAccount = User(
                            (data["id"] as Double).toInt(),
                            data["username"].toString(),
                            data["email"].toString(),
                            account["photo"] as String?,
                            data["first_name"].toString(),
                            data["last_name"].toString(),
                            ((data["groups"] as JSONArray).get(0) as Double).toInt(),
                            ""
                        )

                        listOfAccounts.add(houseAccount)
                    }

                    binding.rvHomeAccounts.adapter!!.notifyDataSetChanged()

                    binding.clLoading.visibility = View.GONE
                    binding.clHomeAccounts.visibility = View.VISIBLE

                    if (singleton.user.role == "admin") binding.btnAdd.visibility = View.VISIBLE

                } else {
                    val message = "Failed getting home accounts"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<ArrayList<Any>>, t: Throwable) {
                Log.e("Request failed", "Home Accounts failed")
                showSnack(t.localizedMessage!!)
            }
        }))
    }


    private fun logout(i: Int) {
        val logoutResponseCall = ApiClient.service.logoutUser(AppData.instance.user.token)
        logoutResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    if (i == 1) {
                        val message = "Successful user logout"
                        showSnack(message)

                        context?.cacheDir?.deleteRecursively()
                        context?.let { trimCache(it) }

                        activity!!.finish()

                        startActivity(
                            Intent(
                                requireContext(),
                                LoginActivity::class.java
                            )
                        )
                    }
                } else {
                    val message = "Logout was not successful"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", "User Logout failed")
                showSnack(t.localizedMessage!!)
            }
        }))
    }

    private fun trimCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            if (dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (e: Exception) {
            Log.e("Failed trim cache", "Failed")
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children: Array<String> = dir.list()!!
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        if (dir != null) {
            return dir.delete()
        }
        return false
    }


    private fun deleteHome() {
        logout(0)

        val homeDeleteResponseCall =
            ApiClient.service.deleteHome(AppData.instance.user.token, singleton.home.id!!)
        homeDeleteResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {

                    for (account in listOfAccounts) {
                        val emailBody = String.format(
                            "%s %s %s,\n\n%s %s, %s %s %s.\n%s [%s] %s.",
                            getString(R.string.email_dear),
                            account.firstName,
                            account.lastName,
                            getString(R.string.email_delete_home_body_1),
                            singleton.home.name,
                            getString(R.string.email_delete_home_body_2),
                            singleton.user.firstName,
                            singleton.user.lastName,
                            getString(R.string.email_delete_home_body_3),
                            account.username,
                            getString(R.string.email_delete_home_body_4)
                        )

                        sendEmail(
                            account.email!!,
                            getString(R.string.email_delete_home_subject),
                            emailBody
                        )
                    }

                    val message = "Home deleted with success"
                    showSnack(message)

                    activity!!.finish()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                } else {
                    val message = "Home not deleted"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", t.localizedMessage!!)
                showSnack("Delete home failed")
            }
        }))
    }

    private fun sendEmail(receiverEmail: String, subject: String, body: String) {
        Thread {
            try {
                val sender = GMailSender()
                sender.sendMail(subject, body, null, receiverEmail)
            } catch (e: java.lang.Exception) {
                Log.e("SendMail", e.message, e)
            }
        }.start()
    }


    private fun uploadFile(encoded: String) {

        val dic = HashMap<String, String>()
        dic["photo"] = encoded

        val requestPhoto = AppData.instance.user.profile?.let {
            ApiClient.service.updatePhoto(
                dic,
                it,
                AppData.instance.user.token
            )
        }
        requestPhoto?.enqueue((object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                if (response.isSuccessful) {

                    val body = response.body()
                    AppData.instance.user.photo = body?.photo

                    for (account in listOfAccounts) {
                        if (account.id == body?.user?.id) {
                            account.photo = body?.photo
                        }
                    }
                    recyclerAccounts.adapter?.notifyDataSetChanged()

                    val message = "Photo updated successfully"
                    showSnack(message)
                } else {
                    val message = "Photo not updated"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("Request failed", t.localizedMessage!!)
                showSnack("Photo not updated")
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