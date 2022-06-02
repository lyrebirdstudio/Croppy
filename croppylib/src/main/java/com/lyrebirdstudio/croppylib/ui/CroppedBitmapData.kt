package com.lyrebirdstudio.croppylib.ui

import android.graphics.Bitmap
import android.graphics.Rect

data class CroppedBitmapData(
    val croppedBitmap: Bitmap?,
    val croppingRect: Rect
)