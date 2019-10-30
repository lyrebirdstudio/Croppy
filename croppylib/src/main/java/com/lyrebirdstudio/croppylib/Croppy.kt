package com.lyrebirdstudio.croppylib

import android.app.Activity
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.main.CroppyActivity

object Croppy {

    fun start(activity: Activity, cropRequest: CropRequest) {
        CroppyActivity.newIntent(context = activity, cropRequest = cropRequest)
            .also { activity.startActivityForResult(it, cropRequest.requestCode) }
    }
}