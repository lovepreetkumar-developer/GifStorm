package com.techfathers.gifstorm.util

import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class BindingUtils {

    companion object {

        @BindingAdapter(value = ["simple_resource"])
        @JvmStatic
        fun simpleResource(imageView: AppCompatImageView, simpleResource: Int) {
            if (simpleResource != -1) imageView.setImageResource(simpleResource)
        }

        @BindingAdapter(value = ["setCustomBackground"])
        @JvmStatic
        fun setCustomBackground(view: View, simpleResource: Int) {
            if (simpleResource != -1) view.setBackgroundResource(simpleResource)
        }

        @BindingAdapter(value = ["load_gif", "placeholder"], requireAll = false)
        @JvmStatic
        fun loadGifImage(
            appCompatImageView: AppCompatImageView,
            imageUrl: String,
            placeholder: Drawable?
        ) {
            Glide.with(appCompatImageView.context)
                .asGif()
                .placeholder(placeholder)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(appCompatImageView)
        }

    }
}