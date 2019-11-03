package com.lyrebirdstudio.croppylib.util.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    private const val MAX_SIZE = 1024


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

    fun resize(uri: Uri, context: Context): Single<ResizedBitmap> {
        return Single.create {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)

            var widthTemp = options.outWidth
            var heightTemp = options.outHeight
            var scale = 1

            while (true) {
                if (widthTemp / 2 < MAX_SIZE || heightTemp / 2 < MAX_SIZE)
                    break
                widthTemp /= 2
                heightTemp /= 2
                scale *= 2
            }

            val resultOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val resizedBitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                resultOptions
            )

            it.onSuccess(ResizedBitmap(resizedBitmap))
        }
    }
}