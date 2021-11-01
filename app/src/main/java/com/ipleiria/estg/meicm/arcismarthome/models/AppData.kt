package com.ipleiria.estg.meicm.arcismarthome.models

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.MutableLiveData
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.common.collect.Lists
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.eclipse.paho.android.service.MqttAndroidClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AppData {


    var home: Home = Home(null, null, null, null, null)
    var user: User = User(null, null, null, null, null, null, null, "")

    var notification = MutableLiveData<Int>()
    var sensorUpdate = MutableLiveData<Int>()
    var housename = MutableLiveData<String>()
    var sensorValueUpdate = MutableLiveData<SensorUpdate>()
    lateinit var mqttClient: MqttAndroidClient
    lateinit var houseKey: String
    var weather: Boolean = true

    //DIALOG FLOW
    val roomEntityId = ""
    val deviceEntityId = ""


    private object HOLDER {
        val INSTANCE = AppData()
    }



    fun hideKeyboard(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }

    fun createEntity(context: Context, value: String, sensorOrRoom: String){
        val stream = context.resources.openRawResource(R.raw.credential)

        val credentials = GoogleCredentials.fromStream(stream)
            .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"))
        val projectId = (credentials as ServiceAccountCredentials).projectId

        CoroutineScope(Dispatchers.Default).launch {
            credentials.refreshIfExpired()
            Log.e("ERRRORRR",credentials.getAccessToken().tokenValue)

            val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
            val body: RequestBody =
                RequestBody.create(mediaType, "{\"entities\":[{\"value\":\"${value}\",\"synonyms\":[\"${value}\"]}]}")
            val entity:Call<Void> = if(sensorOrRoom == "room"){
                ApiClient.service.createEntityValue("Bearer "+credentials.getAccessToken().tokenValue,"application/json", "application/json",roomEntityId, body)
            }else{
                ApiClient.service.createEntityValue("Bearer "+credentials.getAccessToken().tokenValue,"application/json", "application/json",deviceEntityId, body)
            }
            entity.enqueue((object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.e("CREATED ENTITY", "CREATED ENTITY")
                    } else {
                        Log.e("CREATED ENTITY", "NOT CREATED ENTITY")

                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    val message = t.localizedMessage
                    if (message!=null){
                        Log.e("CREATED ENTITY", "CREATED ENTITY")
                    }
                }
            }))
        }



    }


    fun deleteEntity(context: Context,update: Boolean, value: String, oldName: String?, sensorOrRoom: String) {
        val stream = context.resources.openRawResource(R.raw.credential)

        val credentials = GoogleCredentials.fromStream(stream)
            .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"))
        val projectId = (credentials as ServiceAccountCredentials).projectId

        CoroutineScope(Dispatchers.Default).launch {
            credentials.refreshIfExpired()
            Log.e("ERRRORRR",credentials.getAccessToken().tokenValue)

            val entityId = if (sensorOrRoom == "room") roomEntityId else deviceEntityId
            val valueToDelete = if (update) oldName else value

            val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, "{\"entityValues\":[\"${valueToDelete}\"]}")
            val entity = ApiClient.service.deleteEntityValue("Bearer "+credentials.getAccessToken().tokenValue,"application/json", "application/json",entityId, body)
            entity.enqueue((object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        if(update){
                            createEntity(context,value, sensorOrRoom)
                        }
                        Log.e("CREATED ENTITY", "CREATED ENTITY")
                    } else {
                        Log.e("CREATED ENTITY", "NOT CREATED ENTITY")

                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    val message = t.localizedMessage
                    if (message!=null){
                        Log.e("CREATED ENTITY", "CREATED ENTITY")
                    }
                }
            }))
        }
    }

    companion object {
        val instance: AppData by lazy { HOLDER.INSTANCE }
    }
}