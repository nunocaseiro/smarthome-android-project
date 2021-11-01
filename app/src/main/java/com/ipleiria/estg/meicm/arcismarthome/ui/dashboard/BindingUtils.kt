package com.ipleiria.estg.meicm.arcismarthome.ui.dashboard

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("loadImage")
fun ImageView.setImageViewResource(resource: Int) {
    setImageResource(resource)
}
