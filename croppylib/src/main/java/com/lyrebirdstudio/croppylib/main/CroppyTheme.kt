package com.lyrebirdstudio.croppylib.main

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.lyrebirdstudio.croppylib.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CroppyTheme(@ColorRes val accentColor: Int) : Parcelable {

    companion object {
        fun default() = CroppyTheme(R.color.blue)
    }
}