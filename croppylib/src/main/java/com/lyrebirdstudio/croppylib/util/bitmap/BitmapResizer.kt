package com.lyrebirdstudio.croppylib.util.bitmap

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import io.reactivex.Single

class BitmapResizer(private val context: Context) {

    fun resize(uri: Uri): Single<ResizedBitmap> {
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

    companion object {
        private const val MAX_SIZE = 1024
    }
}