package com.lyrebirdstudio.croppylib.main

import android.net.Uri
import android.os.Parcelable
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppylib.R
import kotlinx.android.parcel.Parcelize

@Parcelize
open class CropRequest(
    open val sourceUri: Uri,
    open val requestCode: Int,
    open val excludedAspectRatios: List<AspectRatio>,
    open val croppyTheme: CroppyTheme
) : Parcelable {

    @Parcelize
    class Manual(
        override val sourceUri: Uri,
        val destinationUri: Uri,
        override val requestCode: Int,
        override val excludedAspectRatios: List<AspectRatio> = arrayListOf(),
        override val croppyTheme: CroppyTheme = CroppyTheme(R.color.blue)
    ) : CropRequest(sourceUri, requestCode, excludedAspectRatios, croppyTheme)

    @Parcelize
    class Auto(
        override val sourceUri: Uri,
        override val requestCode: Int,
        val storageType: StorageType = StorageType.EXTERNAL,
        override val excludedAspectRatios: List<AspectRatio> = arrayListOf(),
        override val croppyTheme: CroppyTheme = CroppyTheme(R.color.blue)
    ) : CropRequest(sourceUri, requestCode, excludedAspectRatios, croppyTheme)

    companion object {
        fun empty(): CropRequest =
            CropRequest(Uri.EMPTY, -1, arrayListOf(), CroppyTheme(R.color.blue))
    }
}


