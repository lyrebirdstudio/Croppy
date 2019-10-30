package com.lyrebirdstudio.croppylib.state

import android.content.Context
import android.graphics.RectF
import android.text.Spannable
import android.text.SpannableString
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppylib.inputview.SizeInputData
import com.lyrebirdstudio.croppylib.inputview.SizeInputViewType
import kotlin.math.roundToInt
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.main.CroppyTheme

data class CropFragmentViewState(
    val croppyTheme: CroppyTheme = CroppyTheme.default(),
    val aspectRatio: AspectRatio,
    val sizeInputData: SizeInputData? = null
) {

    fun getWidthButtonText(context: Context): Spannable {
        Log.v("TEST", "text:${croppyTheme.accentColor}")
        if (sizeInputData?.widthValue?.isNaN() == true) {
            return SpannableString("")
        }
        val width = sizeInputData?.widthValue?.roundToInt().toString()
        val wordtoSpan = SpannableString("W $width")
        wordtoSpan.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, croppyTheme.accentColor)),
            0,
            1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        wordtoSpan.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
            1,
            wordtoSpan.length - 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return wordtoSpan
    }

    fun getHeightButtonText(context: Context): Spannable {
        if (sizeInputData?.heightValue?.isNaN() == true) {
            return SpannableString("")
        }
        val height = sizeInputData?.heightValue?.roundToInt().toString()
        val wordtoSpan = SpannableString("H $height")
        wordtoSpan.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, croppyTheme.accentColor)),
            0,
            1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        wordtoSpan.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
            1,
            wordtoSpan.length - 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return wordtoSpan
    }

    fun onAspectRatioChanged(aspectRatio: AspectRatio): CropFragmentViewState {
        return CropFragmentViewState(
            sizeInputData = sizeInputData,
            aspectRatio = aspectRatio,
            croppyTheme = croppyTheme
        )
    }

    fun onCropSizeChanged(cropRect: RectF): CropFragmentViewState {
        return CropFragmentViewState(
            sizeInputData = SizeInputData(
                type = SizeInputViewType.WIDTH,
                widthValue = cropRect.width(),
                heightValue = cropRect.height()
            ),
            aspectRatio = aspectRatio,
            croppyTheme = croppyTheme
        )
    }

    fun onThemeChanged(croppyTheme: CroppyTheme): CropFragmentViewState {
        return CropFragmentViewState(
            sizeInputData = sizeInputData,
            aspectRatio = aspectRatio,
            croppyTheme = croppyTheme
        )
    }
}