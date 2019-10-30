package com.lyrebirdstudio.croppylib.util.bitmap

import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import android.graphics.Bitmap
import io.reactivex.Completable
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class BitmapSaver() {

    fun saveBitmap(croppedBitmapData: CroppedBitmapData, file: File): Completable {
        return Completable.create {
            try {
                FileOutputStream(file).use { out ->
                    croppedBitmapData.croppedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.onError(e)
            }

        }
    }
}