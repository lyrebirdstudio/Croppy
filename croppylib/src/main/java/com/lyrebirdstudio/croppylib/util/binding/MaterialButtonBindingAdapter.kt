package com.lyrebirdstudio.croppylib.util.binding

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton

@BindingAdapter("bindingTintColor")
fun materialButtonBackgroundTintColor(materialButton: MaterialButton, colorRes: Int) {
    materialButton.backgroundTintList =
        ContextCompat.getColorStateList(materialButton.context, colorRes)

}
