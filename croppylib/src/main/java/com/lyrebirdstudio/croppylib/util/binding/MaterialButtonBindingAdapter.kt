package com.lyrebirdstudio.croppylib.util.binding

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton

@BindingAdapter("bindingTintColor")
fun MaterialButton.backgroundTintColor(colorRes: Int) {
    backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
}
