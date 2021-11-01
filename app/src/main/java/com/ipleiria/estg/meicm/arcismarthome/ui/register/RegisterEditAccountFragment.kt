package com.ipleiria.estg.meicm.arcismarthome.ui.register

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.ui.ParentLoginFragment
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentRegisterEditAccountBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.UserRegister
import com.ipleiria.estg.meicm.arcismarthome.server.*
import com.ipleiria.estg.meicm.arcismarthome.utils.email.GMailSender
import kotlinx.android.synthetic.main.fragment_register_edit_account.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.sin

class RegisterEditAccountFragment : ParentLoginFragment() {

    lateinit var binding: FragmentRegisterEditAccountBinding
    var key: Int = 0
    private val pickImage = 100
    private var imageUri: Uri? = null
    private var encodedImage: String? = null

    private var singleton = AppData.instance

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_register_edit_account,
                container,
                false
            )

        val arguments = arguments?.let { RegisterEditAccountFragmentArgs.fromBundle(it) }

        if (arguments !== null) {
            key = arguments.action
        }
        if (key == 2) {
            binding.user = singleton.user
            if (singleton.user.photo != null) Glide.with(binding.root)
                .load(singleton.user.photo)
                .into(binding.civProfileImage)
            else binding.civProfileImage.setImageResource(R.drawable.ic_profile_solid)
        }
        binding.action = key


        binding.civProfileImage.setOnClickListener {
            openGallery()
        }

        binding.ivChangePhoto.setOnClickListener {
            openGallery()
        }

        binding.ivRemovePhoto.setOnClickListener {
            singleton.user.photo = null
            binding.user = singleton.user
            deletePhoto()
            binding.civProfileImage.setImageResource(R.drawable.ic_profile_solid)
        }

        binding.changePassword.setOnClickListener {
            binding.checkBoxPw = changePassword.isChecked
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (key == 0) findNavController().navigate(RegisterEditAccountFragmentDirections.actionRegisterAccountFragmentToLoginFragment())
            else findNavController().navigate(RegisterEditAccountFragmentDirections.actionRegisterAccountFragmentToNavigationSettings())
        }

        binding.btnCreateSaveAccount.setOnClickListener {
            checkAndCreateAccount() }

        binding.viewAddEditAccount.setOnClickListener {
            AppData.instance.hideKeyboard(requireContext(), it)
        }

        return binding.root
    }

    private fun deletePhoto() {
        val userResponseCall = AppData.instance.user.profile?.let { ApiClient.service.deleteProfilePhoto(singleton.user.token, it) }
        userResponseCall?.enqueue((object : Callback<Void> {
            override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
            ) {
                if (response.isSuccessful) {
                    showSnack("Photo deleted successfully")


                } else {
                    showSnack("Photo deleted unsuccessfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showSnack("Request failed. Photo deleted unsuccessfully")
            }
        }))
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
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
                val encodedImageL: String = Base64.encodeToString(
                    imageBytes,
                    Base64.DEFAULT
                ) // actual conversion to Base64 String Image
                encodedImage = encodedImageL
            }

        }
    }

    private fun imageToByteArray(bitmapImage: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        return baos.toByteArray();
    }


    private fun checkAndCreateAccount() {
        val check = checkData()
        if (check.first) {
            binding.btnCreateSaveAccount.isEnabled = false
            if (key == 0 || key == 1) {
                createAccount()
            }else updateAccount()
        } else {
            showSnackMaxLines(check.second, 8,7000)
        }
    }

    private fun checkData(): Pair<Boolean, String> {
        var msg = ""
        var check = true

        if (binding.etUsername.text.isEmpty()) {
            check = false
            msg = appendMsg(msg, "username name cannot be empty")
        }

        if (binding.etFirstName.text.isEmpty()) {
            check = false
            msg = appendMsg(msg, "first name cannot be empty")
        }

        if (binding.etLastName.text.isEmpty()) {
            check = false
            msg = appendMsg(msg, "last name cannot be empty")
        }

        if (binding.etEmail.text.isEmpty()) {
            check = false
            msg = appendMsg(msg, "email name cannot be empty")
        }
        if (key == 0 || key == 1 || changePassword.isChecked){

            if (binding.etPassword.text.toString()
                    .isEmpty() || binding.etPasswordConfirmation.text.isEmpty()
            ) {
                check = false
                msg = appendMsg(msg, "password field cannot be empty")
            }

            if (binding.etPassword.text.toString() != binding.etPasswordConfirmation.text.toString()) {
                check = false
                msg = appendMsg(msg, "password and confirmation must be the same")
            }
            if (binding.etPassword.text.toString().length < 8) {
                check = false
                msg = appendMsg(msg, "password must have more or equal than 8 characters")
            }
        }

        return Pair(check, msg)
    }


    private fun createAccount() {
        if (singleton.home.id != null && singleton.home.id!! > 0) {

            val role =
                if (key == 0) "admin"
                else "user"

            val user = UserRegister(
                binding.etUsername.text.toString(),
                binding.etEmail.text.toString(),
                binding.etFirstName.text.toString(),
                binding.etLastName.text.toString(),
                role,
                binding.etPassword.text.toString(),
                binding.etPasswordConfirmation.text.toString(),
                singleton.home.id!!
            )

            val registerResponseCall = ApiClient.service.registerUser(user)
            registerResponseCall.enqueue((object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {

                        if (key == 1) {
                            val emailBody = String.format(
                                "%s %s %s,\n\n%s %s %s %s %s.\n%s:\n\n%s: %s\n%s: %s\n\n\n%s?\n%s.",
                                getString(R.string.email_dear),
                                user.first_name,
                                user.last_name,
                                getString(R.string.email_create_account_body_1),
                                singleton.user.firstName,
                                singleton.user.lastName,
                                getString(R.string.email_create_account_body_2),
                                singleton.home.name,
                                getString(R.string.email_create_account_body_3),
                                getString(R.string.username),
                                user.username,
                                getString(R.string.password),
                                user.password,
                                getString(R.string.email_create_account_body_4),
                                getString(R.string.email_create_account_body_5)
                            )

                            sendEmail(
                                user.email,
                                getString(R.string.email_create_account_subject),
                                emailBody
                            )
                        }

                        val loginRequest = LoginRequest()
                        loginRequest.username = binding.etUsername.text.toString()
                        loginRequest.password = binding.etPassword.text.toString()

                        if (!encodedImage.isNullOrEmpty()) {
                            if (key == 2)
                                AppData.instance.user.profile?.let {
                                    uploadFile(encodedImage!!, it, loginRequest)
                                }
                            else getProfile(loginRequest, encodedImage!!)
                        } else {
                            if (key == 0) loginUser(loginRequest, null)
                            else findNavController().navigate(RegisterEditAccountFragmentDirections.actionRegisterAccountFragmentToNavigationSettings())
                        }
                    } else if (response.errorBody() != null){
                        binding.btnCreateSaveAccount.isEnabled = true
                        checkErrorBody(response.errorBody()!!)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                   binding.btnCreateSaveAccount.isEnabled = true
                   showSnack("Request failed")
                }
            }))
        }
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

    private fun getProfile(loginRequest: LoginRequest?, encoded: String) {
        val userResponseCall = ApiClient.service.populateUserWHome(
            singleton.user.token,
            loginRequest?.username
        )
        userResponseCall.enqueue((object : Callback<List<ProfileResponse>> {
            override fun onResponse(
                call: Call<List<ProfileResponse>>,
                response: Response<List<ProfileResponse>>
            ) {
                if (response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse != null) {
                        userResponse[0].id?.let { uploadFile(encoded, it, loginRequest) }
                    }
                } else {
                    binding.btnCreateSaveAccount.isEnabled = true
                    showSnack("User info not loaded")
                }
            }

            override fun onFailure(call: Call<List<ProfileResponse>>, t: Throwable) {
                binding.btnCreateSaveAccount.isEnabled = true
                showSnack("Request failed. Populate User and Home failed")
            }
        }))
    }


    private fun updateAccount() {
        if (singleton.home.id != null && singleton.home.id!! > 0) {

            val groups = ArrayList<Int>()
            singleton.user.roleGroup?.let { groups.add(it) }


            val user = UserRegister(
                binding.etUsername.text.toString(),
                binding.etEmail.text.toString(),
                binding.etFirstName.text.toString(),
                binding.etLastName.text.toString(),
                groups,
                binding.etPassword.text.toString(),
                binding.etPasswordConfirmation.text.toString(),
                singleton.home.id!!
            )

            val registerResponseCall = singleton.user.id?.let {
                ApiClient.service.updateUser(
                    user,
                    it,
                    singleton.user.token
                )
            }
            registerResponseCall?.enqueue((object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        singleton.user.firstName = userResponse?.first_name
                        singleton.user.lastName = userResponse?.last_name
                        singleton.user.email = user.email

                        if (!encodedImage.isNullOrEmpty()) {
                            singleton.user.profile?.let {
                                uploadFile(encodedImage!!,it,null)


                            }
                        } else {
                            if (binding.etOldPassword.text.isNotEmpty()) {
                                updatePassword()
                            } else {
                                findNavController().navigate(RegisterEditAccountFragmentDirections.actionRegisterAccountFragmentToNavigationSettings())
                            }
                        }

                    } else {
                        binding.btnCreateSaveAccount.isEnabled = true
                        if (response.errorBody() != null) {
                            checkErrorBody(response.errorBody()!!)
                        }
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    binding.btnCreateSaveAccount.isEnabled = true
                    showSnack("Request failed. User not created. Password must not have username word")
                }
            }))
        }
    }

    private fun updatePassword() {
        val pwRequest = PasswordRequest(
            binding.etOldPassword.text.toString(),
            binding.etPassword.text.toString(),
            binding.etPasswordConfirmation.text.toString()
        )
        val registerResponseCall = singleton.user.id?.let {
            ApiClient.service.updatePassword(
                pwRequest,
                it,
                singleton.user.token
            )
        }
        registerResponseCall?.enqueue((object : Callback<Void?> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                if (response.isSuccessful) {
                    findNavController().navigate(RegisterEditAccountFragmentDirections.actionRegisterAccountFragmentToNavigationSettings())
                } else {
                    binding.btnCreateSaveAccount.isEnabled = true
                   showSnackMaxLines("Error updating user password. Please, insert your old password and new one must have more than 8 characters",7, 8000)
                }
            }

            override fun onFailure(call: Call<Void?>, t: Throwable) {
                binding.btnCreateSaveAccount.isEnabled = true
                showSnackMaxLines("Request failed. Error updating user password.",7, Snackbar.LENGTH_LONG)
            }
        }))
    }


    private fun uploadFile(encoded: String, profileId: Int, loginRequest: LoginRequest?) {

        val dic = HashMap<String, String>()
        dic["photo"] = encoded

        val requestPhoto =
            ApiClient.service.updatePhoto(dic, profileId, singleton.user.token)
        requestPhoto?.enqueue((object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                if (response.isSuccessful) {
                    if (key == 0 && loginRequest != null) {
                        loginUser(loginRequest, null)
                    }
                    if (key == 2) {
                        singleton.user.photo = response.body()?.photo
                    }
                    if (binding.etOldPassword.text.isNotEmpty()) {
                        updatePassword()
                    }else if (key != 0){
                        findNavController().navigate(RegisterEditAccountFragmentDirections.actionRegisterAccountFragmentToNavigationSettings())
                    }
                } else {
                    binding.btnCreateSaveAccount.isEnabled = true
                   showSnack("Photo not uploaded")
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                binding.btnCreateSaveAccount.isEnabled = true
                showSnack("Photo not uploaded")
            }
        }))
    }

    private fun checkErrorBody(errorBody: ResponseBody) {
        val errorObj = JSONObject(errorBody.string())
        var message = ""
        if (errorObj.has("username")) {
            val aux =
                errorObj.getString("username").replace("[", " ").replace("]", " ").replace(".", " ")
                    .replace("\"", " ").trim().decapitalize(
                        Locale.ROOT
                    )
            message = appendMsg(message, aux)
        }
        if (errorObj.has("email")){
            val aux = errorObj.getString("email").replace("["," ").replace("]"," ").replace("."," ").replace("\""," ").trim().decapitalize(Locale.ROOT)

            message = appendMsg(message, "email: $aux")
        }
        if (errorObj.has("password")) {
            val aux =
                errorObj.getString("password").replace("[", " ").replace("]", " ").replace(".", " ")
                    .replace("\"", " ").trim().decapitalize(Locale.ROOT)
            message = appendMsg(message, aux)
        }

       showSnackMaxLines(message,6, Snackbar.LENGTH_LONG)
    }

    private fun appendMsg(fullMessage: String, appendText: String): String {
        val and = " and "
        var msg = fullMessage
        if (msg.isEmpty()) {
            msg += appendText.capitalize(Locale.ROOT)
        } else {
            msg += and
            msg += appendText
        }
        return msg
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_LONG)
        snack.setAction("Dismiss") { snack.dismiss() }
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