package com.ipleiria.estg.meicm.arcismarthome.wearable

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var menuItem: TextView = view.findViewById(R.id.menu_item)
    var menuIcon: ImageView = view.findViewById(R.id.menu_icon)
    var status: View = view.findViewById(R.id.status)
}