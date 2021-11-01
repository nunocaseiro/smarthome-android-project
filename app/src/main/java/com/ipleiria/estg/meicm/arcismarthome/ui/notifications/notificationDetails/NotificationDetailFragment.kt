package com.ipleiria.estg.meicm.arcismarthome.ui.notifications.notificationDetails

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.MainActivity
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabase
import com.ipleiria.estg.meicm.arcismarthome.database.asDM
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentNotificationDetailBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Notification
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Timestamp


class NotificationDetailFragment : Fragment() {

    lateinit var binding : FragmentNotificationDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_notification_detail, container, false
        )

        (activity as MainActivity).include.visibility = View.GONE

        val application = requireNotNull(this.activity).application

        val arguments = arguments?.let { NotificationDetailFragmentArgs.fromBundle(it) }

        val dataSource = NotificationDatabase.getInstance(application).notificationDatabaseDao
        val viewModelFactory = arguments?.let {
            NotificationDetailViewModelFactory(
                it.notificationKey,
                dataSource, application
            )
        }

        val notificationDetailViewModel = viewModelFactory?.let {
            ViewModelProvider(this, it).get(
                NotificationDetailViewModel::class.java
            )
        }

        binding.notificationDetailViewModel = notificationDetailViewModel

        binding.lifecycleOwner = this

        notificationDetailViewModel?.getNotification()?.observe(viewLifecycleOwner, {
            if (it != null) {
                notificationDetailViewModel.getNotification().removeObservers(viewLifecycleOwner)
                binding.lbCreatedData.text = auxTimestamp(it.created)
                Glide.with(binding.root).load("http://161.35.8.148" + it.photo)
                    .into(binding.ivNotification)
                if (!it.seen){
                    updateNotificationAsRead(it, notificationDetailViewModel)
                }
            }
        })

        return binding.root
    }

    private fun auxTimestamp(item: String): String {
        var x = item.replace("T", " ")
        x = x.substring(0, 19)
        x = Timestamp.valueOf(x).toString()
        return x.substring(0, x.length - 5)
    }

    private fun updateNotificationAsRead(
        notificationDB: NotificationDataModel,
        notificationViewModel: NotificationDetailViewModel
    ) {

        notificationDB.seen = true
        notificationViewModel.database.update(notificationDB)

        val vehicleResponseCall = ApiClient.service.updateNotification(
            AppData.instance.user.token, notificationDB.id, notificationDB.asDM(notificationDB)
        )
        vehicleResponseCall.enqueue((object : Callback<Notification> {
            override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                if (response.isSuccessful) {

                    showSnack("Mark as read")

                } else {
                    val message = "Cannot mark as read"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Notification>, t: Throwable) {
                val message = t.localizedMessage
                if (message != null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }
}

