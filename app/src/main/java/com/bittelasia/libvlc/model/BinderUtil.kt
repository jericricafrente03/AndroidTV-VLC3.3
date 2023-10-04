package com.bittelasia.libvlc.model

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load

object BinderUtil {

    @BindingAdapter(value = ["app:loadImageDefault"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(imageDefault: String?) {
        load(imageDefault)
    }

}