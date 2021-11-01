package com.ipleiria.estg.meicm.arcismarthome.ui.notifications

import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import java.sql.Timestamp


@BindingAdapter("notificationState")
fun ImageView.setNotificationItemListImage(item: NotificationDataModel) {
    setImageResource(
        when (item.seen) {
            false -> when (item.description) {
                "allowed" -> R.drawable.ic_unread_border
                else -> R.drawable.ic_unread_border
            }
            true -> when (item.description) {
                "allowed" -> R.drawable.ic_read_solid
                else -> R.drawable.ic_read_solid
            }
        }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("notificationTimestamp")
fun TextView.setNotificationTimestamp(item: NotificationDataModel) {
    var x = item.created.replace("T", " ")
    x = x.substring(0, 19)
    x = Timestamp.valueOf(x).toString()
    text = x.substring(0, x.length - 5)
}
